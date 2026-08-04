# ── Build ──────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build
COPY . .
RUN ./mvnw -pl discovery-server -am package -DskipTests -q -B

# ── Runtime ────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app
COPY --from=builder /build/discovery-server/target/*.jar app.jar
RUN chown app:app app.jar
USER app
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "app.jar"]
