#!/bin/bash

mkdir -p scripts_game

GAME_ID=$(jq -r '.game_id' scripts_game/new_game_user123.json)

if [[ "$GAME_ID" == "null" || -z "$GAME_ID" ]]; then
  echo "❌ Game ID not found. Run start_game_user123.sh first."
  exit 1
fi

curl -s -X POST http://localhost:9001/xl-spaceship/protocol/game/new \
  -H "Content-Type: application/json" \
  -d "{
    \"player_id\": \"user456\",
    \"protocol\": {
      \"hostname\": \"localhost\",
      \"port\": 9001
    },
    \"game_id\": \"${GAME_ID}\"
  }" | tee scripts_game/new_game_user456.json | jq .
