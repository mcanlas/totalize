package com.htmlism.totalize.core.genetic

//import cats.FlatMap
//import cats.effect.std.Random

trait Generator[F[_]]:
  def generate(n: Int): F[Array[Short]]
