#!/bin/zsh

export SDKMAN_DIR="$HOME/.sdkman"
[[ -s "$HOME/.sdkman/bin/sdkman-init.sh" ]] && source "$HOME/.sdkman/bin/sdkman-init.sh"

sdk use java 23.0.1-graal
./gradlew fatJar
native-image -jar ./build/libs/aoc-kt-0.24.0-all.jar -o build/libs/aoc-kt -march=native

