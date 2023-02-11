# syntax = docker/dockerfile:experimental
FROM  gradle:7.6.0-jdk17-focal AS build
COPY --chown=gradle:gradle / /app/
WORKDIR /app

RUN gradle :service_registry:build --no-daemon --stacktrace -x test

FROM openjdk:17-jdk-slim
COPY --from=build /app/service_registry/build/libs/*.jar ./service_registry.jar
EXPOSE 8761 8762
ENTRYPOINT ["java","-jar","service_registry.jar"]