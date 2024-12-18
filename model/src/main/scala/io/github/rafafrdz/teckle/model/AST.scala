package io.github.rafafrdz.teckle.model

object AST {

  type AssetRef  = String
  type SourceRef = String
  type Format    = String
  type Options   = Map[String, String]

  type Context = Map[AssetRef, Asset]

  case class Asset(assetRef: AssetRef, source: Source)

  sealed trait Source

  case class Input(format: Format, options: Options, sourceRef: SourceRef)
    extends Source

  case class Output(assetRef: AssetRef, format: Format, options: Options, sourceRef: SourceRef)
      extends Source

}
