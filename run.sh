#!/bin/bash
cd "$(dirname "$0")"
mkdir -p build/classes
javac -encoding UTF-8 -d build/classes $(find src/main/java -name "*.java") || exit 1
java -Dfile.encoding=UTF-8 -cp "build/classes:lib/sqlite-jdbc.jar" airport.Main
