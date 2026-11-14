#!/usr/bin/env bash
# Compiles the game (JDK 17+ required) into ./out
set -euo pipefail
cd "$(dirname "$0")"
rm -rf out && mkdir -p out
javac -d out $(find src -name '*.java')
echo "Built into ./out"
