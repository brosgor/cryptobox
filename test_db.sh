#!/bin/bash

echo "🧪 TESTING DATABASE INITIALIZATION..."
echo "======================================"

# Limpiar cualquier BD anterior para testing
rm -f src/data/cryptobox.db

echo "🔄 Iniciando aplicación para probar inicialización de BD..."

# Usar expect para automatizar la interacción
expect << 'EOF'
spawn ./run.sh
expect "Selecciona una opción:"
send "2\r"
expect "Selecciona el número del archivo:"
send "3\r"
expect "Nombre del archivo cifrado"
send "\r"
expect "Ingresa tu contraseña personalizada:"
send "test123\r"
expect "exitosamente"
send "10\r"
expect eof
EOF

echo ""
echo "🔍 Verificando si la base de datos se creó correctamente..."

if [ -f "src/data/cryptobox.db" ]; then
    echo "✅ Base de datos creada: src/data/cryptobox.db"
    
    # Verificar si sqlite3 está disponible para inspeccionar
    if command -v sqlite3 &> /dev/null; then
        echo "📊 Contenido de la base de datos:"
        sqlite3 src/data/cryptobox.db "SELECT * FROM passwords;"
        echo "📈 Número de registros: $(sqlite3 src/data/cryptobox.db "SELECT COUNT(*) FROM passwords;")"
    else
        echo "ℹ️  sqlite3 no disponible para inspeccionar contenido"
    fi
else
    echo "❌ Base de datos NO se creó"
fi

echo ""
echo "🎯 Test completado." 