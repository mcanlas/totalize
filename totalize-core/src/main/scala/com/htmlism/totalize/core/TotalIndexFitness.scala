package com.htmlism.totalize.core

object TotalIndexFitness:
  def score[A](index: TotalIndex[A], prefs: PartialOrder[A]): Int =
    val order =
      index.toOrder

    // TODO the constructed orderings are actually partial and fallible, a la partial function
    prefs
      .xs
      .map:
        case (Pair(x, y), pref) =>
          if util.Try(order.lt(x, y)).getOrElse(false) && pref == BinaryPreference.First then 1
          else if util.Try(order.lt(y, x)).getOrElse(false) && pref == BinaryPreference.Second then 1
          else 0
      .sum
