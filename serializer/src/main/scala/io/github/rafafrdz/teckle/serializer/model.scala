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

package io.github.rafafrdz.teckle.serializer

import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive

object model {

  @derive(encoder, decoder)
  case class Input(name: String, format: String, path: String, options: Option[OptionItem])

  @derive(encoder, decoder)
  case class Output(
      name: String,
      format: String,
      mode: String,
      path: String,
      options: Option[OptionItem]
  )

  @derive(encoder, decoder)
  case class ETL(input: List[Input], output: List[Output])

  @derive(encoder, decoder)
  case class OptionItem(header: Option[Boolean], sep: Option[String])

}
