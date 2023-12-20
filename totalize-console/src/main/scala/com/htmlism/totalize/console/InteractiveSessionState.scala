package com.htmlism.totalize.console

import cats.effect.*
import cats.syntax.all.*
import cats.{PartialOrder as _, *}
import io.circe.*
import io.circe.syntax.*

import com.htmlism.totalize.core.*
import com.htmlism.totalize.storage
import com.htmlism.totalize.storage.FileIO
import com.htmlism.totalize.storage.HistoricalEntry
import com.htmlism.totalize.storage.YamlTableService

trait InteractiveSessionState[F[_]]:
  def printCurrentPair: F[Unit]

  def preferFirst: F[Unit]

  def preferSecond: F[Unit]

  def dump: F[Unit]

  def writePuml: F[Unit]

object InteractiveSessionState:
  given Decoder[BinaryPreference] =
    Decoder[Int].emap:
      case -1 =>
        BinaryPreference.First.asRight
      case 1 =>
        BinaryPreference.Second.asRight
      case n =>
        s"number $n was not valid for comparison contract".asLeft

  given Encoder[BinaryPreference] =
    Encoder[Int].contramap:
      case BinaryPreference.First  => -1
      case BinaryPreference.Second => 1

  given [A](using Encoder[A]): Encoder[HistoricalEntry[PartialOrder.Edge[A]]] with
    def apply(x: HistoricalEntry[PartialOrder.Edge[A]]): Json =
      JsonObject(
        "pair"       -> List(x.x.pair.x, x.x.pair.y).asJson,
        "preference" -> x.x.pref.asJson,
        "createdAt"  -> 0L.asJson
      ).asJson

  given [A: Order](using Decoder[List[A]]): Decoder[Pair[A]] =
    Decoder[List[A]].emap:
      case List(x, y) =>
        Pair.from(x, y).leftMap(_.toString)

      case xs =>
        Left:
          s"Input list ${xs.toString} was not exactly length 2"

  given [A](using Decoder[Pair[A]]): Decoder[HistoricalEntry[PartialOrder.Edge[A]]] with
    def apply(c: HCursor): Decoder.Result[HistoricalEntry[PartialOrder.Edge[A]]] =
      for
        xs        <- c.downField("pair").as[Pair[A]]
        pref      <- c.downField("preference").as[BinaryPreference]
        createdAt <- c.downField("createdAt").as[Long]
      yield HistoricalEntry(PartialOrder.Edge[A](xs, pref), createdAt)

  class SyncInteractiveSessionState[F[_]: Sync, A: Order](
      pumlPath: String,
      population: List[A],
      rng: std.Random[F],
      seedRef: Ref[F, Int],
      prefRef: Ref[F, PartialOrder[A]],
      historicalEdges: Ref[F, List[HistoricalEntry[PartialOrder.Edge[A]]]],
      storage: YamlTableService[F, HistoricalEntry[PartialOrder.Edge[A]]]
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

        newEntry = HistoricalEntry(PartialOrder.Edge(pair, BinaryPreference.First), 0L)
        _       <- historicalEdges.update(xs => newEntry :: xs)
        _       <- storage.addOne(newEntry)

        _ <- updateSeed
        _ <- runTournament

        _ <- printCurrentPair
      yield ()

    def preferSecond: F[Unit] =
      for
        pair <- getCurrentPair
        _    <- prefRef.update(_.withPreference(pair, BinaryPreference.Second))

        newEntry = HistoricalEntry(PartialOrder.Edge(pair, BinaryPreference.Second), 0L)
        _       <- historicalEdges.update(xs => newEntry :: xs)
        _       <- storage.addOne(newEntry)

        _ <- updateSeed
        _ <- runTournament

        _ <- printCurrentPair
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

    def writePuml: F[Unit] =
      for
        prefs <- prefRef.get
        lines = prefs
          .xs
          .values
          .toList
          .map:
            case PartialOrder.Edge(Pair(x, y), rel) =>
              rel match
                case BinaryPreference.First =>
                  s"[${x.toString}] --> [${y.toString}]"

                case BinaryPreference.Second =>
                  s"[${y.toString}] --> [${x.toString}]"
          .prepended("@startuml")
          .appended("@enduml")
        _ <- FileIO.Writer.sync[F].writeLines(pumlPath, lines)
      yield ()

    def runTournament: F[Unit] =
      for
        prefs <- prefRef.get

        is <- TotalIndexGenerator.generateN[F, A](rng.shuffleList, population, prefs.xs.size * 2)

        withScores = is.fproduct(idx => TotalIndexFitness.score(idx, prefs))

        _ <- withScores
          .sortBy(_._2)
          .takeRight(3)
          .traverse: idx =>
            out.println(idx.toString) *> out.println("")
      yield ()

  def sync[F[_]: Sync: std.Console, A: Order: Decoder: Encoder](
      population: List[A],
      historicalPath: String,
      pumlPath: String
  ): F[InteractiveSessionState[F]] =
    for
      rng             <- std.Random.scalaUtilRandom[F]
      startSeed       <- Ref[F].of(0)
      startPrefsEmpty <- Ref[F].of(PartialOrder.empty[A])

      yaml = YamlTableService[F, HistoricalEntry[PartialOrder.Edge[A]]](
        historicalPath,
        FileIO.Reader.sync,
        FileIO.Writer.sync
      )

      xs <- yaml.read

      historicalEdges <- Ref[F].of(xs)

      startingPrefs <- Ref[F].of:
        xs
          .groupBy(_.x.pair)
          .view
          .mapValues(_.maxBy(_.millis))
          .mapValues(_.x.pref)
          .toList
          .map: (a, b) =>
            PartialOrder.empty.withPreference(a, b)
          .combineAll

      state = SyncInteractiveSessionState(pumlPath, population, rng, startSeed, startingPrefs, historicalEdges, yaml)

      _ <- state.updateSeed
    yield state
