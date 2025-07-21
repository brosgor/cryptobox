#!/bin/bash

# Script de ejecución para CryptoBox

# Verificar que el proyecto esté compilado
if [ ! -d "bin" ]; then
    echo "El proyecto no está compilado. Ejecuta: ./compile.sh"
    exit 1
fi

echo "Iniciando CryptoBox..."
echo "=========================================="

# Ejecutar con SQLite en el classpath
java -cp "bin:lib/sqlite-jdbc.jar" App 