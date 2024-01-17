package com.htmlism.totalize.core.genetic

import cats.syntax.all.*

/**
  * @tparam A
  *   The encoded type
  * @tparam B
  *   The domain type
  */
trait SolutionDecoder[A, B]:
  def decode(x: A): Either[String, B]

class TotalIndexDecoder(ids: List[String]) extends SolutionDecoder[Array[Int], List[String]]:
  def decode(xs: Array[Int]): Either[String, List[String]] =
    if xs.length == ids.length then
      ids
        .zip(xs)
        .sortBy(_._2)
        .map(_._1)
        .asRight
    else s"Input length ${xs.length} does not match population length ${ids.length}".asLeft
