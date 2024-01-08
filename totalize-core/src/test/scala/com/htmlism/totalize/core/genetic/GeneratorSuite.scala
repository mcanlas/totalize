package com.htmlism.totalize.core.genetic

import cats.effect.*
import weaver.SimpleIOSuite
import weaver.scalacheck.Checkers

object GeneratorSuite extends SimpleIOSuite with Checkers:
  test("A generator can generate a collection of N"):
    for
      rng <- std.Random.scalaUtilRandom[IO]
      xs  <- Generator.sync[IO](rng).generate(3)
    yield expect.eql(
      3,
      xs.length
    )
