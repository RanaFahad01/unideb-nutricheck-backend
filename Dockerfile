# ---------- Build stage ----------
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# ---------- Run stage ----------
FROM eclipse-temurin:21-jdk

# Install Tesseract + Leptonica dependencies
RUN apt-get update && \
    apt-get install -y tesseract-ocr libtesseract-dev libleptonica-dev && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
