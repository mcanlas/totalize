package com.htmlism.totalize.console

import cats.*
import cats.effect.*
import cats.syntax.all.*

import com.htmlism.totalize.core.*

trait InteractiveSessionState[F[_]]

object InteractiveSessionState:
  class SyncInteractiveSessionState[F[_]: FlatMap, A](
      rng: std.Random[F],
      seed: Ref[F, Int],
      val xs: PreferenceRelation[A]
  )(using out: std.Console[F])
      extends InteractiveSessionState[F]:
    def updateSeed: F[Unit] =
      rng.nextInt >>= seed.set

    def printCurrentPair: F[Unit] =
      out.println("")

  def sync[F[_]: Sync: std.Console, A]: F[InteractiveSessionState[F]] =
    for
      rng  <- std.Random.scalaUtilRandom[F]
      refN <- Ref[F].of(0)

      state = SyncInteractiveSessionState(rng, refN, PreferenceRelation.empty[A])

      _ <- state.updateSeed
    yield state
