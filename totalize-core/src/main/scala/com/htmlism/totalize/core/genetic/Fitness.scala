package com.htmlism.totalize.core
package genetic

import cats.Order

trait Fitness[A, B](using B: Ordering[B]):
  def fitness(x: A): B

  def pick(x: A, y: A): A =
    val fx = fitness(x)
    val fy = fitness(y)

    if B.gteq(fx, fy) then x
    else y

object Fitness:
  class ArrayInt(keys: List[String], prefs: PartialOrder[String]) extends Fitness[Array[Int], Int]:
    def fitness(ranks: Array[Int]): Int =
      assert(keys.length == ranks.length)

      val keysWithRanks =
        keys
          .zip(ranks)
          .toMap

      val order =
        Order.by(keysWithRanks)

      prefs
        .xs
        .map:
          case (Pair(x, y), PartialOrder.Edge(_, pref)) =>
            if keysWithRanks.contains(x) && keysWithRanks.contains(y) then
              if order.lt(x, y) && pref == BinaryPreference.First then 1
              else if order.lt(y, x) && pref == BinaryPreference.Second then 1
              else 0
            else 0
        .sum
