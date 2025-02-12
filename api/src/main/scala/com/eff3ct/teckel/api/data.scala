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

import cats.effect.IO
import com.eff3ct.teckel.api.core.{ETL => ETLC, Run}
import com.eff3ct.teckel.semantic.core.EvalContext
import com.eff3ct.teckel.serializer.model.etl.ETL

object data {
  def etl[F[_]: Run, O: EvalContext](data: String): F[O] = ETLC[F].run[O](data)
  def etl[F[_]: Run, O: EvalContext](data: ETL): F[O]    = ETLC[F].run[O](data)
  def etlIO[O: EvalContext](data: String): IO[O]         = ETLC[IO].run[O](data)
  def etlIO[O: EvalContext](data: ETL): IO[O]            = ETLC[IO].run[O](data)
  def unsafeETL[O: EvalContext](data: String): O         = ETLC.unsafe(data)
  def unsafeETL[O: EvalContext](data: ETL): O            = ETLC.unsafe(data)

}
