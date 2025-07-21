#!/bin/bash

echo "🔓 PROBANDO DESCIFRADO Y LECTURA SEGURA"
echo "======================================="

echo "📁 Archivos disponibles:"
echo "  - Base de datos: $([ -f src/data/cryptobox.db ] && echo "✅ Existe" || echo "❌ No existe")"
echo "  - Archivo cifrado: $([ -f src/data/encrypt/test.lock ] && echo "✅ test.lock" || echo "❌ No existe")"

if [ ! -f "src/data/encrypt/test.lock" ]; then
    echo "❌ No hay archivo cifrado para probar. Ejecuta primero: ./test_encryption.sh"
    exit 1
fi

echo ""
echo "🔍 Probando lectura segura (opción 6 - con contraseña)..."

# Simular lectura segura con contraseña
(
echo "6"           # Opción 6: Leer con contraseña (solo memoria)
echo "1"           # Archivo test.lock
echo "quimica123"  # Contraseña correcta
sleep 3
echo "10"          # Salir
) | ./run.sh

echo ""
echo "🎯 La lectura segura debería haber mostrado el contenido sin crear archivos temporales."
echo ""

echo "📊 Verificando que NO se crearon archivos temporales inseguros:"
if [ -z "$(find src/data/decrypt/ -name "*.unlocked" 2>/dev/null)" ]; then
    echo "✅ Perfecto - No hay archivos temporales inseguros"
else
    echo "⚠️  Se encontraron archivos temporales:"
    ls -la src/data/decrypt/*.unlocked 2>/dev/null || echo "Ninguno"
fi

echo ""
echo "🎯 Test de descifrado completado." 