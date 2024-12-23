package com.eff3ct.teckle.semantic

import org.apache.spark.sql.DataFrame

package object sources {

  def debug[S: Debug]: (DataFrame, S) => DataFrame = Debug[S].debug

  def exec[S: Exec]: (DataFrame, S) => Unit = Exec[S].eval
}
