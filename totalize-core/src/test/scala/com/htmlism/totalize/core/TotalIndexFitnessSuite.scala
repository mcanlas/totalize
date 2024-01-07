package com.htmlism.totalize.core

import weaver.FunSuite

object TotalIndexFitnessSuite extends FunSuite:
  test("Fitness is measurable"):
    val idx =
      TotalIndex(List("a", "b"))

    val prefs =
      PartialOrder
        .empty
        .withPreference(Pair("a", "b"), BinaryPreference.First)

    expect.eql(
      1,
      TotalIndexFitness.score(idx, prefs)
    )
