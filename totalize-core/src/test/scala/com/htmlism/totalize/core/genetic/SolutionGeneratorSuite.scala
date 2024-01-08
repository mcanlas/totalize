package com.htmlism.totalize.core.genetic

import cats.effect.*
import weaver.SimpleIOSuite
import weaver.scalacheck.Checkers

object SolutionGeneratorSuite extends SimpleIOSuite with Checkers:
  test("A generator can generate a collection of N, in sequence"):
    for
      rng <- std.Random.scalaUtilRandom[IO]
      xs  <- SolutionGenerator.sync[IO](rng).generate(3)
    yield expect.eql(
      3,
      xs.length
    )

  test("A generator can generate a collection of N, in parallel"):
    for
      rng <- std.Random.scalaUtilRandom[IO]
      xs  <- SolutionGenerator.parallel[IO](rng).generate(3)
    yield expect.eql(
      3,
      xs.length
    )
