FROM gradle:8.5-jdk17 AS build
WORKDIR /home/gradle/src

COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY src ./src
COPY gradle ./gradle

RUN gradle --no-daemon clean installDist

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

COPY --from=build /home/gradle/src/build/install/issuetalk-auth-service /app

EXPOSE 8080

ENTRYPOINT ["./bin/issuetalk-auth-service"]
