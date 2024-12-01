# Advent of Code 2025

## How to run

```bash
./gradlew fatJar
SESSION="<your session cookie>" java \
  -Dorg.slf4j.simpleLogger.log.dev.mtib.aoc24.AocRunner=INFO \
  -Dorg.slf4j.simpleLogger.showThreadName=false \
  -Djava.awt.headless=true \
  -jar build/libs/aoc24-0.1.0-all.jar all
```

Alternatively, to run a specific day or set of days:

```bash
# run day 5
java -jar build/libs/aoc24-0.1.0-all.jar 5

# run day 1, 3, 24
java -jar build/libs/aoc24-0.1.0-all.jar 1 3 24

# run all days
java -jar build/libs/aoc24-0.1.0-all.jar all

# run the latest day available
java -jar build/libs/aoc24-0.1.0-all.jar latest
```

## How to use

1. Create a file like [AocDay01.kt](src/main/kotlin/dev/mtib/aoc24/days/AocDay01.kt) matching the day number (
   e.g. `AocDay05.kt` for day 5).
2. Overwrite the `part1` and `part2` methods with your implementation, returning the solution as a String.
3. Run the application with the command above, replacing `<day>` with the day number, or just running with `latest`, to
   run the most recent day.

If provided with the `SESSION` in your environment variables (matching the cookie value from the AoC website), the
application will automatically fetch the input for the given day.
This will run the parts of the day you implemented and print the results.
Then run 10s worth of benchmarks for both parts and print their results.
Inside `src/main/resources` you will find the input files for each day you've run, as well as plots for the benchmarks,
as well as a results.json file with all results ever recorded.
(You may want to mark a particular run as `"verified": true` in the results file to detect regressions (different
results) in future runs.)

## Tools & technologies

### Foundation

- Kotlin
    - Kotlin.coroutines
    - Kotlin.serialisation
- arrow-kt: Functional programming and stdlib extensions
- lets-plot: Data visualisation
- GraphJ: Graph representations and algorithms
- oj!Algo: MIP solver and fast math

### Build tools

- Gradle

### Misc

- SLF4J
- KotlinLogging
- JUnit 5
- Kotest
