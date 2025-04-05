#!/bin/bash

LOG_DIR="logs"
mkdir -p "$LOG_DIR"

# Detect if on Windows using Git Bash
if [[ "$(uname -s)" =~ MINGW|MSYS|CYGWIN ]]; then
  WINDOWS=true
else
  WINDOWS=false
fi

# Server directories to avoid sbt lock issues (recommended for Windows)
SERVER1_DIR="server_user123"
SERVER2_DIR="server_user456"

if $WINDOWS; then
  echo "🪟 Detected Windows — using separate directories to avoid sbt lock conflict."

  mkdir -p "$SERVER1_DIR" "$SERVER2_DIR"

  # Symlink the actual project into each
  for DIR in "$SERVER1_DIR" "$SERVER2_DIR"; do
    [[ ! -e "$DIR/app" ]] && cp -r app conf project public build.sbt "$DIR" 2>/dev/null
  done

  echo "🚀 Starting Server 1 (user123) on port 9000..."
  (cd "$SERVER1_DIR" && sbt -Dhttp.port=9000 run) > "$LOG_DIR/server1.log" 2>&1 &
  PID1=$!

  echo "🚀 Starting Server 2 (user456) on port 9001..."
  (cd "$SERVER2_DIR" && sbt -Dhttp.port=9001 run) > "$LOG_DIR/server2.log" 2>&1 &
  PID2=$!
else
  echo "🚀 Starting Server 1 (user123) on port 9000..."
  sbt -Dhttp.port=9000 run > "$LOG_DIR/server1.log" 2>&1 &
  PID1=$!

  echo "🚀 Starting Server 2 (user456) on port 9001..."
  sbt -Dhttp.port=9001 run > "$LOG_DIR/server2.log" 2>&1 &
  PID2=$!
fi

echo "⏳ Waiting for servers to become available..."

# Wait for both servers
for i in {1..20}; do
  sleep 1
  STATUS9000=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:9000)
  STATUS9001=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:9001)
  [[ "$STATUS9000" == "200" && "$STATUS9001" == "200" ]] && break
done

# Final status report
[[ "$STATUS9000" == "200" ]] && echo "✅ Server 1 (9000) is ready!" || echo "❌ Server 1 failed. Check $LOG_DIR/server1.log"
[[ "$STATUS9001" == "200" ]] && echo "✅ Server 2 (9001) is ready!" || echo "❌ Server 2 failed. Check $LOG_DIR/server2.log"
