# Builder stage with Maven + JDK 21
# Use gcr.io mirror to avoid intermittent Docker Hub auth errors
FROM mirror.gcr.io/library/maven:3.9.9-eclipse-temurin-21 AS builder

# Ensure external downloads bypass any inherited proxy settings that can block
# access to public Maven repositories during image builds.
ENV HTTP_PROXY="" \
    HTTPS_PROXY="" \
    http_proxy="" \
    https_proxy="" \
    NO_PROXY="" \
    no_proxy=""

WORKDIR /app

# Pre-download dependencies to speed up subsequent builds
COPY apps/backend/pom.xml ./pom.xml
# Note: dependency:go-offline can fail in CI with self-signed certs, but actual build will re-download
# Using || true to allow build to continue - dependencies will be downloaded during the package phase
RUN --mount=type=cache,target=/root/.m2 \
    mvn -U -B -DskipTests \
    -Dmaven.wagon.http.ssl.insecure=true \
    -Dmaven.wagon.http.ssl.allowall=true \
    -Dmaven.wagon.http.ssl.ignore.validity.dates=true \
    -Dmaven.resolver.transport=wagon \
    dependency:go-offline || true

# Copy source code and build
COPY apps/backend/src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn clean package -U -DskipTests -B \
    -Dmaven.wagon.http.ssl.insecure=true \
    -Dmaven.wagon.http.ssl.allowall=true \
    -Dmaven.wagon.http.ssl.ignore.validity.dates=true \
    -Dmaven.resolver.transport=wagon && \
    mv target/*.jar app.jar

# Runtime stage with JRE 21 for smaller image
FROM mirror.gcr.io/library/eclipse-temurin:21-jre-jammy

# Install curl for healthcheck, ffmpeg for audio extraction and unzip for Vosk native library
RUN apt-get update && apt-get install -y --no-install-recommends curl ffmpeg unzip && \
    rm -rf /var/lib/apt/lists/*

# Add Vosk native library for linux-aarch64 to satisfy JNA lookup at runtime
ARG VOSK_VERSION=0.3.45
ARG VOSK_ARCH=linux-aarch64
RUN VOSK_PACKAGE="vosk-${VOSK_ARCH}-${VOSK_VERSION}.zip" && \
    curl -L -o "/tmp/${VOSK_PACKAGE}" "https://github.com/alphacep/vosk-api/releases/download/v${VOSK_VERSION}/${VOSK_PACKAGE}" && \
    unzip -j "/tmp/${VOSK_PACKAGE}" "libvosk.so" -d /usr/local/lib && \
    ldconfig && \
    rm "/tmp/${VOSK_PACKAGE}"

WORKDIR /app

# Copy only the built JAR from builder stage
COPY --from=builder /app/app.jar .

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
