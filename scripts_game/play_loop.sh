#!/bin/bash

# Start a new game and store result to file
echo "🔄 Starting a new game..."
./start_game.sh > new_game.json

# Extract game_id and player_id from start_game output
GAME_ID=$(jq -r '.game_id' new_game.json)
PLAYER_ID=$(jq -r '.user_id' new_game.json)
echo "🎮 Game ID: $GAME_ID | Player ID: $PLAYER_ID"

if [[ "$GAME_ID" == "null" || -z "$GAME_ID" ]]; then
  echo "❌ No valid game ID found!"
  exit 1
fi

# Game loop
while true; do
  echo -e "\n🚀 Firing salvo..."
  ./fire_salvo.sh "$GAME_ID" > salvo_response.json

  echo "💥 Salvo response:"
  cat salvo_response.json | jq

  # Handle error in response
  if jq -e '.error' salvo_response.json >/dev/null; then
    echo "❌ Error: $(jq -r '.error' salvo_response.json)"
    break
  fi

  # Check if game is complete
  GAME_COMPLETE=$(jq -r '.game.gameComplete' salvo_response.json)
  if [[ "$GAME_COMPLETE" == "true" ]]; then
    echo -e "\n🏁 Game Over!"
    break
  fi

  echo -e "\n📊 Getting game status..."
  ./get_status.sh "$GAME_ID" > status.json
  cat status.json | jq

  sleep 2
done
