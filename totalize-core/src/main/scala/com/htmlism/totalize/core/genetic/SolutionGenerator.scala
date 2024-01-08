package com.htmlism.totalize.core.genetic

import cats.*
import cats.effect.std.Random
import cats.syntax.all.*

trait SolutionGenerator[F[_]]:
  def generate(n: Int): F[Array[Int]]

object SolutionGenerator:
  def sync[F[_]: Applicative](rng: Random[F]): SolutionGenerator[F] =
    new SolutionGenerator[F]:
      def generate(n: Int): F[Array[Int]] =
        Vector
          .fill(n)(rng.nextInt)
          .sequence
          .map(_.toArray)

  def parallel[F[_]: Applicative: Parallel](rng: Random[F]): SolutionGenerator[F] =
    new SolutionGenerator[F]:
      def generate(n: Int): F[Array[Int]] =
        Vector
          .fill(n)(rng.nextInt)
          .parSequence
          .map(_.toArray)
