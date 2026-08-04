FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build
COPY . .
RUN ./mvnw -pl identity-service -am package -DskipTests -q -B

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app
COPY --from=builder /build/identity-service/target/*.jar app.jar
RUN chown app:app app.jar
USER app
EXPOSE 8086
ENTRYPOINT ["java", "-jar", "app.jar"]
