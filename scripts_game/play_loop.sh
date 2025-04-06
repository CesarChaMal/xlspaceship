#!/bin/bash

# Ensure directory exists
mkdir -p game

# Start a new game and store result to file
echo "🔄 Starting a new game..."
./start_game.sh > game/new_game.json

if [[ ! -f game/new_game.json ]]; then
  echo "❌ new_game.json was not created!"
  exit 1
fi

# Extract game_id and player_id
GAME_ID=$(jq -r '.game_id' game/new_game.json)
PLAYER_ID=$(jq -r '.user_id' game/new_game.json)
echo "🎮 Game ID: $GAME_ID | Player ID: $PLAYER_ID"

if [[ "$GAME_ID" == "null" || -z "$GAME_ID" ]]; then
  echo "❌ No valid game ID found!"
  exit 1
fi

# Game loop
while true; do
  echo -e "\n🚀 Firing salvo..."
  ./fire_salvo.sh "$GAME_ID" "$PLAYER_ID" > game/salvo_response.json

  if jq -e '.error' game/salvo_response.json >/dev/null; then
    echo "❌ Error: $(jq -r '.error' game/salvo_response.json)"
    break
  fi

  ./accept_salvo.sh "$GAME_ID" "$PLAYER_ID"

  echo -e "\n📊 Getting game status..."
  ./get_status.sh "$GAME_ID" > game/status.json

  # Summary from status
  TURN=$(jq -r '.turn // "N/A"' game/status.json)
  GAME_COMPLETE=$(jq -r '.gameComplete // false' game/status.json)
  GAME_WON=$(jq -r '.won // false' game/status.json)
  SALVO_RESULTS=$(jq -r '.salvo // {}' game/status.json)

  echo ""
  echo "📊 Summary:"
  echo "🌀 Turn:        $TURN"
  echo "✅ Game Over:   $GAME_COMPLETE"
  echo "🏆 You Won?:    $GAME_WON"
  echo ""
  echo "🎯 Salvo Results:"
  echo "$SALVO_RESULTS" | jq .

  if [[ "$GAME_COMPLETE" == "true" ]]; then
    if [[ "$GAME_WON" == "true" ]]; then
      echo -e "\n🏆 [$PLAYER_ID] YOU WIN!"
    else
      echo -e "\n💀 [$PLAYER_ID] YOU LOST!"
    fi
    break
  fi

  sleep 2
done
