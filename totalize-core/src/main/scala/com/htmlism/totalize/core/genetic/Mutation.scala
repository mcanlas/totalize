package com.htmlism.totalize.core.genetic

import cats.MonadThrow
import cats.effect.std.Random
import cats.syntax.all.*

trait Mutation[F[_], A]:
  def mutate(x: A): F[A]

object Mutation:
  class ArrayInt[F[_]: MonadThrow](rng: Random[F]) extends Mutation[F, Array[Int]]:
    def mutate(x: Array[Int]): F[Array[Int]] =
      for
        ix <- rng.nextIntBounded(x.length)
        n  <- rng.nextInt
      yield
        val x2 = x.clone()
        x2(ix) = n
        x2
