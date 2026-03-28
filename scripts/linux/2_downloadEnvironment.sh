#!/bin/bash
# Laedt llama.cpp und das KI-Modell herunter.
# Laedt alles nach $HOME/bikepartsSoftware herunter.

set -e

LLAMA_VERSION="b8308"
BASE_DIR="$(pwd)/bikepartsSoftware"
LLAMA_DIR="$BASE_DIR/llama.cpp-server"
MODELS_DIR="$BASE_DIR/ki_models"
LLAMA_ARCHIVE="llama-${LLAMA_VERSION}-bin-ubuntu-x64.tar.gz"
LLAMA_URL="https://github.com/ggml-org/llama.cpp/releases/download/${LLAMA_VERSION}/$LLAMA_ARCHIVE"
MODEL_FILE="qwen2.5-3b-instruct-q3_k_m.gguf"
MODEL_URL="https://huggingface.co/Qwen/Qwen2.5-3B-Instruct-GGUF/resolve/main/$MODEL_FILE?download=true"
BIKEPARTS_URL="https://github.com/EllenDeveloper/Bikeparts/releases/latest/download/bikeparts"

mkdir -p "$LLAMA_DIR"
mkdir -p "$MODELS_DIR"

# llama.cpp herunterladen und entpacken
echo "Lade llama.cpp ${LLAMA_VERSION} herunter..."
wget -O "$LLAMA_DIR/$LLAMA_ARCHIVE" "$LLAMA_URL"
echo "Entpacke llama.cpp..."
tar -xzf "$LLAMA_DIR/$LLAMA_ARCHIVE" -C "$LLAMA_DIR"
rm "$LLAMA_DIR/$LLAMA_ARCHIVE"
echo "llama.cpp bereit in $LLAMA_DIR"

# KI-Modell herunterladen
echo "Lade Modell $MODEL_FILE herunter (kann einige Minuten dauern)..."
wget -O "$MODELS_DIR/$MODEL_FILE" "$MODEL_URL"
echo "Modell bereit in $MODELS_DIR/$MODEL_FILE"

# bikeparts Binary herunterladen
echo "Lade bikeparts Binary herunter..."
wget -O "$BASE_DIR/bikeparts" "$BIKEPARTS_URL"
chmod +x "$BASE_DIR/bikeparts"
echo "bikeparts bereit in $BASE_DIR/bikeparts"

echo ""
echo "Download abgeschlossen."
echo "  llama-server: $LLAMA_DIR/llama-server"
echo "  Modell:       $MODELS_DIR/$MODEL_FILE"
echo "  bikeparts:    $BASE_DIR/bikeparts"
