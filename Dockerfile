FROM maven:3.9-eclipse-temurin-24-noble AS builder
LABEL authors="josip"

WORKDIR /app

COPY pom.xml .
COPY ./src src

RUN mvn -Dmaven.repo.local=/tmp/.m2 clean package -DskipTests

# Exctacting stages from the .jar file
FROM eclipse-temurin:24-jre-noble
RUN mkdir /app \
    && groupadd --system --gid 3000 dippro \
    && useradd --system --uid 3000 --shell /bin/false --gid dippro dippro

RUN chown -R dippro:dippro /app
USER dippro

WORKDIR /app
EXPOSE 8080

COPY --from=builder /app/target/*.jar /app/app.jar
ENTRYPOINT ["java", "-cp", "app.jar", "hr.fer.dippro.Main"]
#ENTRYPOINT ["java", "-jar", "app.jar"]