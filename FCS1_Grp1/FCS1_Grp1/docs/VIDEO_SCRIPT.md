# Video script (about 15 minutes, one group video)

I could not record this for you: the brief requires each member to explain a part they understand. This is a
ready-to-run plan. Use a terminal with large font, at the repo root. Fill in presenter names and real timestamps
in README section 3 afterwards.

| # | Time | Do on screen | Key points to say |
|---|---|---|---|
| 1 | 0:00-2:00 | `git checkout stage-3 && ./build.sh && ./run.sh` | Offline Java, no libraries; base setting 18 turns, 2 tables; objective (4 paid, $45, avg 60) |
| 2 | 2:00-4:30 | Play turns 1-4 by hand: queue two customers, show Waiter has one task so seating vs serving must be chosen; type an invalid command (`serve 1` too early) | Invalid choice explains why and costs no task; Waiter acts before Chef |
| 3 | 4:30-6:30 | `./run.sh --echo < tests/stage3/hh_demo.in` or play: follow one dish | Order READY in turn N, SERVED earliest turn N+1, eating turn, READY_TO_PAY, paid next turn (`TurnPlan`, `Order` states) |
| 4 | 6:30-8:30 | `git checkout stage-2`, run `docs/evidence/stage2/victory.in` (`tests/stage2/` on that tag) | VIP: 5 loss, $13.50 burger; Critic: 12 loss, Pasta, review +/-10; show `Customer.billFor` override - no type checks in controller |
| 5 | 8:30-10:30 | `git checkout stage-3`, `tests/stage3/hh_demo.in` | Host + Waiter seat two in one turn; Combo $24.30 3 units; Happy Hour: VIP burger $10.80, 2-turn countdown, once per game |
| 6 | 10:30-12:30 | `tests/stage3/victory.in` then `tests/stage3/defeat_neglect.in` | Victory 5 paid $68.94 avg 82.4; defeat 2 paid $22.50; compare `victory_no_happy_hour.in` (choice changes result) |
| 7 | 12:30-15:00 | `git tag`, `git diff --stat stage-1 stage-2`, `stage-2 stage-3`; open `docs/Report.pdf` class and sequence diagrams | Which files changed and why; encapsulation / polymorphism / composition annotations |

Tip: for a smooth recording of segments 3-6 use the recorded inputs; type the first few turns live.
