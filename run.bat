@echo off
cd /d "%~dp0"
if not exist build\classes mkdir build\classes
dir /s /b src\main\java\*.java > sources.txt
javac -encoding UTF-8 -d build\classes @sources.txt
set "compile_error=%ERRORLEVEL%"
del sources.txt
if not "%compile_error%"=="0" (echo Compilation failed & pause & exit /b %compile_error%)
java -Dfile.encoding=UTF-8 -cp "build\classes;lib\sqlite-jdbc.jar" airport.Main
