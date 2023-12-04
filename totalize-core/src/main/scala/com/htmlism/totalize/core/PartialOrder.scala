package com.htmlism.totalize.core

import cats.Monoid

/**
  * A collection of binary preferences, sparsely defined for all pairs in a set
  *
  * If a pair exists, it definitely has a preference for one or the other
  *
  * If a pair does not exist, the no statement can be made about their preference
  *
  * @param xs
  * @tparam A
  */
case class PartialOrder[A](xs: Map[Pair[A], PartialOrder.Edge[A]]):
  def withPreference(pair: Pair[A], pref: BinaryPreference): PartialOrder[A] =
    PartialOrder(xs.updated(pair, PartialOrder.Edge(pair, pref)))

object PartialOrder:
  case class Edge[A](pair: Pair[A], pref: BinaryPreference)

  given [A]: Monoid[PartialOrder[A]] =
    new Monoid[PartialOrder[A]]:
      def empty: PartialOrder[A] =
        PartialOrder(Map.empty)

      def combine(x: PartialOrder[A], y: PartialOrder[A]): PartialOrder[A] =
        PartialOrder(x.xs ++ y.xs)

  def empty[A]: PartialOrder[A] =
    Monoid[PartialOrder[A]].empty
