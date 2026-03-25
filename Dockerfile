FROM eclipse-temurin:11-jre AS builder

# Install sbt
RUN apt-get update && apt-get install -y curl && \
    curl -fsSL "https://github.com/sbt/sbt/releases/download/v1.10.7/sbt-1.10.7.tgz" | tar xz -C /opt && \
    ln -s /opt/sbt/bin/sbt /usr/local/bin/sbt

WORKDIR /build
COPY . .
RUN sbt cli/assembly

FROM apache/spark:3.5.5-java11
USER root

COPY --from=builder /build/cli/target/scala-2.13/teckel-etl_2.13.jar /opt/teckel/teckel-etl.jar

ENTRYPOINT ["/opt/spark/bin/spark-submit", "--class", "com.eff3ct.teckel.app.Main", "/opt/teckel/teckel-etl.jar"]
