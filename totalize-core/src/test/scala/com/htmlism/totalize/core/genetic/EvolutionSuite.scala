package com.htmlism.totalize.core.genetic

import cats.effect.*
import org.scalacheck.Gen
import weaver.SimpleIOSuite
import weaver.scalacheck.Checkers

object EvolutionSuite extends SimpleIOSuite with Checkers:
  val genNonZeroSize =
    Gen.choose(1, 100)

  val fa =
    IO.unit

  test("Evolution takes a population and builds a new population, sequentially"):
    forall(
      for
        x <- genNonZeroSize
        y <- genNonZeroSize
      yield (x, y)
    ): (firstPopSize, nextPopSize) =>
      for
        rng      <- cats.effect.std.Random.scalaUtilRandom
        firstPop <- PopulationGenerator.sequence[IO].generate(firstPopSize, fa)
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
        rng      <- cats.effect.std.Random.scalaUtilRandom
        firstPop <- PopulationGenerator.sequence[IO].generate(firstPopSize, fa)
        nextPop  <- Evolution.evolveParallel(firstPop, nextPopSize, rng)
      yield expect.eql(
        nextPopSize,
        nextPop.length
      )
