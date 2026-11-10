# Test notes - Stage 3 (earlier notes: `docs/evidence/stage1/` and `docs/evidence/stage2/`)

Base setting, deterministic. Menu: Salad, Burger, Pasta and the **Salad + Pasta Combo ($24.30, 3 units)**.
Customer choice rule: customer #k orders offering ((k-1) mod 4) of [Salad, Burger, Pasta, Combo]; the Critic always
orders the most elaborate offering (now the Combo). Arrivals: #1 Regular (T1), #2 VIP (T4), #3 Regular (T7),
#4 Critic (T10), #5 Regular (T13), #6 Regular (T16).

Each turn the input has: a Happy Hour line (`skip` or `hh`) while Happy Hour is unused, then Host, Waiter, Chef.

Reproduce on the `stage-3` tag:

    ./build.sh && ./run.sh --echo < tests/stage3/<name>.in     # compare with <name>.expected
    tests/run_tests.sh 3                                       # unit tests + all transcripts

| Run | Input | Result |
|---|---|---|
| Victory | `victory.in` | 5 paid, $68.94, average 82.4 -> **VICTORY**. Happy Hour on at T11: Critic's Combo billed $19.44, Critic recovers to 100 twice, served at 76 -> glowing review 86 |
| Victory, other choice | `victory_no_happy_hour.in` | 5 paid, $73.80, average 73.6 -> **VICTORY**. Same play without Happy Hour: +$4.86 revenue but the Critic is served at 52 -> harsh review 42 |
| Defeat | `defeat_neglect.in` | manager stops after turn 9: 2 paid, $22.50 -> **DEFEAT** |
| Feature demo | `hh_demo.in` | Host + Waiter seat two customers in one turn; clash and wrong-role choices rejected; VIP Burger in Happy Hour = $10.80 (brief's example); Salad ordered after Happy Hour ends = full $9.00 |

Unit tests (`restaurant.RuleTests`, 59 checks) = all Stage 1 and 2 checks (so earlier rules still hold) plus: combo
price/units/rounding; combo choice rule; Happy Hour discount order (VIP then Happy Hour), price lock-in, duration 2
turns, once per game, +15 recovery capped at 100 and only for customers still waiting; Host and Waiter seating
different customers in one turn, clash on the same customer, and the table limit.
