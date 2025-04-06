#!/bin/bash

echo "🔄 [lose] Starting a new game..."
./start_game.sh

GAME_FILE="game/new_game.json"
STATUS_FILE="game/status.json"

if [[ ! -f "$GAME_FILE" ]]; then
  echo "❌ [lose] Game file not found: $GAME_FILE"
  exit 1
fi

GAME_ID=$(jq -r '.game_id' "$GAME_FILE")
PLAYER_ID=$(jq -r '.user_id' "$GAME_FILE")

if [[ "$GAME_ID" == "null" || -z "$GAME_ID" ]]; then
  echo "❌ [lose] Invalid or missing game_id in $GAME_FILE"
  exit 1
fi

echo "🎮 [lose] Game ID: $GAME_ID | Player ID: $PLAYER_ID"

while true; do
  echo "⏳ [lose] Waiting for opponent to fire..."
  sleep 2

  ./get_status.sh "$GAME_ID" > "$STATUS_FILE"

  if [[ ! -s "$STATUS_FILE" ]]; then
    echo "⚠️  [lose] Status fetch failed or empty!"
    continue
  fi

  # Parse and display status summary
  TURN=$(jq -r '.turn // "N/A"' "$STATUS_FILE")
  GAME_COMPLETE=$(jq -r '.gameComplete // false' "$STATUS_FILE")
  GAME_WON=$(jq -r '.won // false' "$STATUS_FILE")
  SALVO_RESULTS=$(jq -r '.salvo // {}' "$STATUS_FILE")

  echo ""
  echo "📊 Summary:"
  echo "🌀 Turn:        $TURN"
  echo "✅ Game Over:   $GAME_COMPLETE"
  echo "🏆 You Won?:    $GAME_WON"
  echo ""
  echo "🎯 Salvo Results:"
  echo "$SALVO_RESULTS" | jq .

  if [[ "$GAME_COMPLETE" == "true" ]]; then
    echo -e "\n💀 [lose] GAME OVER: You lost!"
    break
  fi

  echo "🟡 [lose] Still in the game... waiting..."
done
