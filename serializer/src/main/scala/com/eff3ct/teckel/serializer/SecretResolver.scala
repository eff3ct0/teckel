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

import io.circe.Json
import io.circe.yaml.parser

object SecretResolver {

  private val secretPattern = """\{\{secrets\.([^}]+)\}\}""".r

  case class SecretKey(alias: String, scope: String, key: String)

  def resolve(content: String, provider: SecretsProvider = SecretsProvider.default): String = {
    // First, try to parse the YAML to extract the secrets.keys section
    val secretKeys: Map[String, SecretKey] = parser
      .parse(content)
      .toOption
      .flatMap(_.hcursor.downField("secrets").downField("keys").as[Map[String, Json]].toOption)
      .map(_.flatMap { case (alias, json) =>
        for {
          scope <- json.hcursor.downField("scope").as[String].toOption
          key   <- json.hcursor.downField("key").as[String].toOption
        } yield alias -> SecretKey(alias, scope, key)
      })
      .getOrElse(Map.empty)

    // Replace all {{secrets.alias}} placeholders
    secretPattern.replaceAllIn(
      content,
      { m =>
        val alias = m.group(1)
        val value = secretKeys.get(alias) match {
          case Some(sk) => provider.get(sk.scope, sk.key)
          case None     => provider.get("", alias)
        }
        value match {
          case Some(v) => java.util.regex.Matcher.quoteReplacement(v)
          case None =>
            throw new IllegalArgumentException(
              s"Secret '{{secrets.$alias}}' could not be resolved. Set environment variable SECRETS__${alias.toUpperCase
                  .replace("-", "_")} or configure a secrets provider."
            )
        }
      }
    )
  }
}
