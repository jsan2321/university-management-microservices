FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline -pl audit-service -am -q -B 2>/dev/null || true
COPY audit-service/ audit-service/
RUN ./mvnw -pl audit-service package -DskipTests -q -B

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app
COPY --from=builder /build/audit-service/target/*.jar app.jar
RUN chown app:app app.jar
USER app
EXPOSE 8087
ENTRYPOINT ["java", "-jar", "app.jar"]
