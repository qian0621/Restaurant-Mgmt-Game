#!/usr/bin/env bash
# Plays the game interactively. Add --echo when piping a script: ./run.sh --echo < tests/stage1/victory.in
cd "$(dirname "$0")"
[ -d out ] || ./build.sh
java -cp out restaurant.Main "$@"
