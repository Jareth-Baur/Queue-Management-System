# Queueing System Made in JavaFX

A simple queue management desktop application built with Java and JavaFX. This project demonstrates a GUI-driven queueing system for small offices, clinics, or service counters. It includes user interfaces for ticket generation, counter management, and basic reporting.

## Features
- Generate and print queue tickets
- Multiple service counters with call/hold/recall actions
- Simple dashboard showing waiting and served counts
- Persist queue state between runs (file-based or lightweight DB — depending on implementation)
- Clean JavaFX-based UI with FXML separation

## Project structure (typical)
- src/main/java — application source code
- src/main/resources — FXML, images, CSS
- src/test/java — unit tests
- build/ or target/ — build outputs (Gradle/Maven)
- README.md — project documentation

## Prerequisites
- Java 11+ (Java 17 recommended)
- JavaFX SDK matching your JDK (if not using a bundled JDK+JavaFX)
- Maven or Gradle (depending on project build tool)
- Optional: an IDE like VS Code, IntelliJ IDEA, or Eclipse

## Build & run (examples)

Maven:
- Build:
  - mvn clean package
- Run (if JavaFX SDK required on Windows):
  - java --module-path "C:\path\to\javafx-sdk\lib" --add-modules javafx.controls,javafx.fxml -jar target\<artifact>-jar-with-dependencies.jar

Gradle:
- Build:
  - gradlew clean build
- Run from Gradle:
  - gradlew run

Run from IDE:
- Import project as Maven/Gradle project.
- Make sure VM options include JavaFX module path when launching the application:
  - --module-path "C:\path\to\javafx-sdk\lib" --add-modules javafx.controls,javafx.fxml

Notes:
- Replace C:\path\to\javafx-sdk with your actual JavaFX SDK installation path.
- If project is configured to bundle JavaFX, module-path options may not be needed.

## Configuration
- Check src/main/resources or a config/ directory for property files (port, persistence path, branding).
- Logging configuration may be under resources (logback.xml or logging.properties).

## Testing
- Unit tests (if present) can be run with:
  - mvn test
  - gradlew test

## Contributing
- Fork the repository, create a feature branch, and open a pull request.
- Keep UI logic separated from business logic for easier testing.
- Add unit tests for non-UI components.

## Troubleshooting
- Common error: JavaFX runtime components not found — ensure --module-path and --add-modules are set when using external JavaFX.
- Check console/log output for stack traces and missing resource paths.

## License
- Add project license (e.g., MIT, Apache-2.0) in LICENSE file.

## Contact
- Add maintainer or project contact information here.
