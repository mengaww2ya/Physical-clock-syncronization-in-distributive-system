#!/bin/bash

echo "=============================================================="
echo "        PHYSICAL CLOCK SYNCHRONIZATION SIMULATOR              "
echo "=============================================================="
echo "This application demonstrates multiple clock synchronization"
echo "algorithms including Cristian's, Berkeley, and NTP-like"
echo "implementations. You can select which algorithm to use at startup."
echo "=============================================================="

# Create directory for compiled classes
mkdir -p target/classes

# Compile the Simple application
echo -e "\nCompiling SimpleApp..."
javac -d target/classes src/clocksync/SimpleApp.java

if [ $? -eq 0 ]; then
    # Run the application
    echo -e "\nRunning the clock synchronization application..."
    java -cp target/classes clocksync.SimpleApp
else
    echo -e "\nCompilation failed. Please fix any errors and try again."
    exit 1
fi 