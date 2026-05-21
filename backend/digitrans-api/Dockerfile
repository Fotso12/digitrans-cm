# Dockerfile pour le backend Spring Boot DIGITRANS-CM
# Ce Dockerfile compile le projet Maven puis exécute le jar produit.

FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

# Copier les fichiers Maven pour accélérer le cache
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
COPY src src

# Installer Maven et compiler l'application
RUN apk add --no-cache bash curl unzip && \
    ./mvnw -B -DskipTests clean package

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
