FROM gradle:9.5.1-jdk21-alpine AS builder
WORKDIR /home/gradle/project
COPY --chown=gradle:gradle . .
RUN gradle bootJar --no-daemon

FROM eclipse-temurin:21-jdk-alpine-3.23
LABEL maintainer="net.rajaonson"
ARG TZ=UTC
ENV TZ=${TZ}
ARG JAVA_OPTS="-Xmx512m -Xms256m"

# Install required packages
RUN apk add --no-cache bash postgresql-client

RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app
COPY --from=builder --chown=spring:spring /home/gradle/project/build/libs/*.jar app.jar

USER spring

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
