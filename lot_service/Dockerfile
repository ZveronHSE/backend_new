# syntax = docker/dockerfile:experimental
FROM  gradle:7.3.2-jdk17-alpine AS build
COPY --chown=gradle:gradle / /app/
WORKDIR /app

RUN gradle :lot_service:build --no-daemon --stacktrace -x test

FROM openjdk:17-jdk-slim
COPY --from=build /app/lot_service/build/libs/*.jar ./lot_service.jar
EXPOSE 6060 6061
ENTRYPOINT ["java","-jar","lot_service.jar"]