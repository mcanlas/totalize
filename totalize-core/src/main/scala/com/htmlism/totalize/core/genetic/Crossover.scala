package com.htmlism.totalize.core.genetic

import cats.*
import cats.effect.std.Random
import cats.syntax.all.*

trait Crossover[F[_], A]:
  def combine(x: A, y: A): F[A]

object Crossover:
  class Blend[F[_]: MonadThrow](rng: Random[F]) extends Crossover[F, Array[Int]]:
    def combine(x: Array[Int], y: Array[Int]): F[Array[Int]] =
      assert(x.length == y.length)

      x
        .iterator
        .zipWithIndex
        .map: (e, i) =>
          for doX <- rng.nextBoolean
          yield if doX then e else y(i)
        .toVector
        .sequence
        .map(_.toArray)
