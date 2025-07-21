#!/bin/bash

# Demo script para CryptoBox Mejorado
# Muestra las nuevas funcionalidades implementadas

echo "=================================================="
echo "   🔐 CRYPTOBOX MEJORADO - DEMO INTERACTIVO 🔐"
echo "=================================================="
echo ""

echo "✅ MEJORAS IMPLEMENTADAS:"
echo "  1. Reutilización de claves públicas ✅"
echo "  2. Contraseñas personalizadas con hashing PBKDF2 ✅"
echo "  3. Base de datos SQLite local ✅"
echo "  4. Generación inteligente de claves ✅"
echo "  5. Gestión completa de contraseñas ✅"
echo "  6. 🔒 NOMBRE OPCIONAL para archivos cifrados ✅"
echo "  7. 🔒 LECTURA SEGURA solo en memoria ✅"
echo "  8. 🔒 LIMPIEZA AUTOMÁTICA de archivos temporales ✅"
echo "  9. 🔒 MANEJO ROBUSTO de excepciones ✅"
echo ""

echo "📁 ESTRUCTURA DEL PROYECTO:"
ls -la src/
echo ""

echo "📦 DEPENDENCIAS:"
echo "  - SQLite JDBC Driver: $(ls -lh lib/sqlite-jdbc.jar 2>/dev/null || echo 'No encontrado')"
echo ""

echo "🏗️  ESTADO DE COMPILACIÓN:"
if [ -d "bin" ]; then
    echo "  ✅ Proyecto compilado correctamente"
    echo "  📂 Clases compiladas: $(find bin -name "*.class" | wc -l)"
else
    echo "  ❌ Proyecto no compilado"
    echo "  💡 Ejecuta: ./compile.sh"
fi
echo ""

echo "📋 ARCHIVOS DE PRUEBA:"
if [ -f "src/central/test.txt" ]; then
    echo "  ✅ Archivo de prueba disponible: test.txt"
else
    echo "  ❌ No hay archivos de prueba"
fi
echo ""

echo "🗃️  BASE DE DATOS:"
if [ -f "src/data/cryptobox.db" ]; then
    echo "  ✅ Base de datos SQLite encontrada"
    echo "  📊 Tamaño: $(ls -lh src/data/cryptobox.db | awk '{print $5}')"
else
    echo "  ℹ️  Base de datos se creará en la primera ejecución"
fi
echo ""

echo "🚀 CÓMO PROBAR LAS NUEVAS FUNCIONALIDADES:"
echo ""
echo "1. CIFRADO CON CONTRASEÑA PERSONALIZADA:"
echo "   ./run.sh → Opción 2 → Seleccionar test.txt → [Enter para usar nombre original] → Contraseña: 'mi_contraseña_segura'"
echo ""
echo "2. LECTURA SEGURA (SOLO MEMORIA):"
echo "   ./run.sh → Opción 5 → Seleccionar archivo .lock → Ver contenido sin crear archivos temporales"
echo ""
echo "3. LECTURA SEGURA CON CONTRASEÑA:"
echo "   ./run.sh → Opción 6 → Seleccionar archivo .lock → Ingresar contraseña → Ver contenido en memoria"
echo ""
echo "4. GESTIÓN DE CONTRASEÑAS:"
echo "   ./run.sh → Opción 8 → Guardar/Ver/Eliminar contraseñas"
echo ""
echo "5. VER ALIASES ALMACENADOS:"
echo "   ./run.sh → Opción 9 → Ver estado de la base de datos"
echo ""
echo "6. REUTILIZACIÓN DE CLAVES:"
echo "   Cifra múltiples archivos con el mismo alias y verifica que reutiliza las claves RSA"
echo ""

echo "💡 COMANDOS ÚTILES:"
echo "  ./compile.sh    - Compilar el proyecto"
echo "  ./run.sh        - Ejecutar la aplicación"
echo "  rm -rf bin/     - Limpiar compilación"
echo "  ls src/data/    - Ver archivos generados"
echo ""

echo "🔍 VERIFICAR MEJORAS:"
echo ""

# Verificar si las nuevas clases existen
if [ -f "src/core/PasswordManager.java" ]; then
    echo "  ✅ PasswordManager.java - Sistema de hashing implementado"
else
    echo "  ❌ PasswordManager.java - Falta archivo"
fi

if [ -f "src/core/DatabaseManager.java" ]; then
    echo "  ✅ DatabaseManager.java - Gestión de SQLite implementada"
else
    echo "  ❌ DatabaseManager.java - Falta archivo"
fi

# Verificar funcionalidades en CryptoBox
if grep -q "lockFileWithPassword" src/core/CryptoBox.java; then
    echo "  ✅ CryptoBox.java - Métodos de contraseña personalizada"
else
    echo "  ❌ CryptoBox.java - Faltan métodos de contraseña"
fi

if grep -q "readFileSecurely" src/core/CryptoBox.java; then
    echo "  ✅ CryptoBox.java - Lectura segura en memoria implementada"
else
    echo "  ❌ CryptoBox.java - Falta lectura segura"
fi

if grep -q "cleanupTemporaryFiles" src/core/CryptoBox.java; then
    echo "  ✅ CryptoBox.java - Limpieza automática de archivos temporales"
else
    echo "  ❌ CryptoBox.java - Falta limpieza automática"
fi

if grep -q "ya existen para el alias" src/core/CryptoBox.java; then
    echo "  ✅ CryptoBox.java - Reutilización de claves implementada"
else
    echo "  ❌ CryptoBox.java - Falta reutilización de claves"
fi

# Verificar nueva interfaz
if grep -q "👁️.*solo memoria" src/cli/MainCLI.java; then
    echo "  ✅ MainCLI.java - Nueva interfaz de lectura segura"
else
    echo "  ❌ MainCLI.java - Falta interfaz de lectura segura"
fi

if grep -q "encryptedFileName.isEmpty" src/cli/MainCLI.java; then
    echo "  ✅ MainCLI.java - Nombre opcional implementado"
else
    echo "  ❌ MainCLI.java - Falta nombre opcional"
fi

echo ""
echo "=================================================="
echo "🎯 LISTO PARA PROBAR - Ejecuta: ./run.sh"
echo "==================================================" 