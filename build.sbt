name := "investigae_thrift_finagle"

version := "0.1"

scalaVersion := "2.12.10"

libraryDependencies ++= Seq(
  // needed to create http service
  "com.twitter" %% "finagle-http" % "20.12.0",

  // needed for thrift
  "com.twitter" %% "finagle-thrift" % "20.12.0",
  "org.scalatest" %% "scalatest" % "3.1.1" % "test ",

  // needed for scrooge https://twitter.github.io/scrooge/SBTPlugin.html
  "org.apache.thrift" % "libthrift" % "0.10.0",
  "com.twitter" %% "scrooge-core" % "20.12.0",
  "com.twitter" %% "finagle-thrift" % "20.12.0",

  // needed for actor
  "com.typesafe.akka" %% "akka-actor-typed" % "2.6.8",

  // convert to and from stuff (for example twitter future to scala future)
  "com.twitter" %% "bijection-core" % "0.9.7",
  "com.twitter" %% "bijection-util" % "0.9.7",
  "com.twitter" %% "bijection-json" % "0.9.7",

  // airframe
  "org.wvlet.airframe" %% "airframe-http-finagle" % "21.1.0",

  // finch
  "com.github.finagle" %% "finchx-core" % "0.32.0",
  "com.github.finagle" %% "finchx-circe" % "0.32.0",
  "io.circe" %% "circe-generic" % "0.13.0",

  // cats
  "org.typelevel" %% "cats-core" % "2.1.1",
  "org.typelevel" %% "cats-effect" % "2.1.1",

  // Twitter server
  "com.twitter" %% "twitter-server" % "20.12.0",

  // Test
  "org.scalatest" %% "scalatest" % "3.2.3" % "test",
  "org.scalacheck" %% "scalacheck" % "1.15.2" % "test",
  "org.scalatestplus" %% "scalatestplus-scalacheck" % "3.1.0.0-RC2" % "test"

)

