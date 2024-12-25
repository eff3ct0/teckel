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
  case object STDIN             extends Commands
  case class FILE(file: String) extends Commands

  def parseCommand(args: List[String]): Commands =
    args match {
      case "-c" :: Nil         => STDIN
      case "-f" :: file :: Nil => FILE(file)
      case _                   => throw new IllegalArgumentException("Invalid arguments")
    }

  def eval[F[_]: Files: Async: Run, O: EvalContext](commands: Commands): fs2.Stream[F, O] =
    commands match {
      case STDIN      => Parser.parseStdin[F, O]
      case FILE(file) => Parser.parseFile[F, O](file)
    }

  def command[F[_]: Sync](args: List[String]): fs2.Stream[F, Commands] =
    fs2.Stream.eval(Sync[F].delay(parseCommand(args)))

}
