package com.htmlism.totalize.core.genetic

import cats.*
import cats.syntax.all.*

trait PopulationGenerator[F[_]]:
  def generate[A](n: Int, fa: F[A]): F[Vector[A]]

object PopulationGenerator:
  def sync[F[_]: Applicative]: PopulationGenerator[F] =
    new PopulationGenerator[F]:
      def generate[A](n: Int, fa: F[A]): F[Vector[A]] =
        Vector
          .fill(n)(fa)
          .sequence

  def parallel[F[_]: Applicative: Parallel]: PopulationGenerator[F] =
    new PopulationGenerator[F]:
      def generate[A](n: Int, fa: F[A]): F[Vector[A]] =
        Vector
          .fill(n)(fa)
          .parSequence
