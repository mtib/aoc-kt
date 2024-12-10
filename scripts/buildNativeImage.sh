#!/bin/zsh

export SDKMAN_DIR="$HOME/.sdkman"
[[ -s "$HOME/.sdkman/bin/sdkman-init.sh" ]] && source "$HOME/.sdkman/bin/sdkman-init.sh"

sdk use java 23.0.1-graal
./gradlew fatJar

JAR_FILE=$(ls ./build/libs/aoc-kt-*-all.jar)
mkdir -p ./build/tmp/native
java -agentlib:native-image-agent=config-merge-dir=./build/tmp/native -Djava.awt.headless=true -jar $JAR_FILE 2024:all --no-plot
native-image \
  -march=native \
  -H:ConfigurationFileDirectories=./build/tmp/native \
  --parallelism=4 \
  --initialize-at-run-time=dev.mtib.aoc.aoc23.days.Day10\$SubGridPosition\$Companion \
  -Djava.awt.headless=true \
  -o build/libs/aoc-kt \
  -jar $JAR_FILE
