
#!/bin/bash

curl -X OPTIONS http://localhost:9000/xl-spaceship/protocol/game/new \
  -H "Access-Control-Request-Method: POST" \
  -H "Origin: http://localhost"
