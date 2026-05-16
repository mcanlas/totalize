import sbt.Keys.*
import sbt.*

object DependenciesPlugin extends AutoPlugin {
  override def trigger = allRequirements

  object autoImport {
    implicit class DependencyOps(p: Project) {
      val circeVersion =
        "0.14.15"

      val circeYamlVersion =
        "0.16.1"

      def withCats: Project =
        p
          .settings(libraryDependencies += "org.typelevel" %% "cats-core" % Versions.catsCore)

      def withEffectMonad: Project =
        p.settings(libraryDependencies += "org.typelevel" %% "cats-effect" % Versions.catsEffect)

      def withLogging: Project =
        p.settings(
          libraryDependencies ++= Seq(
            "org.typelevel" %% "log4cats-slf4j" % "2.6.0"
          )
        )

      def withYaml: Project =
        p.settings(
          libraryDependencies ++= Seq(
            "io.circe" %% "circe-core" % circeVersion,
            "io.circe" %% "circe-yaml" % circeYamlVersion
          )
        )

      def withTesting: Project =
        p.settings(
          libraryDependencies ++= Seq(
            "org.typelevel" %% "weaver-cats"       % Versions.weaver % Test,
            "org.typelevel" %% "weaver-scalacheck" % Versions.weaver % Test
          )
        )
    }
  }
}
