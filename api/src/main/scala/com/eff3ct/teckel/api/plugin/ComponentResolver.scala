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

package com.eff3ct.teckel.api.plugin

import com.eff3ct.teckel.model.plugin.PluginRegistry

object ComponentResolver {

  val CORE_NAMESPACE: Map[String, String] = Map(
    "reader"      -> "com.eff3ct.teckel.plugins.readers",
    "transformer" -> "com.eff3ct.teckel.plugins.transformers",
    "writer"      -> "com.eff3ct.teckel.plugins.writers"
  )

  case class ComponentSpec(
      kind: String,
      alias: String,
      className: String,
      module: Option[String]
  )

  def resolve(kind: String, name: Option[String], classPath: Option[String]): Any =
    classPath match {
      case Some(cp) =>
        // Full class path - load by reflection
        val cls = Class.forName(cp)
        cls.getDeclaredConstructor().newInstance()
      case None =>
        name match {
          case Some(n) =>
            // Try registry first, then core namespace
            try PluginRegistry.resolve(kind, n)
            catch {
              case _: IllegalArgumentException =>
                val fullClass = s"${CORE_NAMESPACE(kind)}.$n"
                PluginRegistry.resolveByClassName(kind, fullClass)
            }
          case None =>
            throw new IllegalArgumentException(s"Component requires either 'name' or 'class'")
        }
    }
}
