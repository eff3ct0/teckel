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

import cats.effect.Sync
import cats.implicits._
import com.eff3ct.teckel.serializer.model.etl.{Hook, Hooks}

object HookRunner {

  def runPreHooks[F[_]: Sync](hooks: Option[Hooks]): F[Unit] =
    hooks.flatMap(_.preExecution).traverse_ { hookList =>
      hookList.traverse_ { hook =>
        Sync[F].delay {
          println(s"[hook:pre] Running '${hook.name}': ${hook.command}")
          val process  = Runtime.getRuntime.exec(Array("/bin/sh", "-c", hook.command))
          val exitCode = process.waitFor()
          if (exitCode != 0)
            throw new RuntimeException(
              s"Pre-execution hook '${hook.name}' failed with exit code $exitCode"
            )
          println(s"[hook:pre] '${hook.name}' completed successfully")
        }
      }
    }

  def runPostHooks[F[_]: Sync](hooks: Option[Hooks]): F[Unit] =
    hooks.flatMap(_.postExecution).traverse_ { hookList =>
      hookList.traverse_ { hook =>
        Sync[F].delay {
          println(s"[hook:post] Running '${hook.name}': ${hook.command}")
          val process  = Runtime.getRuntime.exec(Array("/bin/sh", "-c", hook.command))
          val exitCode = process.waitFor()
          if (exitCode != 0)
            println(s"[hook:post] Warning: '${hook.name}' failed with exit code $exitCode")
          else
            println(s"[hook:post] '${hook.name}' completed successfully")
        }
      }
    }
}
