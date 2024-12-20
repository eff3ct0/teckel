import xerial.sbt.Sonatype.GitHubHosting

sonatypeProfileName := "com.eff3ct"
homepage            := Some(url("https://github.com/rafafrdz/teckel"))
licenses            := Seq("MIT" -> url("https://opensource.org/licenses/MIT"))
publishMavenStyle   := true
scmInfo := Some(
  ScmInfo(
    browseUrl = url("https://github.com/rafafrdz/teckel"),
    connection = "scm:git:git@github.com:rafafrdz/teckel.git"
  )
)

developers := List(
  Developer(
    id = "rafafrdz",
    name = "Rafael Fernandez",
    email = "hi@rafaelfernandez.dev",
    url = url("https://rafaelfernandez.dev")
  )
)

sonatypeProjectHosting := Some(GitHubHosting("rafafrdz", "teckel", "hi@rafaelfernandez.dev"))
