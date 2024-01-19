package com.htmlism.totalize.core.genetic

import cats.effect.*
import org.scalacheck.Gen
import weaver.SimpleIOSuite
import weaver.scalacheck.Checkers

object EvolutionSuite extends SimpleIOSuite with Checkers:
  val genNonZeroSize =
    Gen.choose(1, 100)

  test("Evolution takes a population and builds a new population, sequentially"):
    forall(
      for
        x <- genNonZeroSize
        y <- genNonZeroSize
        z <- genNonZeroSize
      yield (x, y, z)
    ): (solutionLength, firstPopSize, nextPopSize) =>
      for
        rng <- cats.effect.std.Random.scalaUtilRandom

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
        z <- genNonZeroSize
      yield (x, y, z)
    ): (solutionLength, firstPopSize, nextPopSize) =>
      for
        rng <- cats.effect.std.Random.scalaUtilRandom

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
