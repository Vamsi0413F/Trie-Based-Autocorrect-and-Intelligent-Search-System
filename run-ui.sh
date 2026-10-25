#!/bin/bash
# ==============================================================================
# Trie-Based Autocorrect & Intelligent Search System - Web UI & Server Launcher
# ==============================================================================

set -e

PORT=${1:-8080}
DICT=${2:-"data/words_alpha.txt"}

# Locate Java JDK
if [ -z "$JAVA_HOME" ]; then
    if [ -d "/Library/Java/JavaVirtualMachines/amazon-corretto-11.jdk/Contents/Home" ]; then
        export JAVA_HOME="/Library/Java/JavaVirtualMachines/amazon-corretto-11.jdk/Contents/Home"
    elif [ -d "/Library/Java/JavaVirtualMachines/jdk-26.jdk/Contents/Home" ]; then
        export JAVA_HOME="/Library/Java/JavaVirtualMachines/jdk-26.jdk/Contents/Home"
    fi
fi

if [ -n "$JAVA_HOME" ]; then
    export PATH="$JAVA_HOME/bin:$PATH"
fi

echo "================================================================================"
echo "    TRIE AUTOCORRECT & INTELLIGENT SEARCH ENGINE - FULL-STACK WEB LAUNCHER     "
echo "================================================================================"
echo "Java Version : $(java -version 2>&1 | head -n 1)"
echo "Target Port  : $PORT"
echo "Dictionary   : $DICT"
echo "--------------------------------------------------------------------------------"

mkdir -p bin

echo "Compiling Java core and HTTP server..."
javac -d bin \
    src/datastructures/*.java \
    src/core/*.java \
    src/benchmark/*.java \
    src/test/*.java \
    src/server/*.java \
    src/Main.java

echo "Compilation successful."
echo "Starting Trie HTTP Server on http://localhost:$PORT/ ..."
echo "Open http://localhost:$PORT in your web browser."
echo "Press Ctrl+C to terminate."
echo "--------------------------------------------------------------------------------"

exec java -Xmx1024m -cp bin server.TrieHttpServer "$PORT" "$DICT"

