#!/bin/bash

echo "🎨 DEMO DEL NUEVO MENÚ INTERACTIVO"
echo "=================================="
echo ""

echo "📋 OPCIONES DISPONIBLES:"
echo "  Números: 1, 2, 3, 4, 5, 6, 0"
echo "  Letras:  C, D, L, G, A, E, S" 
echo "  Palabras: cifrar, descifrar, leer, gestion, aliases, extension, salir"
echo ""

echo "🔍 Probando diferentes formas de navegación:"

echo ""
echo "1️⃣  Probando con NÚMEROS (opción 1 = cifrar):"
echo "1" | timeout 5s ./run.sh | grep -A 10 "CRYPTOBOX INTERACTIVO" | head -15

echo ""
echo "🅲  Probando con LETRAS (opción c = cifrar):"
echo "c" | timeout 5s ./run.sh | grep -A 10 "CRYPTOBOX INTERACTIVO" | head -15

echo ""
echo "📝 Probando con PALABRAS (opción cifrar):"
echo "cifrar" | timeout 5s ./run.sh | grep -A 10 "CRYPTOBOX INTERACTIVO" | head -15

echo ""
echo "🎯 El menú es ahora MUY INTERACTIVO:"
echo "  ✅ Acepta números (1-6,0)"
echo "  ✅ Acepta letras (C/D/L/G/A/E/S)"
echo "  ✅ Acepta palabras completas"
echo "  ✅ No distingue mayúsculas/minúsculas"
echo "  ✅ Submenús organizados por categorías"
echo "  ✅ Detección automática de contraseñas personalizadas"
echo ""

echo "🚀 ¡Prueba tú mismo con: ./run.sh" 