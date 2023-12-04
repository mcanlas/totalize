lazy val root =
  Project("totalize", file("."))
    .aggregate(core, consoleSubProject, storage)

lazy val core =
  module("core")
    .withCats
    .withTesting
    .settings(description := "A framework for generating total orderings")

lazy val consoleSubProject =
  module("console")
    .withEffectMonad
    .withTesting
    .settings(description := "Tools for refining preferences using the Scala console")
    .dependsOn(core, storage)
    .settings(
      console / initialCommands := Seq(
        "import com.htmlism.totalize.console.*",
        "import com.htmlism.totalize.console.dsl.*",
        "import cats.effect.unsafe.implicits.global"
      )
        .mkString(";")
    )

lazy val storage =
  module("storage")
    .settings(description := "Support for persisting user data")
    .withEffectMonad
    .dependsOn(core)
