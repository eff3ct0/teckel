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

package com.eff3ct.teckel.serializer.model

import cats.data.NonEmptyList
import cats.implicits._
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.{Decoder, Encoder, Json}

object operations {

  sealed trait Operation

  implicit val encodeEvent: Encoder[Operation] =
    Encoder.instance {
      case s: SelectOp        => s.asJson
      case w: WhereOp         => w.asJson
      case g: GroupByOp       => g.asJson
      case o: OrderByOp       => o.asJson
      case j: JoinOp          => j.asJson
      case d: DistinctOp      => d.asJson
      case l: LimitOp         => l.asJson
      case a: AddColumnsOp    => a.asJson
      case d: DropColumnsOp   => d.asJson
      case r: RenameColumnsOp => r.asJson
      case c: CastColumnsOp   => c.asJson
      case s: SqlOp           => s.asJson
      case u: UnionOp         => u.asJson
      case i: IntersectOp     => i.asJson
      case e: ExceptOp        => e.asJson
      case w: WindowOp        => w.asJson
      case f: FlattenOp       => f.asJson
      case s: SampleOp        => s.asJson
      case r: RepartitionOp   => r.asJson
      case c: CoalesceOp      => c.asJson
      case ro: RollupOp       => ro.asJson
      case cu: CubeOp         => cu.asJson
      case p: PivotOp         => p.asJson
      case u: UnpivotOp       => u.asJson
      case c: ConditionalOp   => c.asJson
      case s: SplitOp         => s.asJson
      case s: SCD2Op          => s.asJson
      case e: EnrichOp        => e.asJson
      case s: SchemaEnforceOp => s.asJson
      case cu: CustomOp       => cu.asJson
      case a: AssertionOp     => a.asJson
    }

  implicit val decodeEvent: Decoder[Operation] =
    List[Decoder[Operation]](
      Decoder[SelectOp].widen,
      Decoder[WhereOp].widen,
      Decoder[GroupByOp].widen,
      Decoder[OrderByOp].widen,
      Decoder[JoinOp].widen,
      Decoder[DistinctOp].widen,
      Decoder[LimitOp].widen,
      Decoder[AddColumnsOp].widen,
      Decoder[DropColumnsOp].widen,
      Decoder[RenameColumnsOp].widen,
      Decoder[CastColumnsOp].widen,
      Decoder[SqlOp].widen,
      Decoder[UnionOp].widen,
      Decoder[IntersectOp].widen,
      Decoder[ExceptOp].widen,
      Decoder[WindowOp].widen,
      Decoder[FlattenOp].widen,
      Decoder[SampleOp].widen,
      Decoder[RepartitionOp].widen,
      Decoder[CoalesceOp].widen,
      Decoder[RollupOp].widen,
      Decoder[CubeOp].widen,
      Decoder[PivotOp].widen,
      Decoder[UnpivotOp].widen,
      Decoder[ConditionalOp].widen,
      Decoder[SplitOp].widen,
      Decoder[SCD2Op].widen,
      Decoder[EnrichOp].widen,
      Decoder[SchemaEnforceOp].widen,
      Decoder[AssertionOp].widen,
      Decoder[CustomOp].widen
    ).reduceLeft(_ or _)

  case class SelectOp(from: String, columns: NonEmptyList[String]) extends Operation
  case class WhereOp(from: String, filter: String)                 extends Operation
  case class GroupByOp(from: String, by: NonEmptyList[String], agg: NonEmptyList[String])
      extends Operation
  case class OrderByOp(from: String, by: NonEmptyList[String], order: Option[String])
      extends Operation

  case class JoinOp(left: String, right: NonEmptyList[Relation])             extends Operation
  case class DistinctOp(from: String, columns: Option[NonEmptyList[String]]) extends Operation
  case class LimitOp(from: String, count: Int)                               extends Operation

  case class ColumnDef(name: String, expression: String)
  case class CastColumnDef(name: String, targetType: String)

