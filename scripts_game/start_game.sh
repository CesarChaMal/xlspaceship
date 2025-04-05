#!/bin/bash

curl -X POST http://localhost:9000/xl-spaceship/protocol/game/new \
  -H "Content-Type: application/json" \
  -d '{
    "player_id": "user123",
    "protocol": {
      "hostname": "localhost",
      "port": 9000
    }
  }' | tee new_game.json
