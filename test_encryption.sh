#!/bin/bash

echo "🧪 PROBANDO CIFRADO CON CONTRASEÑA PERSONALIZADA"
echo "================================================"

# Limpiar archivos previos
rm -f src/data/cryptobox.db
rm -f src/data/encrypt/test.lock
rm -f src/data/key/test.*

echo "📁 Estado inicial:"
echo "  - Base de datos: $([ -f src/data/cryptobox.db ] && echo "Existe" || echo "No existe")"
echo "  - Archivo de prueba: $([ -f src/central/test.txt ] && echo "✅ test.txt" || echo "❌ Falta test.txt")"

echo ""
echo "🔐 Ejecutando cifrado automático..."

# Simular entrada del usuario
(
echo "2"           # Opción 2: Cifrar con contraseña personalizada
echo "3"           # Archivo test.txt
echo ""            # Nombre por defecto
echo "quimica123"  # Contraseña
sleep 2
echo "10"          # Salir
) | ./run.sh

echo ""
echo "🔍 Verificando resultados:"

if [ -f "src/data/cryptobox.db" ]; then
    echo "✅ Base de datos creada correctamente"
else
    echo "❌ Base de datos NO se creó"
fi

if [ -f "src/data/encrypt/test.lock" ]; then
    echo "✅ Archivo cifrado creado: test.lock"
    echo "📊 Tamaño: $(ls -lh src/data/encrypt/test.lock | awk '{print $5}')"
else
    echo "❌ Archivo cifrado NO se creó"
fi

if [ -f "src/data/key/test.public.key" ]; then
    echo "✅ Claves RSA generadas"
else
    echo "❌ Claves RSA NO generadas"
fi

echo ""
echo "🎯 Prueba completada." 