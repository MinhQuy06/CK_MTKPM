# syntax=docker/dockerfile:1
# ── Giai đoạn 1: build jar (JDK 21 + Maven) ─────────────────────────
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B -DskipTests package

# ── Giai đoạn 2: chạy bằng JRE, user không phải root ─────────────────
FROM eclipse-temurin:21-jre
RUN apt-get update && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && useradd --system --uid 1001 clinic
WORKDIR /app
COPY --from=build /build/target/*.jar app.jar
USER clinic
ENV SPRING_PROFILES_ACTIVE=prod \
    SERVER_PORT=18081 \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75"
EXPOSE 18081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
