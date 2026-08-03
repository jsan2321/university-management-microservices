FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY security-common/ security-common/
COPY audit-common/ audit-common/
RUN ./mvnw dependency:go-offline -pl identity-service -am -q -B 2>/dev/null || true
COPY identity-service/ identity-service/
RUN ./mvnw -pl identity-service package -DskipTests -q -B

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app
COPY --from=builder /build/identity-service/target/*.jar app.jar
RUN chown app:app app.jar
USER app
EXPOSE 8086
ENTRYPOINT ["java", "-jar", "app.jar"]
