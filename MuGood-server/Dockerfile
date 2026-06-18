FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
COPY muGood-common ./muGood-common
COPY muGood-domain ./muGood-domain
COPY muGood-infrastructure ./muGood-infrastructure
COPY muGood-service ./muGood-service
COPY muGood-web ./muGood-web

RUN mvn clean package -pl muGood-web -am -DskipTests

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/muGood-web/target/*SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java -jar app.jar --server.port=${PORT:-8080}"]