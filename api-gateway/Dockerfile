FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY security-common/ security-common/
RUN ./mvnw dependency:go-offline -pl api-gateway -am -q -B 2>/dev/null || true
COPY api-gateway/ api-gateway/
RUN ./mvnw -pl api-gateway package -DskipTests -q -B

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app
COPY --from=builder /build/api-gateway/target/*.jar app.jar
RUN chown app:app app.jar
USER app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
