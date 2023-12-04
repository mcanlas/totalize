package com.htmlism.totalize.storage

import cats.*
import cats.syntax.all.*

import com.htmlism.totalize.storage.FileIO.*

class YamlTableService[F[_]: FlatMap, A](path: String)(using R: Reader[F], W: Writer[F]):
  def read: F[List[A]] =
    for _ <- R.readString(path)
    yield Nil

  def write(xs: List[A]): F[Unit] =
    for _ <- W.writeLines(path, Nil)
    yield ()

  def addOne(x: A): F[Unit] =
    for
      old <- read
      _   <- write(x :: old)
    yield ()
