package com.htmlism.totalize.core.genetic

import cats.*
import cats.syntax.all.*

trait PopulationGenerator[F[_]]:
  def generate[A](n: Int, fa: F[A]): F[Population[A]]

object PopulationGenerator:
  def sequence[F[_]: Applicative]: PopulationGenerator[F] =
    new PopulationGenerator[F]:
      def generate[A](n: Int, fa: F[A]): F[Population[A]] =
        Vector
          .fill(n)(fa)
          .sequence
          .map(Population(_))

  def parallel[F[_]: Applicative: Parallel]: PopulationGenerator[F] =
    new PopulationGenerator[F]:
      def generate[A](n: Int, fa: F[A]): F[Population[A]] =
        Vector
          .fill(n)(fa)
          .parSequence
          .map(Population(_))
