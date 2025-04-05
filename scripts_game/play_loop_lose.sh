#!/bin/bash

echo "🔄 Starting a new game to lose..."
./start_game.sh

GAME_ID=$(jq -r '.game_id' new_game.json)
PLAYER_ID=$(jq -r '.user_id' new_game.json)
echo "🎮 Game ID: $GAME_ID | Player ID: $PLAYER_ID"

if [[ "$GAME_ID" == "null" || -z "$GAME_ID" ]]; then
  echo "❌ No valid game ID found!"
  exit 1
fi

while true; do
  echo "⏳ Waiting for opponent fire..."
  sleep 2

  STATUS=$(curl -s -X GET "http://localhost:9000/xl-spaceship/user/game/${GAME_ID}" | tee status.json)

  GAME_COMPLETE=$(jq -r '.gameComplete' status.json)
  NEXT_TURN=$(jq -r '.nextTurn' status.json)

  if [[ "$GAME_COMPLETE" == "true" ]]; then
    echo -e "\n💀 YOU LOST!"
    break
  fi

  echo "⏸️ Still alive... Turn: $NEXT_TURN"
done
