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

package com.eff3ct.teckel.api.core

import cats.Id
import cats.effect.unsafe.implicits.global
import cats.effect.{Concurrent, IO}
import com.eff3ct.teckel.semantic.core.EvalContext
import com.eff3ct.teckel.serializer.model.etl.{ETL => ETLD}
import fs2.io.file.{Files, Path}

private[api] object ETL {

  def apply[F[_]: Run]: Run[F] = Run[F]

  def unsafe[O: EvalContext](data: String): O = Run[Id].run(data)

  def unsafe[O: EvalContext](data: ETLD): O = Run[Id].run(data)

  def fromFile[F[_]: Files: Concurrent: Run, O: EvalContext](path: String): F[O] =
    Files[F].readUtf8(Path(path)).evalMap(Run[F].run[O]).compile.lastOrError

  def usafeFromFile[O: EvalContext](path: String): O =
    fromFile[IO, O](path).unsafeRunSync()

}
