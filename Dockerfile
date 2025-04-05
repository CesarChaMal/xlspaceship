FROM openjdk:11
RUN curl -L -o sbt.tgz https://github.com/sbt/sbt/releases/download/v1.9.7/sbt-1.9.7.tgz && \
    tar -xzf sbt.tgz && \
    mv sbt /opt/sbt && \
    ln -s /opt/sbt/bin/sbt /usr/local/bin/sbt
WORKDIR /app
COPY . .
RUN sbt clean compile
CMD ["sbt", "run"]
