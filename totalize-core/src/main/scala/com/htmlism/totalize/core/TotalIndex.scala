package com.htmlism.totalize.core

import cats.*

/**
  * This index is constructed from an input population of `A`.
  *
  * But it is still possible that queries to its order may not have been in the input population during construction.
  */
case class TotalIndex[A: Eq](xs: List[A]):
  private lazy val _map =
    xs.iterator.zipWithIndex.toMap

  private lazy val _vector =
    xs.toVector

  // where do unexpected elements get sorted?
  def getIndex(x: A): Int =
    _map(x)

  def getElement(i: Int): A =
    _vector(i)

  /**
    * Constructs an `Order` from this index
    */
  def toOrder: Order[A] =
    Order.by(getIndex)
