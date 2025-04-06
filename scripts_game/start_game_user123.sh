#!/bin/bash

HOST=${1:-localhost}
PORT=${2:-9000}
GAME_FILE="game/new_game_user123.json"
PLAYER_ID="user123"
FULL_NAME="User 123"
mkdir -p game

echo "🚀 Starting game for $PLAYER_ID on $HOST:$PORT..."

curl -s -X POST "http://$HOST:$PORT/xl-spaceship/protocol/game/new" \
  -H "Content-Type: application/json" \
  -d '{
    "player_id": "'"$PLAYER_ID"'",
    "full_name": "'"$FULL_NAME"'",
    "game_id": "",
    "protocol": {
      "hostname": "'"$HOST"'",
      "port": '"$PORT"'
    }
  }' | tee "$GAME_FILE" | jq .

GAME_ID=$(jq -r '.game_id' "$GAME_FILE")

if [[ "$GAME_ID" == "null" || -z "$GAME_ID" ]]; then
  echo "❌ Failed to create game for $PLAYER_ID"
  exit 1
fi

echo "✅ Game ID created: $GAME_ID"
