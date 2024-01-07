package com.htmlism.totalize.core

import weaver.FunSuite

object TotalIndexSuite extends FunSuite:
  test("A total index can give an ordering"):
    val order =
      TotalIndex(List("a", "b")).toOrder

    expect(
      order.lt("a", "b")
    )
