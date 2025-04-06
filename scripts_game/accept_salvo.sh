#!/bin/bash

GAME_ID=$1
PLAYER_ID=$2
HOST=${3:-localhost}
PORT=${4:-9000}

if [[ -z "$GAME_ID" || -z "$PLAYER_ID" ]]; then
  echo "❌ Usage: $0 <game_id> <player_id> [host] [port]"
  exit 1
fi

# Generate fake salvo
generate_random_hits() {
  local hits=()
  while [[ ${#hits[@]} -lt 3 ]]; do
    x=$((RANDOM % 10))
    y=$((RANDOM % 10))
    coord="${x}x${y}"
    [[ ! " ${hits[*]} " =~ " ${coord} " ]] && hits+=("$coord")
  done
  echo "${hits[@]}"
}

HITS=$(generate_random_hits)
HIT_ARRAY=$(printf '"%s",' $HITS | sed 's/,$//')
PAYLOAD="{\"hits\": [${HIT_ARRAY}], \"player_id\": \"$PLAYER_ID\"}"


echo "📤 Accepting salvo for Game: $GAME_ID | Player: $PLAYER_ID"
echo "📦 Payload: $PAYLOAD"

curl -s -X PUT "http://$HOST:$PORT/xl-spaceship/protocol/game/$GAME_ID" \
  -H "Content-Type: application/json" \
  -d "$PAYLOAD"
