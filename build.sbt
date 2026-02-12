inThisBuild(
  List(
    organization := "io.github.karimagnusson",
    homepage     := Some(url("https://github.com/karimagnusson/slinq")),
    licenses     := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    developers   := List(
      Developer(
        "karimagnusson",
        "Kari Magnusson",
        "kotturinn@gmail.com",
        url("https://github.com/karimagnusson")
      )
    )
  )
)

ThisBuild / version       := "0.9.6-RC1"
ThisBuild / versionScheme := Some("early-semver")
ThisBuild / scalaVersion  := "3.3.7"

ThisBuild / publishTo := Some(
  "GitHub Packages" at "https://maven.pkg.github.com/karimagnusson/slinq"
)
ThisBuild / credentials += Credentials(
  "GitHub Package Registry",
  "maven.pkg.github.com",
  "karimagnusson",
  sys.env.getOrElse("GITHUB_TOKEN", "")
)

lazy val commonScalacOptions = Seq(
  "-encoding", "utf8",
  "-feature",
  "-language:higherKinds",
  "-language:existentials",
  "-language:implicitConversions",
  "-deprecation",
  "-unchecked"
)

lazy val root = (project in file("."))
  .aggregate(slinqPg, slinqPgZio, slinqPgEc)
  .settings(
    name := "slinq",
    publish / skip := true
  )

lazy val slinqPg = (project in file("slinq-pg"))
  .settings(
    name := "slinq-pg",
    libraryDependencies ++= Seq(
      "org.postgresql" % "postgresql" % "42.7.3",
      "org.scalameta" %% "munit"      % "1.0.0" % Test
    ),
    Compile / scalacOptions ++= commonScalacOptions
  )

lazy val slinqPgZio = (project in file("slinq-pg-zio"))
  .dependsOn(slinqPg)
  .settings(
    name := "slinq-pg-zio",
    libraryDependencies ++= Seq(
      "dev.zio"      %% "zio"          % "2.1.23",
      "dev.zio"      %% "zio-streams"  % "2.1.23",
      "org.postgresql" % "postgresql"  % "42.7.3",
      "dev.zio"      %% "zio-test"     % "2.1.23" % Test,
      "dev.zio"      %% "zio-test-sbt" % "2.1.23" % Test,
      "com.typesafe"  % "config"       % "1.4.3"  % Test
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
    Compile / scalacOptions ++= commonScalacOptions
  )

lazy val slinqPgEc = (project in file("slinq-pg-ec"))
  .dependsOn(slinqPg)
  .settings(
    name := "slinq-pg-ec",
    libraryDependencies ++= Seq(
      "com.zaxxer"    % "HikariCP"    % "7.0.2",
      "org.postgresql" % "postgresql" % "42.7.3",
      "com.typesafe"  % "config"      % "1.4.3"  % Test,
      "org.scalameta" %% "munit"      % "1.0.0"  % Test
    ),
    Compile / scalacOptions ++= commonScalacOptions
  )
