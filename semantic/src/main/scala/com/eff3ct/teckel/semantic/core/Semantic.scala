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

package com.eff3ct.teckel.semantic.core

trait Semantic[S, -I, +O] {
  def eval(input: I, source: S): O
}

object Semantic {

  def apply[S, I, O](implicit S: Semantic[S, I, O]): Semantic[S, I, O] = S

  def zero[S, I, O](f: S => O): Semantic[S, I, O] = (_: I, source: S) => f(source)

  def map[S, I, O](f: I => O): Semantic[S, I, O] = Semantic.pure((input: I, _) => f(input))

  def pure[S, I, O](f: (I, S) => O): Semantic[S, I, O] = (input: I, source: S) => f(input, source)

  def any[S, O](f: S)(implicit S: Semantic[S, Any, O]): O = S.eval((), f)
}
