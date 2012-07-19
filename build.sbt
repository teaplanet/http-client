name := "http-client"

version := "1.0"

organization := "com.github.teaplanet"

scalaVersion := "2.9.2"

libraryDependencies ++= Seq(
	"org.apache.httpcomponents" % "httpclient" % "4.2",
	"org.specs2" %% "specs2" % "1.11" % "test",
	"org.scalaz" %% "scalaz-core" % "6.0.4" % "test"
)

