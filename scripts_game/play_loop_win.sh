#!/bin/bash

echo "🔄 Starting a new game to win..."
./start_game.sh

GAME_ID=$(jq -r '.game_id' new_game.json)
PLAYER_ID=$(jq -r '.user_id' new_game.json)
echo "🎮 Game ID: $GAME_ID | Player ID: $PLAYER_ID"

if [[ "$GAME_ID" == "null" || -z "$GAME_ID" ]]; then
  echo "❌ No valid game ID found!"
  exit 1
fi

while true; do
  # You attack aggressively
  echo -e "\n🚀 Firing salvo to win..."

  # Smart hits - you could replace these with pre-known coordinates if you mock them
  H1="0x0"
  H2="1x1"
  H3="2x2"

  echo "🔥 Attacking: $H1, $H2, $H3"
  RESPONSE_FILE="salvo_response.json"
  curl -s -X PUT "http://localhost:9000/xl-spaceship/user/game/${GAME_ID}/fire" \
    -H "Content-Type: application/json" \
    -d "{\"hits\": [\"$H1\", \"$H2\", \"$H3\"]}" > "$RESPONSE_FILE"

  cat "$RESPONSE_FILE" | jq

  if jq -e '.error' "$RESPONSE_FILE" >/dev/null; then
    echo "❌ Salvo error: $(jq -r '.error' "$RESPONSE_FILE")"
    break
  fi

  GAME_COMPLETE=$(jq -r '.game.gameComplete' "$RESPONSE_FILE")
  GAME_WON=$(jq -r '.game.won' "$RESPONSE_FILE")

  if [[ "$GAME_COMPLETE" == "true" && "$GAME_WON" == "true" ]]; then
    echo -e "\n🏆 YOU WIN!"
    break
  fi

  sleep 2
done
