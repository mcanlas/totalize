package com.htmlism.totalize.core

import scala.util.Random

import cats.*
import cats.syntax.all.*

object TotalIndexGenerator:
  def generate[A: Eq](rng: Random, xs: List[A]): TotalIndex[A] =
    TotalIndex:
      rng.shuffle(xs)

  def generateN[F[_]: Applicative, A: Eq](shuffle: List[A] => F[List[A]], xs: List[A], n: Int): F[List[TotalIndex[A]]] =
    (0 until n)
      .toList
      .traverse(_ => shuffle(xs).map(TotalIndex(_)))
