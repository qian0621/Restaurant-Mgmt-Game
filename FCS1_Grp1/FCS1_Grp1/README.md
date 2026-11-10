# Restaurant Rush - SC2002 Group Assignment

Turn-based, command-line restaurant management game in Java (JDK 17+, no external libraries, fully offline).
You are the manager: staff time is limited, so you decide whom to seat, which order to take or serve first,
and what the Chef cooks.

- **Repository URL:** `<PASTE GITHUB URL HERE>`  (push with `git remote add origin <url> && git push origin main --tags`)
- **Group:** `<LAB GROUP / GROUP NUMBER>`  |  **Members:** `<NAME 1>`, `<NAME 2>`, ...
- **Report:** `docs/Report.pdf`  |  **Video:** `<LINK OR FILE NAME>`  |  **Video plan:** `docs/VIDEO_SCRIPT.md`

## 1. Build and run

    ./build.sh            # Windows: build.bat      (javac into ./out)
    ./run.sh              # Windows: run.bat        (plays interactively)
    ./run.sh --echo < tests/stage3/victory.in       # replay a recorded run (prints the typed commands)
    tests/run_tests.sh 3                            # unit tests + transcript tests for a stage

Commands each turn (a Host line appears only from Stage 3, a Happy Hour prompt `hh` / `skip` while unused):

| Role | Commands |
|---|---|
| Host (Stage 3) | `seat <n>`, `wait` |
| Waiter | `seat <n>`, `order <n>`, `serve <n>`, `wait` |
| Chef | `cook <n>`, `wait` |
| Any prompt | `help`, `quit` |

`<n>` is the customer number shown on screen. An invalid choice explains why and asks again; it never uses up the
staff task. The Cashier works automatically.

**Customer food rule (deterministic):** customer #k orders offering `(k-1) mod (menu size)` of the menu. The Critic
always orders the most elaborate offering. Stage 3 menu: Salad, Burger, Pasta, Salad + Pasta Combo ($24.30, 3 units).
**Arrivals (Stage 2/3):** #1 Regular T1, #2 VIP T4, #3 Regular T7, #4 Critic T10, #5 Regular T13, #6 Regular T16.
**Critic behaviour:** loses 12/turn, orders the most elaborate dish, and writes a review when paying: satisfaction >= 60
gives +10 (max 100), otherwise -10 (min 0); the adjusted value counts in the average.

The stage-N code is the tag `stage-N`. `git checkout stage-1` (or 2, 3) gives a runnable version of that stage;
`docs/TEST_NOTES.md` and `docs/evidence/` hold the reproducible victory/defeat runs of each stage.

## 2. Stage comparisons (Section 5 evidence)

Replace `<REPO>` with the repository URL after pushing.

### Stage 1 -> Stage 2 ("Customer behaviour")  [compare](<REPO>/compare/stage-1...stage-2)  (commit `dc9f640`)

| Change | File | Why |
|---|---|---|
| New | `model/VIPCustomer.java` | 5 loss/turn, bill x0.9 via `billFor` |
| New | `model/CriticCustomer.java` | 12 loss/turn, most-elaborate-dish preference, review at payment |
| Changed | `model/Customer.java` | Added two overridable hooks `billFor(item)` and `satisfactionAtPayment()` (defaults keep Regular behaviour); `pay()` uses the hook |
| Changed | `model/CustomerType.java` | Registered `VIP` and `CRITIC` in the factory enum (setup change) |
| Changed | `model/Restaurant.java` | `quote()` asks the customer for its bill instead of reading the item price |
| Changed | `staff/Cashier.java` | Payment message shows a review adjustment |
| Changed | `control/GameConfig.java` | Arrival schedule: turn 4 = VIP, turn 10 = Critic (still six arrivals) |
| Moved | `tests/stage1/*` -> `docs/evidence/stage1/*` | Stage 1 transcripts only valid on their own tag |
| Deleted | nothing | |

`GameController` and every `StaffTask` are **unchanged**: customer differences are dispatched through the `Customer`
objects, not through type checks.

### Stage 2 -> Stage 3 ("Host, ComboMeal, Happy Hour")  [compare](<REPO>/compare/stage-2...stage-3)  (commit `42caf7c`)

| Change | File | Why |
|---|---|---|
| New | `staff/Host.java` | Seats one queued customer per turn; reuses `SeatCustomerTask` unchanged |
| New | `model/ComboMeal.java` | `extends MenuItem`; units = sum, price = 90% of the components rounded to the cent |
| New | `model/HappyHour.java` | Once per game, 2 turns, 20% off, +15 recovery; remaining duration displayed |
| Changed | `model/Restaurant.java` | Owns a `HappyHour`; `quote()` = Happy Hour applied after the customer's own bill (VIP first, then Happy Hour); recovery method; `endTurn()` |
| Changed | `model/Customer.java` | Added package-private `recoverSatisfaction(points)` (capped at 100) |
| Changed | `control/GameConfig.java` | Menu now includes the combo; `newMenu()` |
| Changed | `control/GameController.java` | Host added to the roster; Happy Hour offer/recovery calls; this is the only place the turn flow had to change |
| Changed | `ui/ConsoleUI.java` | Happy Hour prompt and status line |
| Deleted | nothing | |

Stable Stage 1/2 rules were not rewritten: the 59 unit checks of Stage 3 include all Stage 1 and Stage 2 checks.

## 3. Video timestamps (about 15 minutes)

Segment plan with planned timestamps. **Update the times and speakers after recording** (see `docs/VIDEO_SCRIPT.md`).

| # | Segment | Planned time | Presenter |
|---|---|---|---|
| 1 | Game launching from the submitted project | 00:00 | `<member>` |
| 2 | Meaningful manager choices, incl. staff constrained | 02:00 | `<member>` |
| 3 | A dish READY -> SERVED -> payment | 04:30 | `<member>` |
| 4 | VIP / Critic behaviour | 06:30 | `<member>` |
| 5 | Stage 3 feature (Host, Combo, Happy Hour) | 08:30 | `<member>` |
| 6 | One victory and one defeat | 10:30 | `<member>` |
| 7 | Stage tag comparisons and diagrams | 12:30 | `<member>` |

## 4. Package layout

    restaurant/model    entities: Restaurant, Customer (+Regular, VIP, Critic), Order, Table, Menu, MenuItem, ComboMeal, HappyHour ...
    restaurant/staff    Waiter, Chef, Host, Cashier and the StaffTask commands (seat, order, serve, cook)
    restaurant/control  GameController (six-step turn), TurnPlan, GameConfig, GameResult
    restaurant/ui       ConsoleUI, ManagerCommand (display and input only - no game rules)
