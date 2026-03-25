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

package com.eff3ct.teckel.api

import com.eff3ct.teckel.model.{Asset, Context}
import com.eff3ct.teckel.model.Source._
import com.eff3ct.teckel.serializer.Serializer
import com.eff3ct.teckel.serializer.model.etl._
import com.eff3ct.teckel.transform.Rewrite

object GraphViz {

  def generate(yamlContent: String, format: String): Either[Throwable, String] =
    Serializer[ETL].decode(yamlContent).map { etl =>
      val context = Rewrite.rewrite(etl)
      format.toLowerCase match {
        case "mermaid" => toMermaid(context)
        case "dot"     => toDot(context)
        case "ascii"   => toAscii(context)
        case other     => s"Unknown format: $other"
      }
    }

  private def nodeShape(asset: Asset): String = asset.source match {
    case _: Input          => "input"
    case _: Output         => "output"
    case _: Transformation => "transform"
  }

  // Get all referenced asset refs for a source
  private def dependencies(source: com.eff3ct.teckel.model.Source): List[String] = source match {
    case j: Join =>
      (List(j.assetRef) ++ j.others.toList.map(_.name)).distinct
    case u: Union =>
      (List(u.assetRef) ++ u.others.toList).distinct
    case i: Intersect =>
      (List(i.assetRef) ++ i.others.toList).distinct
    case e: Except =>
      List(e.assetRef, e.other).distinct
    case t: Transformation =>
      List(t.assetRef)
    case o: Output =>
      List(o.assetRef)
    case _: Input =>
      Nil
  }

  def toMermaid(context: Context[Asset]): String = {
    val sb = new StringBuilder("graph LR\n")
    context.foreach { case (ref, asset) =>
      val shape = nodeShape(asset)
      val label = shape match {
        case "input"     => s"  $ref[/$ref/]\n"
        case "output"    => s"  $ref[\\$ref\\]\n"
        case "transform" => s"  $ref[$ref]\n"
      }
      sb.append(label)
    }
    context.foreach { case (ref, asset) =>
      dependencies(asset.source).foreach { dep =>
        sb.append(s"  $dep --> $ref\n")
      }
    }
    sb.toString
  }

  def toDot(context: Context[Asset]): String = {
    val sb = new StringBuilder("digraph pipeline {\n  rankdir=LR;\n")
    context.foreach { case (ref, asset) =>
      val shape = nodeShape(asset) match {
        case "input"  => "parallelogram"
        case "output" => "parallelogram"
        case _        => "box"
      }
      sb.append(s"""  "$ref" [shape=$shape];\n""")
    }
    context.foreach { case (ref, asset) =>
      dependencies(asset.source).foreach { dep =>
        sb.append(s"""  "$dep" -> "$ref";\n""")
      }
    }
    sb.append("}\n")
    sb.toString
  }

  def toAscii(context: Context[Asset]): String = {
    val sb = new StringBuilder("Pipeline DAG:\n")
    val inputs     = context.filter(_._2.source.isInstanceOf[Input]).keys
    val outputs    = context.filter(_._2.source.isInstanceOf[Output]).keys
    val transforms = context.filter(e => e._2.source.isInstanceOf[Transformation]).keys

    sb.append("\nInputs:\n")
    inputs.foreach(i => sb.append(s"  [$i]\n"))
    sb.append("\nTransformations:\n")
    transforms.foreach { t =>
      val deps = dependencies(context(t).source)
      sb.append(s"  ${deps.mkString(", ")} -> [$t]\n")
    }
    sb.append("\nOutputs:\n")
    outputs.foreach { o =>
      val deps = dependencies(context(o).source)
      sb.append(s"  ${deps.mkString(", ")} -> [$o]\n")
    }
    sb.toString
  }
}
