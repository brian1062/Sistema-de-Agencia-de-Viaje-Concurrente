#!/bin/bash

cd ..

# Fixed number of runs
NUM_RUNS=10

# Check if policy is provided
if [ $# -ne 1 ]; then
  echo "[INFO] Usage: $0 <policy_number>"
  echo "[INFO] Available policies:"
  echo -e "\t\t1: Balanced Policy"
  echo -e "\t\t2: Prioritized Policy"
  echo -e "\t\t3: FCFS Policy"
  exit 1
fi

# Validate that the policy is a number between 1 and 3
if ! [[ "$1" =~ ^[1-3]$ ]]; then
  echo "[ERROR] Policy must be a number between 1 and 3"
  exit 1
fi

policy=$1
echo "[INFO] Starting $NUM_RUNS executions with policy $policy..."

for ((i=1; i<=$NUM_RUNS; i++)); do
  echo "[INFO] Running execution $i of $NUM_RUNS"
  ./gradlew run --args="$policy"
  
  # Add a small delay between runs
  sleep 1
done

echo "[SUCCESS] All executions completed"
