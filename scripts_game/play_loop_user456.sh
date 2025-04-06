#!/bin/bash

GAME_FILE="game/new_game_user456.json"
STATUS_FILE="game/status_user456.json"
RESPONSE_FILE="game/salvo_user456.json"

HOST="${1:-localhost}"
PORT="${2:-9000}"

if [[ ! -f "$GAME_FILE" ]]; then
  echo "❌ [user456] Game file not found: $GAME_FILE"
  exit 1
fi

GAME_ID=$(jq -r '.game_id' "$GAME_FILE")
PLAYER_ID=$(jq -r '.user_id' "$GAME_FILE")

if [[ "$GAME_ID" == "null" || -z "$GAME_ID" ]]; then
  echo "❌ [$PLAYER_ID] Game ID not found in $GAME_FILE"
  exit 1
fi

echo "🎮 [$PLAYER_ID] Starting play loop | GAME_ID: $GAME_ID"

while true; do
  echo -e "\n🚀 [$PLAYER_ID] Firing salvo..."
  ./fire_salvo.sh "$GAME_ID" "$PLAYER_ID" "$HOST" "$PORT" > "$RESPONSE_FILE"

  if jq -e '.error' "$RESPONSE_FILE" >/dev/null; then
    echo "❌ [$PLAYER_ID] Salvo error: $(jq -r '.error' "$RESPONSE_FILE")"
    break
  fi

  echo "✅ [$PLAYER_ID] Accepting salvo..."
  ./accept_salvo.sh "$GAME_ID" "$PLAYER_ID" "$HOST" "$PORT"

  echo "📊 [$PLAYER_ID] Getting status..."
  ./get_status.sh "$GAME_ID" "$HOST" "$PORT" "$PLAYER_ID" > "$STATUS_FILE"

  TURN=$(jq -r '.turn // "N/A"' "$STATUS_FILE")
  GAME_COMPLETE=$(jq -r '.gameComplete // false' "$STATUS_FILE")
  GAME_WON=$(jq -r '.won // false' "$STATUS_FILE")
  SALVO_RESULTS=$(jq -r '.salvo // {}' "$RESPONSE_FILE")

  echo ""
  echo "📊 Summary:"
  echo "🌀 Turn:        $TURN"
  echo "✅ Game Over:   $GAME_COMPLETE"
  echo "🏆 You Won?:    $GAME_WON"
  echo ""
  echo "🎯 Salvo Results:"
  echo "$SALVO_RESULTS" | jq .

  ./render_board.sh "$STATUS_FILE" "🛸 [$PLAYER_ID] Your Board:" "self"
  ./render_board.sh "$STATUS_FILE" "🧿 [$PLAYER_ID] Opponent Board:" "opponent"

  if [[ "$GAME_COMPLETE" == "true" ]]; then
    if [[ "$GAME_WON" == "true" ]]; then
      echo "🏆 [$PLAYER_ID] YOU WIN!"
    else
      echo "💀 [$PLAYER_ID] YOU LOST!"
    fi
    break
  fi

  sleep 2
done
