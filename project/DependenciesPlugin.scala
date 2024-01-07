import sbt.Keys.*
import sbt.*

object DependenciesPlugin extends AutoPlugin {
  override def trigger = allRequirements

  object autoImport {
    implicit class DependencyOps(p: Project) {
      def withCats: Project =
        p
          .settings(libraryDependencies += "org.typelevel" %% "cats-core" % "2.10.0")

      def withEffectMonad: Project =
        p.settings(libraryDependencies += "org.typelevel" %% "cats-effect" % "3.5.2")

      def withYaml: Project =
        p.settings(
          libraryDependencies ++= Seq(
            "io.circe" %% "circe-yaml" % "0.15.1"
          )
        )

      def withTesting: Project = {
        val weaverVersion =
          "0.8.3"

        p.settings(
          testFrameworks += new TestFramework("weaver.framework.CatsEffect"),
          libraryDependencies ++= Seq(
            "com.disneystreaming" %% "weaver-cats"       % weaverVersion % Test,
            "com.disneystreaming" %% "weaver-scalacheck" % weaverVersion % Test
          )
        )
      }
    }
  }
}
