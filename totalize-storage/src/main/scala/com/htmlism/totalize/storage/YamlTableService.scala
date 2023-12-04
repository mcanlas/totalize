package com.htmlism.totalize.storage

import cats.*
import cats.syntax.all.*

import com.htmlism.totalize.storage.FileIO.*

class YamlTableService[F[_]: Monad, A](path: String)(using R: Reader[F], W: Writer[F]):
  (path, R, W)

  def read: F[List[A]] =
    for _ <- Monad[F].unit
    yield Nil

  def write(xs: List[A]): F[Unit] =
    for _ <- Monad[F].unit
    yield ()

  def addOne(x: A): F[Unit] =
    for
      old <- read
      _   <- write(x :: old)
    yield ()
