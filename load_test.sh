#!/bin/bash
# Requires: hey (go install github.com/rakyll/hey@latest)
# Usage: ./load_test.sh
# Run this on Linux/WSL for accurate numbers. 
# Do not run on Windows PowerShell.
echo "--- AI Threat Monitor Load Test ---"
echo "Sending 500 requests at 50 concurrency to /api/data"
hey -n 500 -c 50 http://localhost:8080/api/data
