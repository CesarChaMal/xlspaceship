#!/bin/bash

GAME_FILE="scripts_game/new_game_user456.json"

if [[ ! -f "$GAME_FILE" ]]; then
  echo "❌ [user456] Game file not found: $GAME_FILE"
  exit 1
fi

GAME_ID=$(jq -r '.game_id' "$GAME_FILE")

if [[ "$GAME_ID" == "null" || -z "$GAME_ID" ]]; then
  echo "❌ [user456] Game ID not found in $GAME_FILE"
  exit 1
fi

while true; do
  echo -e "\n🛡️ [user456] Waiting for attack..."
  curl -s http://localhost:9001/xl-spaceship/user/game/${GAME_ID} | jq
  sleep 2
done
