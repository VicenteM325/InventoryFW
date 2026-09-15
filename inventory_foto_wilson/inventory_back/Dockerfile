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

# Sin esto, la JVM usa UTC (zona horaria por defecto de la imagen base),
# así que todas las marcas de tiempo de negocio (fecha de venta, creación
# de alertas/notificaciones) quedarían 6 horas adelantadas respecto a la
# hora real de Guatemala, donde opera Multiservicios Wilson - visible por
# ejemplo en que "Ventas de hoy" del dashboard no contaría ventas hechas
# en la tarde/noche local.
ENTRYPOINT ["java", "-Duser.timezone=America/Guatemala", "-jar", "app.jar"]