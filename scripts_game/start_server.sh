#!/bin/bash

LOG_DIR="logs"
mkdir -p "$LOG_DIR"

SERVER_DIR="server_user123"

echo "🚀 Starting Server on port 9000..."

# Optional: copy project to a clean server_user123 folder
mkdir -p "$SERVER_DIR"
cp -r ../app ../conf ../project "$SERVER_DIR"
[[ -d ../public ]] && cp -r ../public "$SERVER_DIR"
cp ../build.sbt "$SERVER_DIR"
cp ../README.md "$SERVER_DIR" 2>/dev/null

# Clean up old targets
rm -rf "$SERVER_DIR/target" "$SERVER_DIR/project/target" "$SERVER_DIR/project/project"

# Start server
(cd "$SERVER_DIR" && sbt -Dhttp.port=9000 run) > "$LOG_DIR/server1.log" 2>&1 &
PID=$!

# Wait until server responds
echo "⏳ Waiting for server to become available on port 9000..."
for i in {1..20}; do
  sleep 1
  STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:9000)
  [[ "$STATUS" == "200" ]] && break
done

# Final status
if [[ "$STATUS" == "200" ]]; then
  echo "✅ Server (9000) is ready!"
else
  echo "❌ Server failed to start. Check $LOG_DIR/server1.log"
fi
