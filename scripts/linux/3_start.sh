#!/bin/bash
# Startet llama-server und bikeparts (prod-Profil) als Hintergrundprozesse.
# Verwendung: ./3_start.sh

set -e

BASE_DIR="$(pwd)/bikepartsSoftware"
LLAMA_DIR="$BASE_DIR/llama.cpp-server"
MODELS_DIR="$BASE_DIR/ki_models"
LOGS_DIR="$BASE_DIR/logs"
MODEL_FILE="qwen2.5-3b-instruct-q3_k_m.gguf"
LLAMA_SERVER="$LLAMA_DIR/llama-server"
BIKEPARTS="$BASE_DIR/bikeparts"

mkdir -p "$LOGS_DIR"

# llama-server starten
echo "Starte llama-server..."
nohup "$LLAMA_SERVER" \
    -m "$MODELS_DIR/$MODEL_FILE" \
    --port 8099 \
    -c 2048 \
    --threads 4 \
    > "$LOGS_DIR/llama-server.log" 2>&1 &
echo "llama-server gestartet (PID $!)"

# bikeparts starten
echo "Starte bikeparts (prod)..."
nohup "$BIKEPARTS" \
    --spring.profiles.active=prod \
    > "$LOGS_DIR/bikeparts.log" 2>&1 &
echo "bikeparts gestartet (PID $!)"

echo ""
echo "Logs:"
echo "  llama-server: $LOGS_DIR/llama-server.log"
echo "  bikeparts:    $LOGS_DIR/bikeparts.log"
