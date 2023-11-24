package com.htmlism.totalize.core

import cats.*

case class TotalIndex[A: Eq](xs: List[A]):
  private lazy val _map =
    xs.iterator.zipWithIndex.toMap

  private lazy val _vector =
    xs.toVector

  def byElement(x: A): Int =
    _map(x)

  def byIndex(i: Int): A =
    _vector(i)

  /**
    * Constructs an `Order` from this index
    */
  def toOrder: Order[A] =
    Order.by(byElement)
