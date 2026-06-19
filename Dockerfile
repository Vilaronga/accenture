# Build stage
FROM maven:3.9-eclipse-temurin-25 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:resolve dependency:resolve-plugins -q
COPY src ./src
RUN mvn clean package -DskipTests -q

# Runtime stage
FROM eclipse-temurin:25-jre-jammy
WORKDIR /app
RUN apt-get update && apt-get install -y libopencv-dev && rm -rf /var/lib/apt/lists/*
COPY --from=builder /app/target/accenture-*.jar accenture.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "accenture.jar"]
