/** Compiler */
addCompilerPlugin("com.olegpy"   %% "better-monadic-for" % "0.3.1")
addCompilerPlugin("org.typelevel" % "kind-projector"     % "0.13.2" cross CrossVersion.full)

/** Build */
addSbtPlugin("com.eed3si9n"      % "sbt-assembly" % "2.3.0")
addSbtPlugin("org.scalameta"     % "sbt-scalafmt" % "2.5.2")
addSbtPlugin("de.heikoseeberger" % "sbt-header"   % "5.10.0")

/** Publish */
addSbtPlugin("com.github.sbt" % "sbt-release"  % "1.4.0")
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "3.12.2")
addSbtPlugin("com.github.sbt" % "sbt-pgp"      % "2.3.1")
