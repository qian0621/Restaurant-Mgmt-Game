# Test notes - Stage 2 (Stage 1 notes: `docs/evidence/stage1/TEST_NOTES.md`)

Base setting, deterministic. Customer choice rule: customer #k orders menu item ((k-1) mod 3), except the Critic,
who always orders the most elaborate dish (Pasta). Arrivals: #1 Regular (T1), **#2 VIP (T4)**, #3 Regular (T7),
**#4 Critic (T10)**, #5 Regular (T13), #6 Regular (T16).

Reproduce on the `stage-2` tag:

    ./build.sh && ./run.sh --echo < tests/stage2/<name>.in     # compare with <name>.expected
    tests/run_tests.sh 2                                       # unit tests + all transcripts

| Run | Input | Result |
|---|---|---|
| Victory | `victory.in` | 4 paid, $58.50, average satisfaction 77.0 -> **VICTORY**. VIP pays $13.50 (Burger -10%); Critic served at 64 -> glowing review, recorded 74 |
| Victory, different choice | `victory_critic_harsh.in` | 5 paid, $73.50, average 73.6 -> **VICTORY**. The manager serves #3 before the Critic's table is ready; the Critic is served at 52 -> harsh review, recorded 42 |
| Defeat | `defeat_critic_walkout.in` | manager stops after turn 9: 2 paid, $22.50, the Critic leaves at turn 18 -> **DEFEAT** |

Unit tests (`restaurant.RuleTests`, 37 checks): all Stage 1 checks plus VIP loss 5 and 10% discount; Critic loss 12,
Pasta preference and +10/-10 review; Regular, VIP and Critic all follow the same order/service flow.
