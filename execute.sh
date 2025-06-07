#!/usr/bin/env bash
set -e
mkdir -p bin
javac -d bin src/*.java
java -cp bin GraphingCalculator

