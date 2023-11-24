package com.htmlism.totalize.core

import cats.syntax.all.*
import weaver.FunSuite

object PairSuite extends FunSuite:
  test("Constructing a pair succeeds if the arguments are different"):
    whenSuccess(Pair.from(1, 2)): xs =>
      expect.same(Pair(1, 2), xs)

  test("Constructing a pair fails if the arguments are the same")
  matches(Pair.from(1, 1)):
    case Left(_) =>
      success

  test("Pairs are equivalent regardless of their safe construction order"):
    whenSuccess(
      (Pair.from(1, 2), Pair.from(2, 1)).tupled
    ):
      case (a, b) =>
        expect(a === b)
