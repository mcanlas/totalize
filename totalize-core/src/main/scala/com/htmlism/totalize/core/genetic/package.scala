package com.htmlism.totalize.core.genetic

opaque type Population[A] <: Vector[A] =
  Vector[A]

object Population:
  def apply[A](xs: Vector[A]): Population[A] =
    xs
