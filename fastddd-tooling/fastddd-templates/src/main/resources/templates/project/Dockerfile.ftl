# ${msg.proj.dockerfile.buildStageNote}
FROM eclipse-temurin:${javaVersion}-jdk-alpine AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN apk add --no-cache maven \
    && mvn clean package -DskipTests -q

# ${msg.proj.dockerfile.runStageNote}
FROM eclipse-temurin:${javaVersion}-jre-alpine
WORKDIR /app

# ${msg.proj.dockerfile.nonRootUserNote}
RUN addgroup -S app && adduser -S app -G app

COPY --from=builder /app/target/*.jar app.jar

# ${msg.proj.dockerfile.logDirNote}
RUN mkdir -p /app/logs && chown -R app:app /app

USER app

EXPOSE 8080

ENTRYPOINT ["java", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-Dspring.profiles.active=prod", \
    "-jar", "app.jar"]
