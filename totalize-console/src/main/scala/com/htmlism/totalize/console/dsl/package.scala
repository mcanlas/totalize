package com.htmlism.totalize.console

import cats.Order
import cats.effect.IO

package object dsl:
  val states: List[String] = List(
    "Alabama",
    "Alaska",
    "Arizona",
    "Arkansas",
    "California",
    "Colorado",
    "Connecticut",
    "Delaware",
    "Florida",
    "Georgia",
    "Hawaii",
    "Idaho",
    "Illinois",
    "Indiana",
    "Iowa",
    "Kansas",
    "Kentucky",
    "Louisiana",
    "Maine",
    "Maryland",
    "Massachusetts",
    "Michigan",
    "Minnesota",
    "Mississippi",
    "Missouri",
    "Montana",
    "Nebraska",
    "Nevada",
    "New Hampshire",
    "New Jersey",
    "New Mexico",
    "New York",
    "North Carolina",
    "North Dakota",
    "Ohio",
    "Oklahoma",
    "Oregon",
    "Pennsylvania",
    "Rhode Island",
    "South Carolina",
    "South Dakota",
    "Tennessee",
    "Texas",
    "Utah",
    "Vermont",
    "Virginia",
    "Washington",
    "West Virginia",
    "Wisconsin",
    "Wyoming"
  )

  val planets: List[String] =
    List("Mercury", "Venus", "Earth", "Mars", "Jupiter", "Saturn", "Uranus", "Neptune", "Pluto")

  def session[A: Order](xs: List[A], path: String, pumlPath: String)(using
      cats.effect.unsafe.IORuntime
  ): InteractiveSessionState[IO] =
    InteractiveSessionState
      .sync[IO, A](xs, path, pumlPath: String)
      .unsafeRunSync()

  def ask(using S: InteractiveSessionState[IO], R: cats.effect.unsafe.IORuntime): Unit =
    S.printCurrentPair.unsafeRunSync()

  def a(using S: InteractiveSessionState[IO], R: cats.effect.unsafe.IORuntime): Unit =
    (S.preferFirst *> S.writePuml).unsafeRunSync()

  def d(using S: InteractiveSessionState[IO], R: cats.effect.unsafe.IORuntime): Unit =
    (S.preferSecond *> S.writePuml).unsafeRunSync()

  def dump(using S: InteractiveSessionState[IO], R: cats.effect.unsafe.IORuntime): Unit =
    S.dump.unsafeRunSync()
