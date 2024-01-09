package com.htmlism.totalize.core.genetic

import cats.Show
import org.scalacheck.*
import weaver.SimpleIOSuite
import weaver.scalacheck.Checkers

object SelectionSuite extends SimpleIOSuite with Checkers:
  val genPop: Gen[Population[Array[Int]]] =
    for
      n <- Gen.chooseNum(0, 1000)

      solutionGen =
        Gen.containerOfN[Array, Int](n, Arbitrary.arbInt.arbitrary)

      xs <- Gen.containerOfN[Vector, Array[Int]](n, solutionGen)
    yield Population(xs)

  given Show[Population[Array[Int]]] =
    Show.fromToString

  // TODO
  test("Randomly select two from a population"):
    forall(genPop): pop =>
      expect.eql(pop.size, pop.size)
