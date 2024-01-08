package com.htmlism.totalize.core.genetic

import cats.effect.*
import org.scalacheck.Gen
import weaver.SimpleIOSuite
import weaver.scalacheck.Checkers

object SolutionGeneratorSuite extends SimpleIOSuite with Checkers:
  val genSize =
    Gen.choose(0, 100)

  test("A generator can generate a collection of N, in sequence"):
    forall(genSize): n =>
      for
        rng <- std.Random.scalaUtilRandom[IO]
        xs  <- SolutionGenerator.sync[IO](rng).generate(n)
      yield expect.eql(
        n,
        xs.length
      )

  test("A generator can generate a collection of N, in parallel"):
    forall(genSize): n =>
      for
        rng <- std.Random.scalaUtilRandom[IO]
        xs  <- SolutionGenerator.parallel[IO](rng).generate(n)
      yield expect.eql(
        n,
        xs.length
      )
