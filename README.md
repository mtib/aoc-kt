# Advent of Code 2025

## How to run

```bash
./gradlew fatJar
SESSION="<your session cookie>" java \
  -Dorg.slf4j.simpleLogger.log.dev.mtib.aoc24.AocRunner=INFO \
  -Dorg.slf4j.simpleLogger.showThreadName=false \
  -jar build/libs/aoc24-0.1.0-all.jar <day>
```

## Tools & technologies

- Kotlin
    - Kotlin.coroutines
    - Kotlin.serialisation
- Gradle
- arrow-kt
- lets-plot
- GraphJ
- SLF4J
- KotlinLogging
- JUnit 5
- Kotest

