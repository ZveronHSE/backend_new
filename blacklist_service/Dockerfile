# syntax = docker/dockerfile:experimental
FROM  gradle:7.6.0-jdk17-focal AS build
COPY --chown=gradle:gradle / /app/
WORKDIR /app

RUN gradle :blacklist_service:build --no-daemon --stacktrace -x test

FROM openjdk:17-jdk-slim
COPY --from=build /app/blacklist_service/build/libs/*.jar ./blacklist_service.jar
EXPOSE 6060 6061
ENTRYPOINT ["java","-jar","blacklist_service.jar"]