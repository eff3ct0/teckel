/** Compiler */
//addSbtPlugin("org.typelevel"      % "sbt-tpolecat"       % "0.5.2")
addSbtPlugin("org.scoverage"      % "sbt-scoverage"      % "2.3.0")
addCompilerPlugin("com.olegpy"   %% "better-monadic-for" % "0.3.1")
addCompilerPlugin("org.typelevel" % "kind-projector"     % "0.13.2" cross CrossVersion.full)

/** Build */
addSbtPlugin("com.eed3si9n"      % "sbt-assembly" % "2.3.0")
addSbtPlugin("org.scalameta"     % "sbt-scalafmt" % "2.5.2")
addSbtPlugin("de.heikoseeberger" % "sbt-header"   % "5.10.0")

/** Publish */
addSbtPlugin("com.github.sbt" % "sbt-ci-release" % "1.9.2")
