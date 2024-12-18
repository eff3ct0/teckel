package io.github.rafafrdz.teckle.yaml

import io.github.rafafrdz.teckle.model.AST.{Asset, Input => I, Output => O}
import io.github.rafafrdz.teckle.yaml.AST._

object transform {

  def map(options: OptionItem): Map[String, String] =
    List(
      options.header.map(v => "header" -> v.toString),
      options.sep.map(v => "sep" -> v)
    ).flatten.toMap

  def map(item: Input): Asset =
    Asset(item.name, I(item.format, item.options.map(map).getOrElse(Map.empty), item.path))

  def map(item: Output): Asset =
    Asset(
      s"output_${item.name}",
      O(item.name, item.format, item.options.map(map).getOrElse(Map.empty), item.path)
    )

  def map(item: ETL): Map[String, Asset] =
    (item.input.map { i =>
      val asset = map(i)
      asset.assetRef -> asset
    } :::
      item.output.map { i =>
        val asset = map(i)
        asset.assetRef -> asset
      }).toMap

}
