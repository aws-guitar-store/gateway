FROM openjdk:8-jdk-alpine

ARG JAR_FILE
ENV SERVER_PORT=8080

VOLUME /tmp

COPY ${JAR_FILE} app.jar

RUN apk --no-cache add curl
HEALTHCHECK CMD curl -v --fail http://localhost:${SERVER_PORT}/actuator/health || exit 1

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
