lazy val root =
  Project("totalize", file("."))
    .aggregate(core, console)

lazy val core =
  module("core")
    .withCats
    .withTesting
    .settings(description := "A framework for generating total orderings")

lazy val console =
  module("console")
    .withCats
    .withTesting
    .settings(description := "Tools for refining preferences using the Scala console")
