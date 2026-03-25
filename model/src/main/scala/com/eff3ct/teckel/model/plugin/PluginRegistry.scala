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

package com.eff3ct.teckel.model.plugin

import scala.collection.mutable
import scala.util.Try

object PluginRegistry {

  private val registry: mutable.Map[String, mutable.Map[String, Class[_]]] = mutable.Map(
    "reader"      -> mutable.Map.empty,
    "transformer" -> mutable.Map.empty,
    "writer"      -> mutable.Map.empty
  )

  def register(kind: String, name: String, cls: Class[_]): Unit = {
    require(
      registry.contains(kind),
      s"Unknown kind: $kind. Must be reader, transformer, or writer"
    )
    registry(kind)(name) = cls
  }

  def resolve[T](kind: String, name: String): T = {
    val cls = registry
      .getOrElse(kind, throw new IllegalArgumentException(s"Unknown kind: $kind"))
      .getOrElse(
        name,
        throw new IllegalArgumentException(
          s"$kind '$name' not registered. Available: ${registry(kind).keys.mkString(", ")}"
        )
      )
    cls.getDeclaredConstructor().newInstance().asInstanceOf[T]
  }

  def resolveByClassName(kind: String, className: String): Any = {
    val cls      = Class.forName(className)
    val instance = cls.getDeclaredConstructor().newInstance()
    register(kind, cls.getSimpleName, cls)
    instance
  }

  def loadPlugins(classNames: List[String]): Unit =
    classNames.foreach { cn =>
      Try(Class.forName(cn)).recover { case e =>
        System.err.println(s"[plugin] Failed to load '$cn': ${e.getMessage}")
      }
    }

  def list(kind: String): Set[String] =
    registry.getOrElse(kind, mutable.Map.empty).keySet.toSet

  def clear(): Unit = registry.values.foreach(_.clear())
}
