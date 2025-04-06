#!/bin/bash

LOG_DIR="logs"
mkdir -p "$LOG_DIR"

# Detect if on Windows using Git Bash
if [[ "$(uname -s)" =~ MINGW|MSYS|CYGWIN ]]; then
  WINDOWS=true
else
  WINDOWS=false
fi

SERVER1_DIR="server_user123"
SERVER2_DIR="server_user456"

copy_project() {
  local TARGET_DIR=$1
  echo "📁 Preparing $TARGET_DIR"
  mkdir -p "$TARGET_DIR"

  cp -r ../app ../conf ../project "$TARGET_DIR"
  [[ -d ../public ]] && cp -r ../public "$TARGET_DIR"
  cp ../build.sbt "$TARGET_DIR"
  cp ../README.md "$TARGET_DIR" 2>/dev/null

  # Clean up old targets
  rm -rf "$TARGET_DIR/target" "$TARGET_DIR/project/target" "$TARGET_DIR/project/project"
}

if $WINDOWS; then
  echo "🪟 Detected Windows — using separate directories to avoid sbt lock conflict."
  copy_project "$SERVER1_DIR"
  copy_project "$SERVER2_DIR"

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
