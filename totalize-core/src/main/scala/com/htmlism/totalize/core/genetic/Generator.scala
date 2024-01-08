package com.htmlism.totalize.core.genetic

import cats.*
import cats.effect.std.Random
import cats.syntax.all.*

trait Generator[F[_]]:
  def generate(n: Int): F[Array[Int]]

object Generator:
  def sync[F[_]: Monad](rng: Random[F]): Generator[F] =
    new Generator[F]:
      def generate(n: Int): F[Array[Int]] =
        Vector
          .fill(n)(rng.nextInt)
          .sequence
          .map(_.toArray)
