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

package com.eff3ct.teckel.transform

import cats.Show
import cats.data.NonEmptyList
import com.eff3ct.teckel.model.{Asset, Context, Source}
import com.eff3ct.teckel.serializer.model.etl._
import com.eff3ct.teckel.serializer.model.input._
import com.eff3ct.teckel.serializer.model.operations.Relation
import com.eff3ct.teckel.serializer.model.output._
import com.eff3ct.teckel.serializer.model.transformation._
import com.eff3ct.teckel.serializer.types.PrimitiveType
import com.eff3ct.teckel.serializer.types.implicits._

object Rewrite {

  def rewrite(options: Map[String, PrimitiveType]): Map[String, String] =
    options.map { case (k, v) => k -> Show[PrimitiveType].show(v) }

  def rewrite(item: Input): Asset =
    Asset(item.name, Source.Input(item.format, rewrite(item.options), item.path))

  def rewrite(item: Output): Asset =
    Asset(
      s"output_${item.name}",
      Source.Output(item.name, item.format, item.mode, rewrite(item.options), item.path)
    )

  def rewriteOp(item: Select): Asset =
    Asset(item.name, Source.Select(item.select.from, item.select.columns))

  def rewriteOp(item: Where): Asset =
    Asset(item.name, Source.Where(item.where.from, item.where.filter))

  def rewriteOp(item: GroupBy): Asset =
    Asset(item.name, Source.GroupBy(item.group.from, item.group.by, item.group.agg))

  def rewriteOp(item: OrderBy): Asset =
    Asset(item.name, Source.OrderBy(item.order.from, item.order.by, item.order.order))

  def rewriteOp(item: Join): Asset =
    Asset(item.name, Source.Join(item.join.left, item.join.right.map(rewriteOp)))

  def rewriteOp(item: Distinct): Asset =
    Asset(item.name, Source.Distinct(item.distinct.from, item.distinct.columns))

  def rewriteOp(item: Limit): Asset =
    Asset(item.name, Source.Limit(item.limit.from, item.limit.count))

  def rewriteOp(item: Relation): Source.Relation =
    Source.Relation(item.name, item.relationType, item.on)

  def rewriteOp(item: AddColumns): Asset =
    Asset(
      item.name,
      Source.AddColumns(
        item.addColumns.from,
        item.addColumns.columns.map(c => Source.ColumnDef(c.name, c.expression))
      )
    )

  def rewriteOp(item: DropColumns): Asset =
    Asset(item.name, Source.DropColumns(item.dropColumns.from, item.dropColumns.columns))

  def rewriteOp(item: RenameColumns): Asset =
    Asset(item.name, Source.RenameColumns(item.renameColumns.from, item.renameColumns.mappings))

  def rewriteOp(item: CastColumns): Asset =
    Asset(
      item.name,
      Source.CastColumns(
        item.castColumns.from,
        item.castColumns.columns.map(c => Source.CastColumn(c.name, c.targetType))
      )
    )

  def rewriteOp(item: SqlExpr): Asset =
    Asset(item.name, Source.Sql(item.sql.from, item.sql.query))
  def rewriteOp(item: UnionT): Asset = {
    val head = item.union.sources.head
    val tail = item.union.sources.tail
    Asset(
      item.name,
      Source.Union(head, NonEmptyList.fromListUnsafe(tail), item.union.all.getOrElse(false))
    )
  }

  def rewriteOp(item: IntersectT): Asset = {
    val head = item.intersect.sources.head
    val tail = item.intersect.sources.tail
    Asset(
      item.name,
      Source
        .Intersect(head, NonEmptyList.fromListUnsafe(tail), item.intersect.all.getOrElse(false))
    )
  }

  def rewriteOp(item: ExceptT): Asset =
    Asset(
      item.name,
      Source.Except(item.except.left, item.except.right, item.except.all.getOrElse(false))
    )

  def rewriteOp(item: FlattenT): Asset =
    Asset(
      item.name,
      Source.Flatten(item.flatten.from, item.flatten.separator, item.flatten.explodeArrays)
    )

  def rewriteOp(item: SampleT): Asset =
    Asset(
      item.name,
      Source.Sample(
        item.sample.from,
        item.sample.fraction,
        item.sample.withReplacement,
        item.sample.seed
      )
    )

  def rewriteOp(item: RepartitionT): Asset =
    Asset(
      item.name,
      Source.Repartition(
        item.repartition.from,
        item.repartition.numPartitions,
        item.repartition.columns
      )
    )

  def rewriteOp(item: CoalesceT): Asset =
    Asset(item.name, Source.Coalesce(item.coalesce.from, item.coalesce.numPartitions))

  def rewriteOp(item: RollupT): Asset =
    Asset(item.name, Source.Rollup(item.rollup.from, item.rollup.by, item.rollup.agg))

