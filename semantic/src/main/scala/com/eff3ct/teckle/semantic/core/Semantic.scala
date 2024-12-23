package com.eff3ct.teckle.semantic.core

trait Semantic[-S, -I, +O] {
  def eval(input: I, source: S): O
}

object Semantic {

  def apply[S, I, O](implicit S: Semantic[S, I, O]): Semantic[S, I, O] = S

  def pure[S, I, O](f: S => O): Semantic[S, I, O] = (_: I, source: S) => f(source)

  def any[S, O](f: S)(implicit S: Semantic[S, Any, O]): O = S.eval((), f)
}
