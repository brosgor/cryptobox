#!/bin/bash

# Script de compilación para CryptoBox con dependencias

# Crear directorios necesarios
mkdir -p bin
mkdir -p src/data/key
mkdir -p src/data/encrypt
mkdir -p src/data/decrypt
mkdir -p src/data/extension
mkdir -p src/central

# Compilar el proyecto con SQLite y SLF4J en el classpath
echo "Compilando CryptoBox..."
find src -name "*.java" -exec javac -cp "lib/sqlite-jdbc.jar:lib/slf4j-api.jar:lib/slf4j-simple.jar" -d bin {} +

if [ $? -eq 0 ]; then
    echo "Compilación exitosa!"
    echo "Para ejecutar: ./run.sh"
else
    echo "Error en la compilación!"
    exit 1
fi 