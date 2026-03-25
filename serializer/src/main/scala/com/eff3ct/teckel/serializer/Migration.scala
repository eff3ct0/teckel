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

object Migration {

  case class MigrationRule(
      fromVersion: String,
      toVersion: String,
      description: String,
      migrate: String => String
  )

  private var rules: List[MigrationRule] = List(
    MigrationRule(
      "1.0",
      "2.0",
      "Rename 'filter' to 'where' in transformation definitions",
      content => content.replace("filter:", "where:")
    )
  )

  def currentVersion: String = "2.0"

  def registerRule(rule: MigrationRule): Unit =
    rules = rules :+ rule

  def migrate(content: String, fromVersion: String, toVersion: String): Either[String, String] = {
    val path = findMigrationPath(fromVersion, toVersion)
    if (path.isEmpty && fromVersion != toVersion)
      Left(s"No migration path from $fromVersion to $toVersion")
    else
      Right(path.foldLeft(content)((c, rule) => rule.migrate(c)))
  }

  private def findMigrationPath(from: String, to: String): List[MigrationRule] =
    rules.filter(r => r.fromVersion == from && r.toVersion == to)

  def checkDeprecations(content: String): List[String] = {
    var warnings = List.empty[String]
    if (content.contains("filter:"))
      warnings = warnings :+ "Deprecated: 'filter' field should be renamed to 'where'"
    warnings
  }
}
