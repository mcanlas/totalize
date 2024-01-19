package com.htmlism.totalize.core.genetic

import cats.effect.*
import org.scalacheck.Gen
import weaver.SimpleIOSuite
import weaver.scalacheck.Checkers

object MutationSuite extends SimpleIOSuite with Checkers:
  val genNonZeroSize =
    Gen.choose(1, 100)

  test("Mutation takes one solution and returns a new one"):
    forall(genNonZeroSize): n =>
      for
        rng <- std.Random.scalaUtilRandom[IO]
        x   <- SolutionGenerator.sequence[IO](rng).generate(n)
        x2  <- Mutation.ArrayInt[IO](rng).mutate(x)
      yield expect.eql(x.length, x2.length)
