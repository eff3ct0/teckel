package io.github.rafafrdz.teckel.core.transformation.join

trait JoinType
object JoinType {
  case object Inner extends JoinType
  case object Left  extends JoinType
  case object Right extends JoinType
  case object Cross extends JoinType

}
