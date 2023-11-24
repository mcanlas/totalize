package com.htmlism.totalize.console

import cats.*
import cats.effect.*
import cats.syntax.all.*

import com.htmlism.totalize.core.*

trait InteractiveSessionState[F[_]]:
  def printCurrentPair: F[Unit]

  def preferFirst: F[Unit]

  def preferSecond: F[Unit]

  def dump: F[Unit]

object InteractiveSessionState:
  class SyncInteractiveSessionState[F[_]: Sync, A: Order](
      population: List[A],
      rng: std.Random[F],
      seedRef: Ref[F, Int],
      prefRef: Ref[F, PreferenceRelation[A]]
  )(using out: std.Console[F])
      extends InteractiveSessionState[F]:
    assert(population.size > 1, "Population must be at least 2")

    def updateSeed: F[Unit] =
      rng.nextInt >>= seedRef.set

    def getCurrentPair: F[Pair[A]] =
      for
        shuffler <- seedRef
          .get
          .flatMap(std.Random.scalaUtilRandomSeedInt[F])

        x <- shuffler.elementOf(population)
        y <- shuffler.elementOf(population diff List(x))

        pair <- Pair.from(x, y).liftTo[F]
      yield pair

    def preferFirst: F[Unit] =
      for
        pair <- getCurrentPair
        _    <- prefRef.update(_.withPreference(pair, BinaryPreference.First))
        _    <- updateSeed
        _    <- printCurrentPair
      yield ()

    def preferSecond: F[Unit] =
      for
        pair <- getCurrentPair
        _    <- prefRef.update(_.withPreference(pair, BinaryPreference.Second))
        _    <- updateSeed
        _    <- printCurrentPair
      yield ()

    def printCurrentPair: F[Unit] =
      for
        pair <- getCurrentPair

        _ <- out.println(pair.toString)
      yield ()

    def dump: F[Unit] =
      for
        prefs <- prefRef.get
        _ <- prefs
          .xs
          .toList
          .map(_.toString)
          .traverse(out.println)
      yield ()

  def sync[F[_]: Sync: std.Console, A: Order](population: List[A]): F[InteractiveSessionState[F]] =
    for
      rng      <- std.Random.scalaUtilRandom[F]
      refN     <- Ref[F].of(0)
      refPrefs <- Ref[F].of(PreferenceRelation.empty[A])

      state = SyncInteractiveSessionState(population, rng, refN, refPrefs)

      _ <- state.updateSeed
    yield state
