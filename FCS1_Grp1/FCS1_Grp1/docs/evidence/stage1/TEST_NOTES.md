# Test notes - Stage 1

All runs use the base setting and are fully deterministic (no randomness; customer #k orders menu
item ((k-1) mod 3): Salad, Burger, Pasta, Salad, Burger, Pasta).

Reproduce any run on the `stage-1` tag:

    ./build.sh
    ./run.sh --echo < tests/stage1/<name>.in      # compare with tests/stage1/<name>.expected
    tests/run_tests.sh 1                          # runs everything and diffs the transcripts

Input files hold two lines per turn (Waiter, then Chef); lines starting with `#` are comments.

| Run | Input | Result |
|---|---|---|
| Victory | `victory.in` | 5 paid, $66.00, average satisfaction 79.2 -> **VICTORY** |
| Defeat (neglect) | `defeat_neglect.in` | customers #1-#3 handled, then the manager stops: 3 paid, $42.00 -> **DEFEAT** (paid-count and revenue targets missed) |
| Defeat (idle) | `defeat_idle.in` | nothing assigned: 0 paid, 2 unhappy departures -> **DEFEAT** |
| Invalid input | `invalid_input.in` | wrong state, unknown customer, non-number, wrong role, gibberish: each explained, staff task not consumed |

Unit tests (`restaurant.RuleTests`, 30 checks) cover: integer-cent money; READY-then-SERVED next turn;
one full eating turn then payment the turn after; cashier cannot charge early; abandonment cancels
the order and frees the table; served customers lose nothing; Chef can cook an order the Waiter takes
the same turn; Pasta needs two units; no free table blocks seating; N/A average before the first payment.
