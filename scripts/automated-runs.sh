#!/bin/bash

# Fixed number of runs
NUM_RUNS=10

# Variable to store the PID of the current Gradle process
GRADLE_PID=""

# Handle SIGINT (CTRL+C)
cleanup() {
  echo -e "\n[INFO] Received interrupt signal..."
  if [ ! -z "$GRADLE_PID" ]; then
    echo "[INFO] Stopping Java process (PID: $GRADLE_PID)..."
    kill -SIGINT "$GRADLE_PID"
    wait "$GRADLE_PID" 2>/dev/null
  fi
  echo "[INFO] Script terminated"
  exit 1
}

# Register the cleanup function for SIGINT
trap cleanup SIGINT

# Check if policy is provided
if [ $# -ne 1 ]; then
  echo "[INFO] Usage: $0 <policy_number>"
  echo "[INFO] Available policies:"
  echo -e "\t\t\t1: Balanced Policy"
  echo -e "\t\t\t2: Prioritized Policy"
  echo -e "\t\t\t3: FCFS Policy"
  exit 1
fi

# Validate that the policy is a number between 1 and 3
if ! [[ "$1" =~ ^[1-3]$ ]]; then
  echo "[ERROR] Error: Policy must be a number between 1 and 3"
  exit 1
fi

policy=$1
echo "[INFO] Starting $NUM_RUNS executions with policy $policy..."

for ((i=1; i<=$NUM_RUNS; i++)); do
  echo "[INFO] Running execution $i of $NUM_RUNS"
  # Run Gradle in background and capture its PID
  ./gradlew run --args="$policy" & GRADLE_PID=$!
  
  # Wait for the current Gradle process to finish
  wait $GRADLE_PID
  
  # Reset GRADLE_PID
  GRADLE_PID=""
  
  # Add a small delay between runs
  sleep 1
done

echo "[SUCCESS] All executions completed"
