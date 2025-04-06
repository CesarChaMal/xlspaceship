#!/bin/bash

# Arguments
GAME_ID="$1"
HOST="${2:-localhost}"
PORT="${3:-9000}"
PLAYER_ID="${4:-user123}"

# Paths
STATUS_URL="http://$HOST:$PORT/xl-spaceship/user/game/$GAME_ID?player_id=$PLAYER_ID"
STATUS_FILE="game/status_${PLAYER_ID}.json"

# Info message
>&2 echo "📊 [$PLAYER_ID] Getting status for Game: $GAME_ID from $HOST:$PORT..."

# Fetch and extract clean JSON only
RAW_RESPONSE=$(curl -s "$STATUS_URL")
CLEAN_RESPONSE=$(echo "$RAW_RESPONSE" | perl -0777 -ne 'print $1 if /({.*})/s')

# Validate and save only if valid JSON
if echo "$CLEAN_RESPONSE" | jq empty > /dev/null 2>&1; then
    echo "$CLEAN_RESPONSE" > "$STATUS_FILE"
    >&2 echo "📁 JSON saved to $STATUS_FILE"
else
    echo "$CLEAN_RESPONSE" > "$STATUS_FILE.invalid"
    >&2 echo "❌ Invalid JSON content. Not saving."
    exit 1
fi
