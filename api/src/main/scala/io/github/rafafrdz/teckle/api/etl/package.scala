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

package io.github.rafafrdz.teckle.api

import cats.Id
import cats.effect.IO
import fs2.Compiler
import io.github.rafafrdz.teckle.semantic.EvalContext

package object etl {

  type Compile[F[_]] = Compiler[F, F]

  def etlF[F[_]: Run, O: EvalContext](path: String): F[O] = Run[F].run(path)
  def etl[O: EvalContext](path: String): IO[O]            = Run[IO].run(path)
  def unsafeETL[O: EvalContext](path: String): O          = Run[Id].run(path)
}
