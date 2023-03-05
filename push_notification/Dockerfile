# syntax = docker/dockerfile:experimental
FROM gradle:7.3.2-jdk17-alpine AS build
COPY --chown=gradle:gradle / /app/
WORKDIR /app

RUN gradle :push_notification:build --no-daemon --stacktrace -x test

FROM openjdk:17-jdk-slim
COPY --from=build /app/push_notification/build/libs/*.jar ./push_notification.jar
EXPOSE 6060 6061
ENTRYPOINT ["java","-jar","push_notification.jar"]