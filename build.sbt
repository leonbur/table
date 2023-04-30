import scala.scalanative.build.*

ThisBuild / version      := "1.0.1"
ThisBuild / organization := "com.burdinov"
ThisBuild / scalaVersion := "3.2.2"

lazy val table = (project in file("."))
  .settings(
    name := "table",
    libraryDependencies ++=
      Seq(
        "com.github.scopt" %%% "scopt"      % "4.1.0",
        "org.scalameta"    %%% "munit"      % "1.0.0-M7" % Test,
        "org.scalatestplus" %% "junit-4-13" % "3.2.15.0" % Test
      ),
    testFrameworks += new TestFramework("munit.Framework")
  )

enablePlugins(ScalaNativePlugin)

nativeConfig ~= {
  _.withLTO(LTO.full)
    .withMode(Mode.releaseFull)
    .withGC(GC.commix)
}
