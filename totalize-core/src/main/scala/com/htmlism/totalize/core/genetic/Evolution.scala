package com.htmlism.totalize.core.genetic

import cats.*
import cats.effect.std.Random
import cats.syntax.all.*

object Evolution:
  private def sample[F[_]: MonadThrow, A](xs: Population[A], rng: Random[F])(using
      F: Fitness[A, ?],
      C: Crossover[F, A],
      M: Mutation[F, A]
  ): F[A] =
    for
      a <- rng.elementOf(xs)
      b <- rng.elementOf(xs)
      c <- rng.elementOf(xs)
      d <- rng.elementOf(xs)

      w1 = F.pick(a, b)
      w2 = F.pick(c, d)

      ww  <- C.combine(w1, w2)
      ww2 <- M.mutate(ww)
    yield ww2

  def evolveSequence[F[_]: MonadThrow, A](xs: Population[A], size: Int, rng: Random[F])(using
      Fitness[A, ?],
      Crossover[F, A],
      Mutation[F, A]
  ): F[Population[A]] =
    Vector
      .fill(size)(sample(xs, rng))
      .sequence
      .map(Population(_))

  def evolveParallel[F[_]: MonadThrow: Parallel, A](xs: Population[A], size: Int, rng: Random[F])(using
      Fitness[A, ?],
      Crossover[F, A],
      Mutation[F, A]
  ): F[Population[A]] =
    Vector
      .fill(size)(sample(xs, rng))
      .parSequence
      .map(Population(_))
