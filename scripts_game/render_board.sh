#!/bin/bash

STATUS_FILE="$1"
LABEL="$2"
BOARD_TYPE="${3:-self}"  # "self" or "opponent"

if [[ ! -f "$STATUS_FILE" ]]; then
  echo "❌ Status file not found: $STATUS_FILE"
  exit 1
fi

# Extract and display the board
echo -e "\n$LABEL"

for y in {0..15}; do
  LINE=""
  for x in {0..15}; do
	STATUS=$(jq -r ".${BOARD_TYPE}.board.rows[$y].columns[$x].status" "$STATUS_FILE")
    case "$STATUS" in
      ".") SYMBOL="." ;;        # untouched
      "X") SYMBOL="💥" ;;       # hit
      "-") SYMBOL="❌" ;;       # miss
      "*" ) SYMBOL="🔵" ;;  	# unhit ship part
      *) SYMBOL="?" ;;          # unknown / invalid
    esac
    LINE+="$SYMBOL "
  done
  echo "$LINE"
done
