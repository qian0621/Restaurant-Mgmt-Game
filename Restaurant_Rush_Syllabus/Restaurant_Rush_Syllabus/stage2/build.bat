@echo off
rem Compiles the game (JDK 17+ required) into .\out
cd /d "%~dp0"
if exist out rmdir /s /q out
mkdir out
dir /s /b src\main\java\*.java > sources.txt
javac -d out @sources.txt
del sources.txt
echo Built into .\out
