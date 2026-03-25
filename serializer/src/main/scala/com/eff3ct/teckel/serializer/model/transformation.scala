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

import cats.implicits._
import com.eff3ct.teckel.serializer.model.operations._
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.{Decoder, Encoder}

object transformation {
  sealed trait Transformation

  implicit val encodeEvent: Encoder[Transformation] =
    Encoder.instance {
      case s: Select         => s.asJson
      case w: Where          => w.asJson
      case g: GroupBy        => g.asJson
      case o: OrderBy        => o.asJson
      case j: Join           => j.asJson
      case d: Distinct       => d.asJson
      case l: Limit          => l.asJson
      case a: AddColumns     => a.asJson
      case d: DropColumns    => d.asJson
      case r: RenameColumns  => r.asJson
      case c: CastColumns    => c.asJson
      case s: SqlExpr        => s.asJson
      case u: UnionT         => u.asJson
      case i: IntersectT     => i.asJson
      case e: ExceptT        => e.asJson
      case w: WindowT        => w.asJson
      case f: FlattenT       => f.asJson
      case s: SampleT        => s.asJson
      case r: RepartitionT   => r.asJson
      case c: CoalesceT      => c.asJson
      case ro: RollupT       => ro.asJson
      case cu: CubeT         => cu.asJson
      case p: PivotT         => p.asJson
      case u: UnpivotT       => u.asJson
      case c: ConditionalT   => c.asJson
      case s: SplitT         => s.asJson
      case s: SCD2T          => s.asJson
      case e: EnrichT        => e.asJson
      case a: AssertionT     => a.asJson
      case s: SchemaEnforceT => s.asJson
      case cu: CustomT       => cu.asJson
    }

  implicit val decodeEvent: Decoder[Transformation] =
    List[Decoder[Transformation]](
      Decoder[Select].widen,
      Decoder[Where].widen,
      Decoder[GroupBy].widen,
      Decoder[OrderBy].widen,
      Decoder[Join].widen,
      Decoder[Distinct].widen,
      Decoder[Limit].widen,
      Decoder[AddColumns].widen,
      Decoder[DropColumns].widen,
      Decoder[RenameColumns].widen,
      Decoder[CastColumns].widen,
      Decoder[SqlExpr].widen,
      Decoder[UnionT].widen,
      Decoder[IntersectT].widen,
      Decoder[ExceptT].widen,
      Decoder[WindowT].widen,
      Decoder[FlattenT].widen,
      Decoder[SampleT].widen,
      Decoder[RepartitionT].widen,
      Decoder[CoalesceT].widen,
      Decoder[RollupT].widen,
      Decoder[CubeT].widen,
      Decoder[PivotT].widen,
      Decoder[UnpivotT].widen,
      Decoder[ConditionalT].widen,
      Decoder[SplitT].widen,
      Decoder[SCD2T].widen,
      Decoder[EnrichT].widen,
      Decoder[AssertionT].widen,
      Decoder[SchemaEnforceT].widen,
      Decoder[CustomT].widen
    ).reduceLeft(_ or _)

  case class Select(name: String, select: SelectOp)       extends Transformation
  case class Where(name: String, where: WhereOp)          extends Transformation
  case class GroupBy(name: String, group: GroupByOp)      extends Transformation
  case class OrderBy(name: String, order: OrderByOp)      extends Transformation
  case class Join(name: String, join: JoinOp)             extends Transformation
  case class Distinct(name: String, distinct: DistinctOp) extends Transformation
  case class Limit(name: String, limit: LimitOp)          extends Transformation

  case class AddColumns(name: String, addColumns: AddColumnsOp)          extends Transformation
  case class DropColumns(name: String, dropColumns: DropColumnsOp)       extends Transformation
  case class RenameColumns(name: String, renameColumns: RenameColumnsOp) extends Transformation
  case class CastColumns(name: String, castColumns: CastColumnsOp)       extends Transformation
  case class SqlExpr(name: String, sql: SqlOp)                           extends Transformation

  case class UnionT(name: String, union: UnionOp)             extends Transformation
  case class IntersectT(name: String, intersect: IntersectOp) extends Transformation
  case class ExceptT(name: String, except: ExceptOp)          extends Transformation

  case class WindowT(name: String, window: WindowOp) extends Transformation

  case class FlattenT(name: String, flatten: FlattenOp) extends Transformation

  case class SampleT(name: String, sample: SampleOp) extends Transformation

  case class RepartitionT(name: String, repartition: RepartitionOp) extends Transformation

  case class CoalesceT(name: String, coalesce: CoalesceOp) extends Transformation

  case class RollupT(name: String, rollup: RollupOp) extends Transformation

  case class CubeT(name: String, cube: CubeOp) extends Transformation

  case class PivotT(name: String, pivot: PivotOp) extends Transformation

  case class UnpivotT(name: String, unpivot: UnpivotOp) extends Transformation

  case class ConditionalT(name: String, conditional: ConditionalOp) extends Transformation

  case class SplitT(name: String, split: SplitOp) extends Transformation

  case class SCD2T(name: String, scd2: SCD2Op) extends Transformation

  case class EnrichT(name: String, enrich: EnrichOp) extends Transformation

  case class AssertionT(name: String, assertion: AssertionOp) extends Transformation

  case class SchemaEnforceT(name: String, schemaEnforce: SchemaEnforceOp) extends Transformation

  case class CustomT(name: String, custom: CustomOp) extends Transformation

}
