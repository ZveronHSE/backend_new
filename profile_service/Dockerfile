# syntax = docker/dockerfile:experimental
FROM  gradle:7.3.2-jdk17-alpine AS build
COPY --chown=gradle:gradle / /app/
WORKDIR /app

RUN gradle :profile_service:build --no-daemon --stacktrace -x test

FROM openjdk:17-jdk-slim
COPY --from=build /app/profile_service/build/libs/*.jar ./profile_service.jar
EXPOSE 6060 6061
ENTRYPOINT ["java","-jar","profile_service.jar"]