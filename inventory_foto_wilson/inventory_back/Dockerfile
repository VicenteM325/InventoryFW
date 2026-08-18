# Compilación con Temurin JDK 25
FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /app

# Permisos de ejecución al wrapper de Maven
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

# Ejecución ligera con Temurin JRE 25
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]