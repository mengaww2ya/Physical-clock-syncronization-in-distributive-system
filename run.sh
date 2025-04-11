#!/bin/bash

# Compile the Java files
echo "Compiling Java files..."
mkdir -p target/classes
javac -d target/classes -cp "lib/*" $(find src -name "*.java")

# Run the application
echo "Running the application..."
java -cp "target/classes:lib/*" clocksync.ClockSyncApp 