package com.htmlism.totalize.storage

object HistoricalEntry:
  trait Key[A, B]:
    def keyOf(x: A): B

case class HistoricalEntry[A](x: A, createdAtMillis: Long)
