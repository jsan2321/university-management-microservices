# ── Build ──────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
# Install parent POM and download dependencies first (layer cache)
RUN ./mvnw dependency:go-offline -pl config-server -am -q -B 2>/dev/null || true
COPY config-server/ config-server/
RUN ./mvnw -pl config-server package -DskipTests -q -B

# ── Runtime ────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app
COPY --from=builder /build/config-server/target/*.jar app.jar
RUN chown app:app app.jar
USER app
EXPOSE 8888
ENTRYPOINT ["java", "-jar", "app.jar"]
