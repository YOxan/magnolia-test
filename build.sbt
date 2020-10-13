name := "Magnolia"

version := "0.1"

scalaVersion := "2.13.3"

libraryDependencies += "com.propensive" %% "magnolia" % "0.16.0"
libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value % Provided
libraryDependencies += "org.typelevel" %% "cats-core" % "2.0.0"
