#!/bin/bash

GAME_ID=$1
if [ -z "$GAME_ID" ]; then
  echo "❌ Missing game ID!"
  exit 1
fi

# Random hits
H1="$((RANDOM % 16))x$((RANDOM % 16))"
H2="$((RANDOM % 16))x$((RANDOM % 16))"
H3="$((RANDOM % 16))x$((RANDOM % 16))"

curl -s -X PUT "http://localhost:9000/xl-spaceship/user/game/$GAME_ID/fire" \
  -H "Content-Type: application/json" \
  -d "{\"hits\": [\"$H1\", \"$H2\", \"$H3\"]}"
