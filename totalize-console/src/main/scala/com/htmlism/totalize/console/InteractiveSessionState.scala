package com.htmlism.totalize.console

import cats.effect.*
import cats.syntax.all.*
import cats.{PartialOrder as _, *}
import io.circe.*
import io.circe.syntax.*

import com.htmlism.totalize.core.*
import com.htmlism.totalize.core.genetic.*
import com.htmlism.totalize.storage
import com.htmlism.totalize.storage.FileIO
import com.htmlism.totalize.storage.HistoricalEntry
import com.htmlism.totalize.storage.YamlTableService

trait InteractiveSessionState[F[_], A]:
  def getCurrentPair: F[Pair[A]]

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
    def apply(hx: HistoricalEntry[PartialOrder.Edge[A]]): Json =
      JsonObject(
        "pair"       -> List(hx.x.pair.x, hx.x.pair.y).asJson,
        "preference" -> hx.x.pref.asJson,
        "createdAt"  -> hx.createdAtMillis.asJson
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

  class SyncInteractiveSessionState[F[_]: Sync: Clock, A: Order](
      pumlPath: String,
      population: List[A],
      rng: std.Random[F],
      seedRef: Ref[F, Int],
      prefRef: Ref[F, PartialOrder[A]],
      historicalEdges: Ref[F, List[HistoricalEntry[PartialOrder.Edge[A]]]],
      storage: YamlTableService[F, HistoricalEntry[PartialOrder.Edge[A]]],
      geneticConfig: GeneticConfig
  )(using out: std.Console[F])
      extends InteractiveSessionState[F, A]:
    assert(population.size > 1, "Population must be at least 2")

    def updateSeed: F[Unit] =
      rng.nextInt >>= seedRef.set

    def getCurrentPair: F[Pair[A]] =
      for
        shuffler <- seedRef
          .get
          .flatMap(std.Random.scalaUtilRandomSeedInt[F])

        prefs <- prefRef.get

//        _ = prefs.xs.keys.foreach(println)

        xToYs = prefs
          .xs
          .keys
          .toList
          .flatMap(p =>
            List(
              p.x -> p.y,
              p.y -> p.x
            )
          )
          .groupBy(_._1)

//        sorted = population
//          .fproduct(x => prefs.xs.keys.toList.flatMap(p => List(p.x, p.y)).count(_ == x))
//          .sortBy(_._2)

//        _ = sorted.foreach(println)

        x <- shuffler.elementOf(population)

        yCandidates = population diff (x :: xToYs.getOrElse(x, Nil))
//        _ = println(x)
//        _ = yCandidates.foreach(println)

        // TODO this is actually fallible when the pairs are full; in that case we should just go back to selecting random
        y <- shuffler.elementOf(yCandidates)

//        List(x, y) <- sorted
//          .map(_._1)
//          .take(3)
//          .pipe(shuffler.shuffleList)
//          .map(_.take(2))

//        y <- shuffler.elementOf(population diff List(x))

        pair <- Pair.from(x, y).liftTo[F]
      yield pair

    def preferFirst: F[Unit] =
      for
        pair <- getCurrentPair
        _    <- prefRef.update(_.withPreference(pair, BinaryPreference.First))

        now <- Clock[F].realTime.map(_.toMillis)

        newEntry = HistoricalEntry(PartialOrder.Edge(pair, BinaryPreference.First), now)
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

        now <- Clock[F].realTime.map(_.toMillis)

        newEntry = HistoricalEntry(PartialOrder.Edge(pair, BinaryPreference.Second), now)
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
          .filter(edge => population.contains(edge.pair.x) && population.contains(edge.pair.y))
          .toList
          .map:
            case PartialOrder.Edge(Pair(x, y), rel) =>
              val xSafe =
                x.toString.replace("[", "").replace("]", "")

              val ySafe =
                y.toString.replace("[", "").replace("]", "")

              rel match
                case BinaryPreference.First =>
                  s"[$xSafe] --> [$ySafe]"

                case BinaryPreference.Second =>
                  s"[$ySafe] --> [$xSafe]"
          .prepended("@startuml")
          .appended("@enduml")
        _ <- FileIO.Writer.sync[F].writeLines(pumlPath, lines)
      yield ()

    def runTournament: F[Unit] =
      for
        prefs <- prefRef.get
        rng   <- std.Random.scalaUtilRandom[F]

        ff: Fitness[Array[Int], Int] =
          Fitness.ArrayInt(population, prefs)

        given Fitness[Array[Int], Int] = ff

        given Crossover[F, Array[Int]] =
          Crossover.Blend[F](rng)

        given Mutation[F, Array[Int]] =
          Mutation.ArrayInt[F](rng)

        solGen = SolutionGenerator.sequence[F](rng).generate(population.length)
        pop <- PopulationGenerator
          .sequence[F]
          .generate(geneticConfig.firstPopulationSize, solGen)
          .flatMap(xs => Evolution.evolveSequence(xs, geneticConfig.populationSize, rng))
          .flatMap(xs => Evolution.evolveSequence(xs, geneticConfig.populationSize, rng))
          .flatMap(xs => Evolution.evolveSequence(xs, geneticConfig.populationSize, rng))
          .flatMap(xs => Evolution.evolveSequence(xs, geneticConfig.populationSize, rng))
          .flatMap(xs => Evolution.evolveSequence(xs, geneticConfig.populationSize, rng))
          .flatMap(xs => Evolution.evolveSequence(xs, geneticConfig.populationSize, rng))
          .flatMap(xs => Evolution.evolveSequence(xs, geneticConfig.populationSize, rng))
          .flatMap(xs => Evolution.evolveSequence(xs, geneticConfig.populationSize, rng))

        _ = println("scores:")
        _ = pop.map(ff.fitness).sorted.foreach(println)

        bestSolution = pop.maxBy(ff.fitness)

        orderedPopulation = population
          .zip(bestSolution)
          .sortBy(_._2)

        _ = orderedPopulation.foreach(println)
      yield ()

  def sync[F[_]: Sync: std.Console, A: Order: Decoder: Encoder](
      population: List[A],
      historicalPath: String,
      pumlPath: String,
      geneticConfig: GeneticConfig
  ): F[InteractiveSessionState[F, A]] =
    for
      rng       <- std.Random.scalaUtilRandom[F]
      startSeed <- Ref[F].of(0)

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
          .mapValues(_.maxBy(_.createdAtMillis))
          .mapValues(_.x.pref)
          .toList
          .map: (a, b) =>
            PartialOrder.empty.withPreference(a, b)
          .combineAll

      state = SyncInteractiveSessionState(
        pumlPath,
        population,
        rng,
        startSeed,
        startingPrefs,
        historicalEdges,
        yaml,
        geneticConfig
      )

      _ <- state.updateSeed
    yield state
