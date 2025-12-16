# Builder stage with JDK 21
FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /app

# Install Maven
RUN apt-get update && \
    apt-get install -y --no-install-recommends maven && \
    rm -rf /var/lib/apt/lists/*

# Copy pom.xml first for dependency layer caching
COPY apps/backend/pom.xml ./pom.xml

# Copy source code
COPY apps/backend/src ./src

# Build the application with BuildKit cache mount for Maven local repository
# Dependencies will be cached in the mount even if layer is rebuilt
# Note: -Dmaven.wagon.http.ssl.insecure=true is a workaround for SSL issues in some Docker environments
RUN --mount=type=cache,target=/root/.m2 \
    mvn clean package -DskipTests -B \
    -Dmaven.wagon.http.ssl.insecure=true \
    -Dmaven.wagon.http.ssl.allowall=true \
    -Dmaven.wagon.http.ssl.ignore.validity.dates=true && \
    mv target/*.jar app.jar

# Runtime stage with JRE 21 for smaller image
FROM eclipse-temurin:21-jre-jammy

# Install curl for healthcheck
RUN apt-get update && apt-get install -y --no-install-recommends curl && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy only the built JAR from builder stage
COPY --from=builder /app/app.jar .

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
