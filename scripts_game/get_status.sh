#!/bin/bash

GAME_ID=$(jq -r '.game_id' new_game.json)

curl -X GET http://localhost:9000/xl-spaceship/user/game/$GAME_ID \
  -H "Accept: application/json"
