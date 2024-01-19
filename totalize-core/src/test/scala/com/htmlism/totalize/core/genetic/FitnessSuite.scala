package com.htmlism.totalize.core
package genetic

import weaver.FunSuite

object FitnessSuite extends FunSuite:
  test("Fitness measures the fitness of a candidate solution"):
    val candidateOrder =
      List("alpha", "beta", "gamma")

    val prefs =
      PartialOrder
        .empty
        .withPreference(Pair("alpha", "beta"), BinaryPreference.First)
        .withPreference(Pair("alpha", "gamma"), BinaryPreference.First)

    val fitnessFunc =
      Fitness.ArrayInt(candidateOrder, prefs)

    val encodedCandidate =
      Array(-1, 0, 300)

    expect.eql(
      fitnessFunc.fitness(encodedCandidate),
      2
    )