  case class AddColumnsOp(from: String, columns: NonEmptyList[ColumnDef])      extends Operation
  case class DropColumnsOp(from: String, columns: NonEmptyList[String])        extends Operation
  case class RenameColumnsOp(from: String, mappings: Map[String, String])      extends Operation
  case class CastColumnsOp(from: String, columns: NonEmptyList[CastColumnDef]) extends Operation
  case class SqlOp(from: String, query: String)                                extends Operation

  case class UnionOp(sources: NonEmptyList[String], all: Option[Boolean])     extends Operation
  case class IntersectOp(sources: NonEmptyList[String], all: Option[Boolean]) extends Operation
  case class ExceptOp(left: String, right: String, all: Option[Boolean])      extends Operation

  case class WindowFuncDef(expression: String, alias: String)

  case class WindowOp(
      from: String,
      partitionBy: NonEmptyList[String],
      orderBy: Option[NonEmptyList[String]],
      functions: NonEmptyList[WindowFuncDef]
  ) extends Operation

  case class FlattenOp(from: String, separator: Option[String], explodeArrays: Option[Boolean])
      extends Operation

  case class SampleOp(
      from: String,
      fraction: Double,
      withReplacement: Option[Boolean],
      seed: Option[Long]
  ) extends Operation

  case class RepartitionOp(from: String, numPartitions: Int, columns: Option[NonEmptyList[String]])
      extends Operation

  case class CoalesceOp(from: String, numPartitions: Int) extends Operation

  case class RollupOp(from: String, by: NonEmptyList[String], agg: NonEmptyList[String])
      extends Operation

  case class CubeOp(from: String, by: NonEmptyList[String], agg: NonEmptyList[String])
      extends Operation

  case class PivotOp(
      from: String,
      groupBy: NonEmptyList[String],
      pivotColumn: String,
      values: Option[List[String]],
      agg: NonEmptyList[String]
  ) extends Operation

  case class UnpivotOp(
      from: String,
      ids: NonEmptyList[String],
      values: NonEmptyList[String],
      variableColumn: String,
      valueColumn: String
  ) extends Operation

  case class WhenBranchDef(condition: String, value: String)

  case class ConditionalOp(
      from: String,
      outputColumn: String,
      branches: NonEmptyList[WhenBranchDef],
      otherwise: Option[String]
  ) extends Operation

  case class SplitOp(from: String, condition: String, pass: String, fail: String) extends Operation

  case class SCD2Op(
      from: String,
      keyColumns: NonEmptyList[String],
      trackColumns: NonEmptyList[String],
      startDateColumn: String,
      endDateColumn: String,
      currentFlagColumn: String
  ) extends Operation

  case class EnrichOp(
      from: String,
      url: String,
      method: Option[String],
      keyColumn: String,
      responseColumn: String,
      headers: Option[Map[String, String]]
  ) extends Operation

  case class SchemaColumnDef(
      name: String,
      dataType: String,
      nullable: Option[Boolean],
      default: Option[String]
  )
  case class SchemaEnforceOp(
      from: String,
      columns: NonEmptyList[SchemaColumnDef],
      mode: Option[String]
  ) extends Operation

  case class QualityCheckDef(column: Option[String], rule: String, description: Option[String])

  case class AssertionOp(
      from: String,
      checks: NonEmptyList[QualityCheckDef],
      onFailure: Option[String]
  ) extends Operation

  case class CustomOp(from: String, component: String, options: Option[Map[String, String]])
      extends Operation

  case class Relation(name: String, relationType: String, on: List[String])

  implicit val encodeRelationType: Encoder[Relation] =
    Encoder.instance { r =>
      Json.obj(
        "name" -> r.name.asJson,
        "type" -> r.relationType.asJson,
        "on"   -> r.on.asJson
      )
    }

  implicit val decodeRelationType: Decoder[Relation] =
    Decoder.instance { c =>
      for {
        name         <- c.downField("name").as[String]
        relationType <- c.downField("type").as[String]
        on           <- c.downField("on").as[List[String]]
      } yield Relation(name, relationType, on)
    }

}
