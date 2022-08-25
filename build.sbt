import scala.scalanative.build._

ThisBuild / version      := "1.0.1"
ThisBuild / organization := "com.burdinov"
ThisBuild / scalaVersion := "3.1.3"

lazy val root = (project in file("."))
  .settings(
    name := "table",
    libraryDependencies ++= Seq("com.github.scopt" %%% "scopt" % "4.1.0", "org.scalameta" %% "munit" % "0.7.29" % Test),
    testFrameworks += new TestFramework("munit.Framework")
  )

enablePlugins(ScalaNativePlugin)
//TODO add cross compilation

nativeConfig ~= {
  _.withLTO(LTO.full)
    .withMode(Mode.releaseFull)
    .withGC(GC.commix)
}
