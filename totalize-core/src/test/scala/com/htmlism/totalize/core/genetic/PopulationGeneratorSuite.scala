package com.htmlism.totalize.core.genetic

import cats.effect.*
import org.scalacheck.Gen
import weaver.SimpleIOSuite
import weaver.scalacheck.Checkers

object PopulationGeneratorSuite extends SimpleIOSuite with Checkers:
  val genSize =
    Gen.choose(0, 100)

  val fa =
    IO.unit

  test("A population generator can generate a collection of N, in sequence"):
    forall(genSize): n =>
      for xs <- PopulationGenerator.sync[IO].generate(n, fa)
      yield expect.eql(
        n,
        xs.length
      )

  test("A population generator can generate a collection of N, in parallel"):
    forall(genSize): n =>
      for xs <- PopulationGenerator.parallel[IO].generate(n, fa)
      yield expect.eql(
        n,
        xs.length
      )
