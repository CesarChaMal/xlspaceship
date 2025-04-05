logLevel := Level.Warn

resolvers += "Typesafe Releases" at "https://repo.typesafe.com/typesafe/releases/"
resolvers += Resolver.sbtPluginRepo("releases")


addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.9.0")
addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.10.0")

