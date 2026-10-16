#!/bin/bash
cd "$(dirname "$0")"
mkdir -p out
javac -encoding UTF-8 -d out $(find src -name "*.java") || exit 1
java -Dfile.encoding=UTF-8 -cp "out:lib/sqlite-jdbc.jar" airport.Main
