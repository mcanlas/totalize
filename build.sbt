lazy val root =
  Project("totalize", file("."))
    .aggregate(core)

lazy val core =
  module("core")
    .withCats
    .withTesting
    .settings(description := "A framework for generating total orderings")
