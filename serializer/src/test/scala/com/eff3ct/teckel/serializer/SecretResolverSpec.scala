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

import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers

class SecretResolverSpec extends AnyFlatSpecLike with Matchers {

  /** A test-only secrets provider backed by a simple map. */
  private def mapProvider(secrets: Map[(String, String), String]): SecretsProvider =
    new SecretsProvider {
      override def get(scope: String, key: String): Option[String] =
        secrets.get((scope, key))
    }

  "SecretResolver" should "resolve a secret placeholder using a custom provider" in {
    val content =
      """secrets:
        |  keys:
        |    db-password:
        |      scope: production
        |      key: db-pass
        |connection:
        |  password: '{{secrets.db-password}}'""".stripMargin

    val provider = mapProvider(Map(("production", "db-pass") -> "s3cret!"))
    val result   = SecretResolver.resolve(content, provider)
    result should include("s3cret!")
    result should not include "{{secrets.db-password}}"
  }

  it should "resolve multiple secret placeholders" in {
    val content =
      """secrets:
        |  keys:
        |    user:
        |      scope: app
        |      key: username
        |    pass:
        |      scope: app
        |      key: password
        |db:
        |  user: '{{secrets.user}}'
        |  pass: '{{secrets.pass}}'""".stripMargin

    val provider = mapProvider(
      Map(
        ("app", "username") -> "admin",
        ("app", "password") -> "hunter2"
      )
    )
    val result = SecretResolver.resolve(content, provider)
    result should include("admin")
    result should include("hunter2")
  }

  it should "throw IllegalArgumentException for unresolvable secret" in {
    val content =
      """connection:
        |  password: '{{secrets.missing-key}}'""".stripMargin

    val provider = mapProvider(Map.empty)
    an[IllegalArgumentException] should be thrownBy {
      SecretResolver.resolve(content, provider)
    }
  }

  it should "include the secret alias in the error message" in {
    val content  = "value: '{{secrets.my-alias}}'"
    val provider = mapProvider(Map.empty)
    val ex = the[IllegalArgumentException] thrownBy {
      SecretResolver.resolve(content, provider)
    }
    ex.getMessage should include("my-alias")
  }

  it should "return content unchanged when no secret placeholders are present" in {
    val content  = "path: /data/files\nformat: parquet"
    val provider = mapProvider(Map.empty)
    SecretResolver.resolve(content, provider) shouldBe content
  }

  it should "fall back to scope-less lookup when no secrets.keys section exists" in {
    val content  = "password: '{{secrets.db-pass}}'"
    val provider = mapProvider(Map(("", "db-pass") -> "fallback-secret"))
    val result   = SecretResolver.resolve(content, provider)
    result should include("fallback-secret")
  }

  it should "handle special regex characters in secret values" in {
    val content =
      """secrets:
        |  keys:
        |    token:
        |      scope: ci
        |      key: api-token
        |auth: '{{secrets.token}}'""".stripMargin

    val provider = mapProvider(Map(("ci", "api-token") -> "abc$123\\def"))
    val result   = SecretResolver.resolve(content, provider)
    result should include("abc$123\\def")
  }
}
