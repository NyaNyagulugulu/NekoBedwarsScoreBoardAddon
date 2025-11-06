@echo off
echo Compiling BedwarsScoreBoardAddon...
javac -source 1.8 -target 1.8 -cp "lib/*;src/main/java" -d target/classes src/main/java/me/ram/bedwarsscoreboardaddon/**/*.java src/main/java/org/bstats/metrics/Metrics.java
if %errorlevel% == 0 (
    echo Compilation successful!
) else (
    echo Compilation failed!
)
pause