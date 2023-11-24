package com.htmlism.totalize.core

object TotalIndexFitness:
  def score[A](index: TotalIndex[A], prefs: PreferenceRelation[A]): Int =
    val order =
      index.toOrder

    prefs
      .xs
      .map:
        case (Pair(x, y), pref) =>
          if order.lt(x, y) && pref == BinaryPreference.First then 1
          else if order.lt(y, x) && pref == BinaryPreference.Second then 1
          else 0
      .sum
