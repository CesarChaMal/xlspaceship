#!/bin/bash

HOST=${1:-localhost}
PORT=${2:-9000}
GAME_FILE="game/new_game_user123.json"
OUTPUT_FILE="game/new_game_user456.json"
PLAYER_ID="user456"
FULL_NAME="User 456"
mkdir -p game

if [[ ! -f "$GAME_FILE" ]]; then
  echo "❌ $GAME_FILE not found. Run start_game_user123.sh first."
  exit 1
fi

GAME_ID=$(jq -r '.game_id' "$GAME_FILE")

if [[ "$GAME_ID" == "null" || -z "$GAME_ID" ]]; then
  echo "❌ Game ID not found in $GAME_FILE."
  exit 1
fi

echo "🚀 Joining game $GAME_ID as $PLAYER_ID on $HOST:$PORT..."

curl -s -X POST "http://$HOST:$PORT/xl-spaceship/protocol/game/join" \
  -H "Content-Type: application/json" \
  -d '{
    "player_id": "'"$PLAYER_ID"'",
    "full_name": "'"$FULL_NAME"'",
    "game_id": "'"$GAME_ID"'",
    "protocol": {
      "hostname": "'"$HOST"'",
      "port": '"$PORT"'
    }
  }' | tee "$OUTPUT_FILE" | jq .

echo "✅ $PLAYER_ID joined game: $GAME_ID"
