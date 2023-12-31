package com.htmlism.totalize.core

import cats.*
import cats.syntax.all.*

/**
  * An unordered pair of identifiers
  *
  * @param x
  *   The "lesser" of the pair, e.g. the A of { A, B }
  * @param y
  *   The "greater" of the pair, e.g. the B of { A, B }
  *
  * @tparam A
  *   The type of the set the pair belongs to
  */
case class Pair[A](x: A, y: A)(using A: Order[A]):
  assert(A.lt(x, y), s"${x.toString} must be less than ${y.toString}")

object Pair:
  def from[A](x: A, y: A)(using A: Order[A]): Either[IllegalArgumentException, Pair[A]] =
    if A.lt(x, y) then Pair(x, y).asRight
    else if A.lt(y, x) then Pair(y, x).asRight
    else new IllegalArgumentException("Arguments must not be equal to form a pair").asLeft

  given [A: Order]: Order[Pair[A]] =
    Order.by(p => p.x -> p.y)
