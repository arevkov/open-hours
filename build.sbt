
lazy val akkaHttpVersion = "10.1.12"
lazy val akkaVersion    = "2.6.6"

lazy val `open-hours` = (project in file("."))
  .settings(
    inThisBuild(List(
      organization    := "test",
      scalaVersion    := "2.12.11"
    )),
    name := "open-hours",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http"                % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json"     % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-stream"              % akkaVersion,
      "ch.qos.logback"    % "logback-classic"           % "1.2.3",

      "com.typesafe.akka" %% "akka-testkit"             % akkaVersion     % Test,
      "com.typesafe.akka" %% "akka-http-testkit"        % akkaHttpVersion % Test,
      "org.scalatest"     %% "scalatest"                % "3.0.8"         % Test
    ),
    assemblyJarName in assembly := "openhours.jar",
    mainClass in assembly := Some("openh.OpenHoursServer"),
  )
  .settings(
    artifact in(Compile, assembly) := {
      val art = (artifact in(Compile, assembly)).value
      art.withClassifier(Some("assembly"))
    }
  )
  .settings(addArtifact(artifact in (Compile, assembly), assembly))
