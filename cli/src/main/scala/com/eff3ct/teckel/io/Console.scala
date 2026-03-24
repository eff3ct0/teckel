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

package com.eff3ct.teckel.io

import cats.effect.{Async, Sync}
import com.eff3ct.teckel.api.core.Run
import com.eff3ct.teckel.semantic.core.EvalContext
import fs2.io.file.Files

object Console {

  sealed trait Commands
  case class STDIN(variables: Map[String, String])                                    extends Commands
  case class FILE(file: String, variables: Map[String, String], dryRun: Boolean = false, env: Option[String] = None)
      extends Commands

  def parseCommand(args: List[String]): Commands = {
    val dryRun = args.contains("--dry-run")
    val variables = args
      .filter(_.startsWith("-D"))
      .map(_.stripPrefix("-D"))
      .map { kv =>
        val parts = kv.split("=", 2)
        if (parts.length == 2) parts(0) -> parts(1)
        else
          throw new IllegalArgumentException(
            s"Invalid variable definition: -D$kv (expected -Dkey=value)"
          )
      }
      .toMap

    val filteredArgs = args.filterNot(a => a.startsWith("-D") || a == "--dry-run")

    val envIdx = filteredArgs.indexOf("--env")
    val (env, argsWithoutEnv) = if (envIdx >= 0 && envIdx + 1 < filteredArgs.length) {
      (Some(filteredArgs(envIdx + 1)), filteredArgs.take(envIdx) ++ filteredArgs.drop(envIdx + 2))
    } else (None, filteredArgs)

    argsWithoutEnv match {
      case "-c" :: Nil         => STDIN(variables)
      case "-f" :: file :: Nil => FILE(file, variables, dryRun, env)
      case _ =>
        throw new IllegalArgumentException(
          s"Invalid arguments: ${args.mkString(" ")}. Usage: -f <file> [--env <env>] [--dry-run] [-D key=value ...] or -c [-D key=value ...]"
        )
    }
  }

  def eval[F[_]: Files: Async: Run, O: EvalContext](commands: Commands): fs2.Stream[F, O] =
    commands match {
      case STDIN(variables)              => Parser.parseStdin[F, O](variables)
      case FILE(file, variables, _, env) => Parser.parseFile[F, O](file, variables, env)
    }

  def command[F[_]: Sync](args: List[String]): fs2.Stream[F, Commands] =
    fs2.Stream.eval(Sync[F].delay(parseCommand(args)))

}
