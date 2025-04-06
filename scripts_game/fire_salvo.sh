#!/bin/bash

# Generate 3 unique random coordinates (e.g., 0x1, 4x3, 6x7)
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

GAME_ID=$1
PLAYER_ID=$2
HOST=${3:-localhost}
PORT=${4:-9000}
HITS="${5:-$(generate_random_hits)}"

if [[ -z "$GAME_ID" || -z "$PLAYER_ID" ]]; then
  echo "❌ Usage: $0 <game_id> <player_id> [host] [port] [hits...]"
  exit 1
fi

# Convert hits to JSON array
HIT_ARRAY=$(printf '"%s",' $HITS | sed 's/,$//')
PAYLOAD="{\"hits\": [${HIT_ARRAY}], \"player_id\": \"$PLAYER_ID\"}"


RESPONSE=$(curl -s -X PUT "http://$HOST:$PORT/xl-spaceship/protocol/game/$GAME_ID" \
  -H "Content-Type: application/json" \
  -d "$PAYLOAD")

echo "$RESPONSE" | jq . || echo "❌ fire_salvo.sh: Invalid JSON:" && echo "$RESPONSE"