  def rewriteOp(item: CubeT): Asset =
    Asset(item.name, Source.Cube(item.cube.from, item.cube.by, item.cube.agg))

  def rewriteOp(item: PivotT): Asset =
    Asset(
      item.name,
      Source.Pivot(
        item.pivot.from,
        item.pivot.groupBy,
        item.pivot.pivotColumn,
        item.pivot.values,
        item.pivot.agg
      )
    )

  def rewriteOp(item: UnpivotT): Asset =
    Asset(
      item.name,
      Source.Unpivot(
        item.unpivot.from,
        item.unpivot.ids,
        item.unpivot.values,
        item.unpivot.variableColumn,
        item.unpivot.valueColumn
      )
    )

  def rewriteOp(item: WindowT): Asset =
    Asset(
      item.name,
      Source.Window(
        item.window.from,
        item.window.partitionBy,
        item.window.orderBy,
        item.window.functions.map(f => Source.WindowFunc(f.expression, f.alias))
      )
    )

  def rewriteOp(item: ConditionalT): Asset =
    Asset(
      item.name,
      Source.Conditional(
        item.conditional.from,
        item.conditional.outputColumn,
        item.conditional.branches.map(b => Source.WhenBranch(b.condition, b.value)),
        item.conditional.otherwise
      )
    )

  def rewriteOp(item: SCD2T): Asset =
    Asset(
      item.name,
      Source.SCD2(
        item.scd2.from,
        item.scd2.keyColumns,
        item.scd2.trackColumns,
        item.scd2.startDateColumn,
        item.scd2.endDateColumn,
        item.scd2.currentFlagColumn
      )
    )

  def rewriteOp(item: EnrichT): Asset =
    Asset(
      item.name,
      Source.Enrich(
        item.enrich.from,
        item.enrich.url,
        item.enrich.method,
        item.enrich.keyColumn,
        item.enrich.responseColumn,
        item.enrich.headers
      )
    )

  def rewrite(item: Transformation): Asset =
    item match {
      case s: Select        => rewriteOp(s)
      case s: Where         => rewriteOp(s)
      case s: GroupBy       => rewriteOp(s)
      case s: OrderBy       => rewriteOp(s)
      case s: Join          => rewriteOp(s)
      case s: Distinct      => rewriteOp(s)
      case s: Limit         => rewriteOp(s)
      case s: AddColumns    => rewriteOp(s)
      case s: DropColumns   => rewriteOp(s)
      case s: RenameColumns => rewriteOp(s)
      case s: CastColumns   => rewriteOp(s)
      case s: SqlExpr       => rewriteOp(s)
      case s: UnionT        => rewriteOp(s)
      case s: IntersectT    => rewriteOp(s)
      case s: ExceptT       => rewriteOp(s)
      case s: WindowT       => rewriteOp(s)
      case s: FlattenT      => rewriteOp(s)
      case s: SampleT       => rewriteOp(s)
      case s: RepartitionT  => rewriteOp(s)
      case s: CoalesceT     => rewriteOp(s)
      case s: RollupT       => rewriteOp(s)
      case s: CubeT         => rewriteOp(s)
      case s: PivotT        => rewriteOp(s)
      case s: UnpivotT      => rewriteOp(s)
      case s: ConditionalT  => rewriteOp(s)
      case s: SCD2T         => rewriteOp(s)
      case s: EnrichT       => rewriteOp(s)
      case _: SplitT        => throw new IllegalStateException("SplitT is expanded in tcontext")
    }

  def icontext(item: NonEmptyList[Input]): Context[Asset] =
    item
      .map { i =>
        val asset: Asset = rewrite(i)
        asset.assetRef -> asset
      }
      .toList
      .toMap

  def ocontext(item: NonEmptyList[Output]): Context[Asset] =
    item
      .map { o =>
        val asset: Asset = rewrite(o)
        asset.assetRef -> asset
      }
      .toList
      .toMap

  def tcontext(item: Option[NonEmptyList[Transformation]]): Context[Asset] =
    (for {
      transformation <- item
      context = transformation.toList.flatMap {
        case s: SplitT =>
          val passAsset = Asset(s.split.pass, Source.Where(s.split.from, s.split.condition))
          val failAsset =
            Asset(s.split.fail, Source.Where(s.split.from, s"NOT (${s.split.condition})"))
          List(passAsset.assetRef -> passAsset, failAsset.assetRef -> failAsset)
        case t =>
          val asset: Asset = rewrite(t)
          List(asset.assetRef -> asset)
      }
    } yield context.toMap).getOrElse(Map())

  def rewrite(item: ETL): Context[Asset] =
    icontext(item.input) ++ ocontext(item.output) ++ tcontext(item.transformation)

}
