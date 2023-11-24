lazy val root =
  Project("totalize", file("."))
    .aggregate(core, console)

lazy val core =
  module("core")
    .withCats
    .withTesting
    .settings(description := "A framework for generating total orderings")
    .settings(
      console / initialCommands := "import com.htmlism.totalize.console.dsl.*"
    )

lazy val console =
  module("console")
    .withCats
    .withTesting
    .settings(description := "Tools for refining preferences using the Scala console")
