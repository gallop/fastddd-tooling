# Build stage — builds from project root with all common modules
FROM maven:3.9-eclipse-temurin-${javaVersion} AS builder
WORKDIR /project
COPY . .
RUN mvn clean package -DskipTests -pl ${subApp.moduleName} -am -q

# Runtime stage
FROM eclipse-temurin:${javaVersion}-jre-alpine
WORKDIR /app

RUN addgroup -S app && adduser -S app -G app

COPY --from=builder /project/${subApp.moduleName}/target/*.jar app.jar

RUN mkdir -p /app/logs && chown -R app:app /app

USER app

EXPOSE ${subApp.port}

ENTRYPOINT ["java", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-Dspring.profiles.active=prod", \
    "-jar", "app.jar"]
