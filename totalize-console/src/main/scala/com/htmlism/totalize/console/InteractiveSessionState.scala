package com.htmlism.totalize.console

import cats.*
import cats.effect.*
import cats.syntax.all.*

trait InteractiveSessionState[F[_]]

object InteractiveSessionState:
  class SyncInteractiveSessionState[F[_]: FlatMap](rng: std.Random[F], seed: Ref[F, Int])
      extends InteractiveSessionState[F]:
    def updateSeed: F[Unit] =
      rng.nextInt >>= seed.set

  def sync[F[_]: Sync]: F[InteractiveSessionState[F]] =
    for
      rng  <- std.Random.scalaUtilRandom[F]
      refN <- Ref[F].of(0)

      state = SyncInteractiveSessionState(rng, refN)

      _ <- state.updateSeed
    yield state
