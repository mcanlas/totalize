lazy val root =
  Project("totalize", file("."))
    .aggregate(core, tConsole)

lazy val core =
  module("core")
    .withCats
    .withTesting
    .settings(description := "A framework for generating total orderings")

lazy val tConsole =
  module("console")
    .withEffectMonad
    .withTesting
    .settings(description := "Tools for refining preferences using the Scala console")
    .dependsOn(core)
    .settings(
      console / initialCommands := Seq(
        "import com.htmlism.totalize.console.dsl.*",
        "import cats.effect.unsafe.implicits.global"
      )
        .mkString(";")
    )
