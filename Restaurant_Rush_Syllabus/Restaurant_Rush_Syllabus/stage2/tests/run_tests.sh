#!/usr/bin/env bash
# Usage: tests/run_tests.sh <stage>            run the stage's transcripts and unit tests
#        tests/run_tests.sh <stage> --update   regenerate the expected transcripts
# Run it on the matching git tag (stage-1, stage-2, stage-3): later stages change the rules,
# so earlier transcripts are only valid against the code of their own stage.
set -euo pipefail
cd "$(dirname "$0")/.."
STAGE="${1:?usage: tests/run_tests.sh <stage 1|2|3> [--update]}"
UPDATE="${2:-}"
rm -rf out && mkdir -p out
javac -d out $(find src -name '*.java') 2> >(grep -v '^Picked up' >&2)
fail=0
echo "== unit tests =="
java -cp out restaurant.RuleTests 2> >(grep -v '^Picked up' >&2) || fail=1
echo "== transcript tests (stage $STAGE) =="
for input in tests/stage"$STAGE"/*.in; do
  expected="${input%.in}.expected"
  actual="$(java -cp out restaurant.Main --echo < "$input" 2> >(grep -v '^Picked up' >&2))"
  if [ "$UPDATE" = "--update" ]; then
    printf '%s\n' "$actual" > "$expected"; echo "updated $expected"
  elif diff -u "$expected" <(printf '%s\n' "$actual") > /dev/null; then
    echo "PASS $(basename "$input")"
  else
    echo "FAIL $(basename "$input")"; fail=1
  fi
done
exit $fail
