import sbt._
import Keys._

object Build extends Build {

  lazy val project = Project("root", file(".")).settings(
    name := "SrtCorrector",

    scalaVersion := "2.10.1",

    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-reflect" % "2.10.1",
      "joda-time" % "joda-time" % "2.2",
      "org.joda" % "joda-convert" % "1.3.1",
      "org.specs2" %% "specs2" % "1.14" % "test"
    ),

    resolvers ++= Seq(
      "sonatypeSnapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
      "sgodbillon" at "https://bitbucket.org/sgodbillon/repository/raw/master/snapshots/",
      "sbt-idea-repo" at "http://mpeltonen.github.com/maven/"
    )
  )
}
