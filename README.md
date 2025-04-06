# play-framework-scala-seed

![](https://playframework.com/assets/images/logos/play_full_color.png)

## Development

To run a local server execute:

```bash
./scripts/server.sh
```

## Tests

To run the complete test suite execute:

```bash
./scripts/tests.sh
```

## Production

### Binary

To build a binary version of the application execute:

```bash
./scripts/build.sh
```

Your package will be ready in `./target/universal/scala-dci-<VERSION>.zip`.

### Staged

If you just want to compile the application in place execute:

```bash
./scripts/production.sh
```

You will find the packaged application in the `./target/universal/stage` directory. In this folder, you can run the application using:

```bash
./bin/scala-dci
```

## Deployment

If you want to deploy the application to Heroku, use the following button:

[![Deploy](https://www.herokucdn.com/deploy/button.png)](https://heroku.com/deploy)

To deploy the application manually later, run:

```bash
./scripts/deploy.sh
```

---

## ✅ Step-by-Step: Run the 2-Player SpaceXL Game Simulation

### 1️⃣ Start the Server

```bash
./scripts_game/start_server.sh
```

This will launch:
- Player 1 server at `localhost:9000`

Wait ~10 seconds for both servers to be ready.

---

### 2️⃣ Terminal 1: Start the Game (as `user123`)

```bash
./scripts_game/start_game_user123.sh
```

This initializes a new game and saves the setup to `new_game_user123.json`.

---
### 2️⃣ Terminal 2: Register user `user456`

```bash
./scripts_game/start_game_user456.sh
```

This initializes a new game and saves the setup to `new_game_user123.json`.

---

### 3️⃣ Terminal 3: Player 1 Attack Loop (`user123`)

```bash
./scripts_game/play_loop_user123.sh
```

This script repeatedly fires random salvos toward Player 2.

---

### 4️⃣ Terminal 4: Player 2 Defense Loop (`user456`)

```bash
./scripts_game/play_loop_user456.sh
```

This script passively waits and accepts incoming salvos from Player 1.

---

## 💡 Optional Scenarios

- To simulate a **win**:
  ```bash
  ./scripts_game/play_loop_win.sh
  ```

- To simulate a **loss** (no attacks, just wait to die):
  ```bash
  ./scripts_game/play_loop_lose.sh
  ```

- To manually check the game status at any time:
  ```bash
  ./scripts_game/get_status.sh | jq
  ```

---

Enjoy battling in space 🚀👾
```
