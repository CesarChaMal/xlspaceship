#!/bin/bash

GAME_FILE="scripts_game/new_game_user123.json"

if [[ ! -f "$GAME_FILE" ]]; then
  echo "❌ [user123] Game file not found: $GAME_FILE"
  exit 1
fi

GAME_ID=$(jq -r '.game_id' "$GAME_FILE")

if [[ "$GAME_ID" == "null" || -z "$GAME_ID" ]]; then
  echo "❌ [user123] Game ID not found in $GAME_FILE"
  exit 1
fi

while true; do
  echo -e "\n🎯 [user123] Firing salvo..."
  H1="$((RANDOM % 16))x$((RANDOM % 16))"
  H2="$((RANDOM % 16))x$((RANDOM % 16))"
  H3="$((RANDOM % 16))x$((RANDOM % 16))"

  curl -s -X PUT "http://localhost:9000/xl-spaceship/user/game/${GAME_ID}/fire" \
    -H "Content-Type: application/json" \
    -d "{\"hits\": [\"$H1\", \"$H2\", \"$H3\"]}" | jq

  sleep 2
done
