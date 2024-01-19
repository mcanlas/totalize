package com.htmlism.totalize.core.genetic

import cats.*
import cats.effect.std.Random
import cats.syntax.all.*

object Evolution:
  private def sample[F[_]: MonadThrow, A](xs: Population[A], rng: Random[F]): F[A] =
    for
      x <- rng.elementOf(xs)
      _ <- rng.elementOf(xs)
    yield x

  def evolveSequence[F[_]: MonadThrow, A](xs: Population[A], size: Int, rng: Random[F]): F[Population[A]] =
    Vector
      .fill(size)(sample(xs, rng))
      .sequence
      .map(Population(_))

  def evolveParallel[F[_]: MonadThrow: Parallel, A](xs: Population[A], size: Int, rng: Random[F]): F[Population[A]] =
    Vector
      .fill(size)(sample(xs, rng))
      .parSequence
      .map(Population(_))
