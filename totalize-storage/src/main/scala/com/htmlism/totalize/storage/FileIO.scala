package com.htmlism.totalize.storage

import java.nio.file.*

import scala.jdk.CollectionConverters.*

import cats.effect.*
import cats.syntax.all.*

object FileIO:
  trait Reader[F[_]]:
    def readString(src: String): F[String]

  object Reader:
    def sync[F[_]](using F: Sync[F]): Reader[F] =
      new Reader[F]:
        def readString(src: String): F[String] =
          F.blocking:
            Files.readString(Path.of(src))

  trait Writer[F[_]]:
    def writeLines(dest: String, xs: Iterable[String]): F[Unit]

  object Writer:
    def sync[F[_]](using F: Sync[F]): Writer[F] =
      new Writer[F]:
        def writeLines(dest: String, xs: Iterable[String]): F[Unit] =
          F.blocking:
            Files
              .write(Path.of(dest), xs.asJava)
          .void
