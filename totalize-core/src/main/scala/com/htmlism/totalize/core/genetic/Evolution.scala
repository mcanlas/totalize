package com.htmlism.totalize.core.genetic

import cats.*
import cats.effect.std.Random
import cats.syntax.all.*

object Evolution:
  private def sample[F[_]: MonadThrow, A](xs: Population[A], rng: Random[F])(using
      C: Crossover[F, A],
      M: Mutation[F, A]
  ): F[A] =
    for
      x   <- rng.elementOf(xs)
      y   <- rng.elementOf(xs)
      xy  <- C.combine(x, y)
      xy2 <- M.mutate(xy)
    yield xy2

  def evolveSequence[F[_]: MonadThrow, A](xs: Population[A], size: Int, rng: Random[F])(using
      Crossover[F, A],
      Mutation[F, A]
  ): F[Population[A]] =
    Vector
      .fill(size)(sample(xs, rng))
      .sequence
      .map(Population(_))

  def evolveParallel[F[_]: MonadThrow: Parallel, A](xs: Population[A], size: Int, rng: Random[F])(using
      Crossover[F, A],
      Mutation[F, A]
  ): F[Population[A]] =
    Vector
      .fill(size)(sample(xs, rng))
      .parSequence
      .map(Population(_))
