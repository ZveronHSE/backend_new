# syntax = docker/dockerfile:experimental

# Build stage
FROM gradle:8.0.2-jdk17-focal AS build
WORKDIR /app

# Use buildkit's new cache mount to cache gradle dependencies
RUN --mount=type=cache,id=gradle-cache,target=/home/gradle/.gradle gradle --version

COPY --chown=gradle:gradle build.gradle settings.gradle /app/
COPY --chown=gradle:gradle service_registry/build.gradle /app/service_registry/

# Download and cache dependencies before copying the source code
RUN --mount=type=cache,id=gradle-cache,target=/home/gradle/.gradle gradle :service_registry:dependencies --no-daemon --stacktrace

COPY --chown=gradle:gradle / /app/

# Build the project and disable the test task
RUN --mount=type=cache,id=gradle-cache,target=/home/gradle/.gradle gradle :service_registry:build --no-daemon --stacktrace -x test

# Runtime stage
FROM openjdk:17-jdk-slim
COPY --from=build /app/service_registry/build/libs/*.jar ./service_registry.jar

# Add a non-root user for better security
RUN addgroup --system appgroup && adduser --system --ingroup appgroup appuser
USER appuser

# Expose necessary ports
EXPOSE 8761 8762

# Use tini to manage the process and avoid potential issues
ENTRYPOINT ["tini", "--"]
CMD ["java", "-jar", "service_registry.jar"]
