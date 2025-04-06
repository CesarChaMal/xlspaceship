#!/bin/bash

echo "🔄 [win] Starting a new game..."
./start_game.sh

GAME_FILE="game/new_game.json"
RESPONSE_FILE="game/salvo_response.json"
STATUS_FILE="game/status.json"

if [[ ! -f "$GAME_FILE" ]]; then
  echo "❌ [win] Game file not found: $GAME_FILE"
  exit 1
fi

GAME_ID=$(jq -r '.game_id' "$GAME_FILE")
PLAYER_ID=$(jq -r '.user_id' "$GAME_FILE")

if [[ "$GAME_ID" == "null" || -z "$GAME_ID" ]]; then
  echo "❌ [win] Invalid or missing game_id in $GAME_FILE"
  exit 1
fi

echo "🎮 [win] Game ID: $GAME_ID | Player ID: $PLAYER_ID"

while true; do
  echo -e "\n🚀 [win] Firing salvo..."

  H1="0x0"
  H2="1x1"
  H3="2x2"

  ./fire_salvo.sh "$GAME_ID" "$PLAYER_ID" "$H1" "$H2" "$H3" > "$RESPONSE_FILE"

  if jq -e '.error' "$RESPONSE_FILE" >/dev/null; then
    echo "❌ [win] Salvo error: $(jq -r '.error' "$RESPONSE_FILE")"
    break
  fi

  echo "✅ [win] Accepting salvo..."
  ./accept_salvo.sh "$GAME_ID" "$PLAYER_ID"

  echo "📊 [win] Getting status..."
  ./get_status.sh "$GAME_ID" > "$STATUS_FILE"

  # Parse summary from status
  TURN=$(jq -r '.turn // "N/A"' "$STATUS_FILE")
  GAME_COMPLETE=$(jq -r '.gameComplete // false' "$STATUS_FILE")
  GAME_WON=$(jq -r '.won // false' "$STATUS_FILE")

  # Salvo results from fire_salvo
  SALVO_RESULTS=$(jq -r '.salvo // {}' "$RESPONSE_FILE")

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
      echo -e "\n🏆 [win] YOU WIN!"
    else
      echo -e "\n💀 [win] YOU LOST!"
    fi
    break
  fi

  sleep 2
done
