package io.github.rafafrdz.teckel.core.transformation.join

import io.github.rafafrdz.teckel.core.Operation.Transformation
import io.github.rafafrdz.teckel.core.Source
import org.apache.spark.sql.Column

case class Join(joinType: JoinType, relation: JoinRelation) extends Transformation
object Join {
  case class JoinRelation(left: Source, right: Source, on: List[RelationField])
  case class RelationField(left: Column, right: Column)
}
