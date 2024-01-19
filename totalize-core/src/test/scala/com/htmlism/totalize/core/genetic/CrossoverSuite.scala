package com.htmlism.totalize.core.genetic

import cats.effect.*
import org.scalacheck.Gen
import weaver.SimpleIOSuite
import weaver.scalacheck.Checkers

object CrossoverSuite extends SimpleIOSuite with Checkers:
  val genSize =
    Gen.choose(0, 100)

  test("Crossover takes two solutions and returns a new one"):
    forall(genSize): n =>
      for
        rng <- std.Random.scalaUtilRandom[IO]
        x   <- SolutionGenerator.sequence[IO](rng).generate(n)
        y   <- SolutionGenerator.sequence[IO](rng).generate(n)
        xy  <- Crossover.Blend[IO](rng).combine(x, y)
      yield expect.eql(x.length, xy.length)
