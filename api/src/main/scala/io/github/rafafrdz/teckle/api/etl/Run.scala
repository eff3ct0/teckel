/*
 * Invasion Order Software License Agreement
 *
 * This file is part of the proprietary software provided by Invasion Order.
 * Use of this file is governed by the terms and conditions outlined in the
 * Invasion Order Software License Agreement.
 *
 * Unauthorized copying, modification, or distribution of this file, via any
 * medium, is strictly prohibited. The software is provided "as is," without
 * warranty of any kind, express or implied.
 *
 * For the full license text, please refer to the LICENSE file included
 * with this distribution, or contact Invasion Order at contact@iorder.dev.
 *
 * (c) 2024 Invasion Order. All rights reserved.
 */

package io.github.rafafrdz.teckle.api.etl

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.implicits._
import cats.{Id, MonadThrow}
import fs2.io.file.{Files, Path}
import io.github.rafafrdz.teckle.semantic.EvalContext
import io.github.rafafrdz.teckle.serializer._
import io.github.rafafrdz.teckle.serializer.model.ETL
import io.github.rafafrdz.teckle.transform.Rewrite

trait Run[F[_]] {
  def run[O: EvalContext](path: String): F[O]

}
object Run {

  def apply[F[_]: Run]: Run[F] = implicitly[Run[F]]

  implicit def runF[F[_]: Compile: Files: MonadThrow]: Run[F] = new Run[F] {
    override def run[O: EvalContext](path: String): F[O] =
      for {
        data <- Files[F].readUtf8(Path(path)).compile.lastOrError
        etl  <- MonadThrow[F].fromEither(Serializer[ETL].decode(data))
        context = Rewrite.rewrite(etl)
      } yield EvalContext[O].eval(context)
  }

  implicit val runId: Run[Id] = new Run[Id] {
    override def run[O: EvalContext](path: String): Id[O] =
      Run[IO].run(path).unsafeRunSync()
  }
}
