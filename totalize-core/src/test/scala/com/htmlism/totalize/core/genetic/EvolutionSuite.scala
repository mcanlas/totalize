package com.htmlism.totalize.core
package genetic

import cats.effect.*
import org.scalacheck.Gen
import weaver.SimpleIOSuite
import weaver.scalacheck.Checkers

object EvolutionSuite extends SimpleIOSuite with Checkers:
  val genNonZeroSize =
    Gen.choose(1, 100)

  val keys =
    List("alpha", "beta", "gamma")

  val solutionLength =
    keys.length

  val prefs =
    PartialOrder
      .empty
      .withPreference(Pair("alpha", "beta"), BinaryPreference.First)
      .withPreference(Pair("alpha", "gamma"), BinaryPreference.First)

  test("Evolution takes a population and builds a new population, sequentially"):
    forall(
      for
        x <- genNonZeroSize
        y <- genNonZeroSize
      yield (x, y)
    ): (firstPopSize, nextPopSize) =>
      for
        rng <- cats.effect.std.Random.scalaUtilRandom

        given Fitness[Array[Int], Int] =
          Fitness.ArrayInt(keys, prefs)

        given Crossover[IO, Array[Int]] =
          Crossover.Blend[IO](rng)

        given Mutation[IO, Array[Int]] =
          Mutation.ArrayInt[IO](rng)

        solGen = SolutionGenerator.sequence[IO](rng).generate(solutionLength)

        firstPop <- PopulationGenerator.sequence[IO].generate(firstPopSize, solGen)
        nextPop  <- Evolution.evolveSequence(firstPop, nextPopSize, rng)
      yield expect.eql(
        nextPopSize,
        nextPop.length
      )

  test("Evolution takes a population and builds a new population, in parallel"):
    forall(
      for
        x <- genNonZeroSize
        y <- genNonZeroSize
      yield (x, y)
    ): (firstPopSize, nextPopSize) =>
      for
        rng <- cats.effect.std.Random.scalaUtilRandom

        given Fitness[Array[Int], Int] =
          Fitness.ArrayInt(keys, prefs)

        given Crossover[IO, Array[Int]] =
          Crossover.Blend[IO](rng)

        given Mutation[IO, Array[Int]] =
          Mutation.ArrayInt[IO](rng)

        solGen = SolutionGenerator.sequence[IO](rng).generate(solutionLength)

        firstPop <- PopulationGenerator.sequence[IO].generate(firstPopSize, solGen)
        nextPop  <- Evolution.evolveParallel(firstPop, nextPopSize, rng)
      yield expect.eql(
        nextPopSize,
        nextPop.length
      )
