# Stufe 1: Produktions-Jar bauen (Vaadin production mode, Frontend gebündelt).
# Vaadin lädt Node.js während des Builds selbst nach, dafür braucht der Build Internetzugang.
FROM eclipse-temurin:21-jdk AS build
WORKDIR /build

# Erst nur Build-Dateien kopieren, damit Docker den Dependency-Download cachen kann
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw -B -ntp -q dependency:go-offline || true

COPY src src
RUN ./mvnw -B -ntp -DskipTests -Dvaadin.productionMode=true package \
    && mv target/*.jar app.jar

# Stufe 2: schlankes Laufzeit-Image, nur JRE + Jar
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --create-home --shell /usr/sbin/nologin app
USER app
COPY --from=build /build/app.jar app.jar

# Render/Fly setzen PORT; Spring Boot liest server.port aus der Umgebungsvariable (siehe application.properties)
ENV PORT=8080
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
