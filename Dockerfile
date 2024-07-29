# Verwende das Node.js-Image als Basis, um die Anwendung zu bauen
FROM node:14 as build-frotend

# Setze das Arbeitsverzeichnis im Container
WORKDIR /app

# Kopiere package.json und package-lock.json in das Arbeitsverzeichnis
COPY frontend/package*.json ./

# Installiere Abhängigkeiten
RUN npm install

# Kopiere den Rest des Anwendungsquellcodes in das Arbeitsverzeichnis
COPY frontend/* .

# Baue die Anwendung für die Produktion
RUN npm run build

FROM gradle:latest as build-backend

# Use an official Gradle image with JDK 21 to run Gradle tasks


# Set the working directory inside the container
WORKDIR /home/gradle/project

# Copy the entire project to the working directory
COPY backend/* .


# Kopiere die gebauten Dateien aus der vorherigen Stage
COPY --from=build-frotend /app/dist/* /home/gradle/project/src/main/resources/static

# Run the Gradle build task
RUN gradle clean bootJar

# Use an official OpenJDK 21 image to run the application
FROM eclipse-temurin:21-jre-alpine

# Copy the built jar from the build stage
COPY --from=build /home/gradle/project/build/libs/*.jar app.jar

# Expose port 8080
EXPOSE 8080

# Set the entry point to run the application
ENTRYPOINT ["java", "-jar", "/app.jar"]

