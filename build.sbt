organization  := "agileand.me"

version       := "0.1"

licenses in ThisBuild := Seq(("The MIT License (MIT)", new URL("http://opensource.org/licenses/MIT")))

scalaVersion  := "2.10.3"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= {
  val akkaVersion = "2.3.1"
  val sprayVersion = "1.3.1"
  Seq(
    "io.spray"                %   "spray-client"    % sprayVersion,
    "io.spray"                %   "spray-can"       % sprayVersion,
    "io.spray"                %   "spray-http"      % sprayVersion,
    "io.spray"                %   "spray-httpx"     % sprayVersion,
    "io.spray"                %   "spray-util"      % sprayVersion,
    "io.spray"                %   "spray-routing"   % sprayVersion,
    "io.spray"               %%   "spray-json"      % "1.2.6",
    "com.github.nscala-time" %%   "nscala-time"     % "0.8.0",
    "com.typesafe.akka"      %%   "akka-actor"      % akkaVersion,
    "com.typesafe.akka"      %%   "akka-contrib"    % akkaVersion,
    "ch.qos.logback"          %   "logback-classic" % "1.1.1",
    "io.spray"                %   "spray-testkit"   % sprayVersion  % "test",
    "com.typesafe.akka"      %%   "akka-testkit"    % akkaVersion   % "test",
    "org.specs2"             %%   "specs2"          % "2.2.3" % "test",
    "org.mockito"             %   "mockito-core"    % "1.9.5" % "test"
  )
}

