# -------- Stage 1: build --------
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# Cache deps
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

# Build
COPY src ./src
RUN mvn -q -DskipTests package

# -------- Stage 2: runtime --------
FROM eclipse-temurin:21-jre
WORKDIR /app

# Run as non-root
RUN useradd -r -u 10001 appuser
USER appuser

# Copy jar
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8081

ENV JAVA_OPTS=""

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
