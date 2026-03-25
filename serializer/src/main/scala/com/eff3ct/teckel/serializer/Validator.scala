/*
 * MIT License
 *
 * Copyright (c) 2024 Rafael Fernandez
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.eff3ct.teckel.serializer

import cats.data.ValidatedNel
import cats.implicits._
import com.eff3ct.teckel.model.{Asset, AssetRef, Context}
import com.eff3ct.teckel.model.Source._

object Validator {

  type ValidationResult = ValidatedNel[String, Unit]

  def validate(context: Context[Asset]): ValidationResult =
    List(
      validateReferences(context),
      validateCycles(context),
      validateOutputReferences(context)
    ).combineAll

  private def validateReferences(context: Context[Asset]): ValidationResult =
    context.toList.traverse_ { case (ref, asset) =>
      asset.source match {
        case _: Input => ().validNel
        case s: Output =>
          if (context.contains(s.assetRef)) ().validNel
          else
            s"Output '${ref}' references unknown asset '${s.assetRef}'${suggest(s.assetRef, context.keys)}".invalidNel
        case t: Transformation =>
          val deps = dependencies(ref, context)
          deps.traverse_ { dep =>
            if (context.contains(dep)) ().validNel
            else
              s"Transformation '${ref}' references unknown asset '${dep}'${suggest(dep, context.keys)}".invalidNel
          }
      }
    }

  private def validateOutputReferences(context: Context[Asset]): ValidationResult =
    context.toList.traverse_ { case (ref, asset) =>
      asset.source match {
        case s: Output =>
          val sourceRef = s.assetRef
          context.get(sourceRef).map(_.source) match {
            case Some(_: Input) | Some(_: Transformation) => ().validNel
            case Some(_: Output) =>
              s"Output '${ref}' references another output '${sourceRef}' — outputs must reference inputs or transformations".invalidNel
            case None => ().validNel // already caught by validateReferences
          }
        case _ => ().validNel
      }
    }

  private def validateCycles(context: Context[Asset]): ValidationResult = {
    def hasCycle(
        node: AssetRef,
        visited: Set[AssetRef],
        path: List[AssetRef]
    ): Option[List[AssetRef]] =
      if (visited.contains(node)) Some(path.reverse :+ node)
      else {
        val deps = dependencies(node, context)
        deps.collectFirst {
          case dep if hasCycle(dep, visited + node, node :: path).isDefined =>
            hasCycle(dep, visited + node, node :: path).get
        }
      }

    context.keys.toList.traverse_ { ref =>
      hasCycle(ref, Set.empty, Nil) match {
        case Some(cycle) =>
          s"Circular dependency detected: ${cycle.mkString(" -> ")}".invalidNel
        case None => ().validNel
      }
    }
  }

  private def dependencies(ref: AssetRef, context: Context[Asset]): List[AssetRef] =
    context.get(ref).map(_.source).toList.flatMap {
      case _: Input          => Nil
      case s: Output         => List(s.assetRef)
      case s: Join           => s.assetRef :: s.others.toList.map(_.name)
      case s: Union          => s.assetRef :: s.others.toList
      case s: Intersect      => s.assetRef :: s.others.toList
      case s: Except         => List(s.assetRef, s.other)
      case t: Transformation => List(t.assetRef)
    }

  private def suggest(target: String, candidates: Iterable[String]): String = {
    val suggestions = candidates
      .filter(c => levenshtein(c.toLowerCase, target.toLowerCase) <= 3)
      .toList
      .sortBy(c => levenshtein(c.toLowerCase, target.toLowerCase))
      .take(3)
    if (suggestions.nonEmpty) s". Did you mean: ${suggestions.mkString(", ")}?" else ""
  }

  private def levenshtein(s1: String, s2: String): Int = {
    val dist = Array.tabulate(s1.length + 1, s2.length + 1) { (i, j) =>
      if (i == 0) j else if (j == 0) i else 0
    }
    for (i <- 1 to s1.length; j <- 1 to s2.length) {
      val cost = if (s1(i - 1) == s2(j - 1)) 0 else 1
      dist(i)(j) = (dist(i - 1)(j) + 1).min(dist(i)(j - 1) + 1).min(dist(i - 1)(j - 1) + cost)
    }
    dist(s1.length)(s2.length)
  }

  def formatErrors(result: ValidationResult): Option[String] =
    result.toEither.left.toOption.map { errors =>
      s"Pipeline validation failed with ${errors.size} error(s):\n${errors.toList.zipWithIndex
          .map { case (e, i) => s"  ${i + 1}. $e" }
          .mkString("\n")}"
    }
}
