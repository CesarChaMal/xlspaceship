#!/bin/bash

GAME_ID=$(jq -r '.game_id' new_game.json)

curl -X PUT http://localhost:9000/xl-spaceship/protocol/game/$GAME_ID \
  -H "Content-Type: application/json" \
  -d '{"salvo": ["6x6", "7x7", "8x8"]}'
