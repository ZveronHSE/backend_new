# syntax = docker/dockerfile:experimental
FROM  gradle:7.3.2-jdk17-alpine AS build
COPY --chown=gradle:gradle / /app/
WORKDIR /app

RUN gradle :apigateway:build --no-daemon --stacktrace -x test

FROM openjdk:17-jdk-slim
COPY --from=build /app/apigateway/build/libs/*.jar ./apigateway.jar
EXPOSE 9091
ENTRYPOINT ["java","-jar","apigateway.jar"]