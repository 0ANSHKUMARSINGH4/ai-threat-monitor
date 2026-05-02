# Build Stage
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app

# Optimization for low-memory build agents (Render Free Tier)
ENV MAVEN_OPTS="-Xmx448m -XX:MaxMetaspaceSize=128m -XX:+UseSerialGC"

# Copy files and leverage Docker layer caching
COPY pom.xml .
# Prefetch dependencies to cache them in a separate layer
RUN mvn dependency:go-offline -B

COPY src ./src

# Build the application with single-threading to save memory
RUN mvn clean package -DskipTests -B -T 1

# Run Stage
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copy the compiled JAR
RUN mkdir -p target
COPY --from=builder /app/target/backend-0.0.1-SNAPSHOT.jar ./target/app.jar

# Expose standard port
EXPOSE 8080

# Runtime memory optimization
ENTRYPOINT exec java -Xmx384m -Xss512k -Djava.security.egd=file:/dev/./urandom -Dserver.port=${PORT:-8080} -jar target/app.jar --spring.profiles.active=prod
