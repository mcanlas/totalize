package com.htmlism.totalize.storage

import cats.*
import cats.syntax.all.*
import io.circe.*

import com.htmlism.totalize.storage.FileIO.*

class YamlTableService[F[_]: MonadThrow, A](path: String, R: Reader[F], W: Writer[F])(using
    enc: Encoder[List[A]],
    dec: Decoder[List[A]]
):
  def read: F[List[A]] =
    for
      s  <- R.readString(path)
      xs <- yaml
        .parser
        .parse(s)
        .flatMap(_.as[List[A]])
        .liftTo[F]
    yield xs

  def write(xs: List[A]): F[Unit] =
    val str =
      enc
        .apply(xs)
        .spaces2

    for _ <- W.writeLines(path, List(str))
    yield ()

  def addOne(x: A): F[List[A]] =
    for
      old <- read

      everything = x :: old

      _ <- write(everything)
    yield everything
