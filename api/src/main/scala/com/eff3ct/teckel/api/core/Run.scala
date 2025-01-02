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

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.implicits._
import cats.{Id, Monad, MonadThrow}
import com.eff3ct.teckel.semantic.core.EvalContext
import com.eff3ct.teckel.serializer._
import com.eff3ct.teckel.serializer.model.etl._
import com.eff3ct.teckel.transform.Rewrite

trait Run[F[_]] {
  def run[O: EvalContext](data: String): F[O]
  def run[O: EvalContext](data: ETL): F[O]
}

object Run {

  def apply[F[_]: Run]: Run[F] = implicitly[Run[F]]

  implicit def runF[F[_]: MonadThrow]: Run[F] = new Run[F] {
    override def run[O: EvalContext](data: String): F[O] =
      for {
        etl <- MonadThrow[F].fromEither(Serializer[ETL].decode(data))
        context = Rewrite.rewrite(etl)
      } yield EvalContext[O].eval(context)

    override def run[O: EvalContext](data: ETL): F[O] =
      for {
        etl <- Monad[F].pure(data)
        context = Rewrite.rewrite(etl)
      } yield EvalContext[O].eval(context)
  }

  implicit val runId: Run[Id] = new Run[Id] {
    override def run[O: EvalContext](data: String): Id[O] =
      Run[IO].run(data).unsafeRunSync()

    override def run[O: EvalContext](data: ETL): Id[O] =
      Run[IO].run(data).unsafeRunSync()
  }
}
