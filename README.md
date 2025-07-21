# CryptoBox - Sistema de Cifrado Híbrido Mejorado

## Descripción

CryptoBox es un sistema de cifrado híbrido avanzado que combina RSA y AES para proporcionar máxima seguridad en el cifrado de archivos. **Versión mejorada** con soporte para:

- ✅ **Reutilización de claves públicas** - Las claves RSA se reutilizan automáticamente
- ✅ **Contraseñas personalizadas con hashing seguro** - PBKDF2 con 100,000 iteraciones
- ✅ **Base de datos SQLite local** - Almacenamiento seguro de contraseñas
- ✅ **Generación inteligente de claves** - Basada en BD con fallback a archivos
- ✅ **Gestión completa de contraseñas** - CRUD completo desde la interfaz

## Nuevas Características

### 🔐 Sistema de Contraseñas Avanzado
- Contraseñas personalizadas protegidas con **PBKDF2WithHmacSHA256**
- Salt único por contraseña para máxima seguridad
- Base de datos SQLite para almacenamiento persistente
- Derivación de claves contextual por alias

### 🔄 Reutilización Inteligente de Claves
- Las claves RSA se reutilizan automáticamente si ya existen
- Optimización del proceso de cifrado
- Compatibilidad total con archivos existentes

### 📊 Gestión de Base de Datos
- Almacenamiento local de contraseñas hasheadas
- Interfaz completa para gestionar aliases
- Estado y estadísticas de la base de datos
- Eliminación segura de contraseñas

## Instalación y Configuración

### Prerrequisitos
- Java 8 o superior
- wget (para descargar dependencias)
- Bash (para scripts de compilación)

### Compilación
```bash
# Compilar el proyecto (descarga automáticamente SQLite JDBC)
./compile.sh
```

### Ejecución
```bash
# Ejecutar la aplicación
./run.sh
```

## Uso de las Nuevas Funciones

### 1. Cifrado con Contraseña Personalizada
```
Opción 2: Cifrar un archivo con contraseña personalizada
- Selecciona el archivo a cifrar
- Ingresa un alias único
- Define tu contraseña personalizada
- La contraseña se guarda automáticamente en la BD
```

### 2. Descifrado con Contraseña Personalizada
```
Opción 4: Descifrar un archivo con contraseña personalizada
- Selecciona el archivo .lock
- Ingresa la contraseña correspondiente
- El sistema verifica contra la BD automáticamente
```

### 3. Gestión de Contraseñas
```
Opción 7: Gestionar contraseñas
- Guardar nueva contraseña para un alias
- Eliminar contraseñas existentes
- Ver todas las contraseñas almacenadas
```

### 4. Visualización de Aliases
```
Opción 8: Ver aliases almacenados
- Muestra todos los aliases con contraseñas en BD
- Indica si tienen claves RSA disponibles
- Estado de la base de datos
```

## Arquitectura del Sistema

### Componentes Principales

#### `CryptoBox.java` (Mejorado)
- Reutilización automática de claves RSA
- Métodos específicos para contraseñas personalizadas
- Integración con PasswordManager
- Compatibilidad con sistema legacy

#### `PasswordManager.java` (Nuevo)
- Hashing seguro con PBKDF2
- Generación de salt único
- Derivación de claves para cifrado
- Validación de contraseñas

#### `DatabaseManager.java` (Nuevo)
- Gestión completa de SQLite
- CRUD para contraseñas
- Consultas optimizadas
- Manejo de conexiones

#### `MainCLI.java` (Mejorado)
- Interfaz expandida con 9 opciones
- Gestión completa de contraseñas
- Manejo de errores mejorado
- Cierre apropiado de recursos

## Estructura de Archivos

```
cryptobox/
├── src/
│   ├── App.java                    # Punto de entrada
│   ├── cli/
│   │   └── MainCLI.java           # Interfaz mejorada
│   ├── core/
│   │   ├── CryptoBox.java         # Motor de cifrado mejorado
│   │   ├── PasswordManager.java   # 🆕 Gestión de contraseñas
│   │   ├── DatabaseManager.java   # 🆕 Gestión de BD
│   │   └── models/
│   │       └── DataFile.java      # Modelo de datos
│   ├── utils/
│   │   └── Utils.java             # Utilidades
│   ├── data/                      # Datos del sistema
│   │   ├── cryptobox.db          # 🆕 Base de datos SQLite
│   │   ├── key/                  # Claves RSA
│   │   ├── encrypt/              # Archivos cifrados
│   │   ├── decrypt/              # Archivos descifrados
│   │   └── extension/            # Info de extensiones
│   └── central/                   # Archivos originales
├── lib/
│   └── sqlite-jdbc.jar           # 🆕 Driver SQLite
├── bin/                          # Archivos compilados
├── compile.sh                    # 🆕 Script de compilación
├── run.sh                        # 🆕 Script de ejecución
└── README.md                     # Esta documentación
```

## Flujo de Trabajo

### Cifrado con Contraseña Personalizada
1. Usuario selecciona archivo y define alias
2. Ingresa contraseña personalizada
3. Sistema verifica si el alias existe en BD
4. Si no existe, guarda la contraseña hasheada
5. Deriva clave de cifrado usando salt contextual
6. Reutiliza claves RSA existentes o genera nuevas
7. Cifra archivo con AES derivado de la contraseña
8. Cifra clave AES con RSA reutilizable

### Descifrado con Verificación de BD
1. Usuario selecciona archivo .lock
2. Sistema extrae alias del nombre del archivo
3. Solicita contraseña al usuario
4. Verifica contraseña contra hash en BD
5. Deriva la misma clave de cifrado
6. Descifra usando claves reutilizables
7. Recupera archivo original

## Seguridad Implementada

### Hashing de Contraseñas
- **Algoritmo**: PBKDF2WithHmacSHA256
- **Iteraciones**: 100,000
- **Salt**: 32 bytes aleatorios por contraseña
- **Hash**: 64 bytes de salida

### Derivación de Claves
- Salt contextual usando alias + salt original
- XOR para combinar contexto sin comprometer seguridad
- Claves únicas por alias aunque se use la misma contraseña

### Almacenamiento Seguro
- Contraseñas nunca almacenadas en texto plano
- Solo hashes y salts en base de datos
- Conexiones SQLite locales (sin red)

## Compatibilidad

- ✅ **Archivos existentes**: Totalmente compatible con sistema anterior
- ✅ **Claves RSA**: Reutiliza claves generadas previamente
- ✅ **Contraseña por defecto**: Funciona como fallback para archivos legacy
- ✅ **Migración**: Sin necesidad de re-cifrar archivos existentes

## Comandos Útiles

```bash
# Compilar proyecto
./compile.sh

# Ejecutar aplicación
./run.sh

# Ver estructura de BD (requiere sqlite3)
sqlite3 src/data/cryptobox.db ".schema"

# Limpiar compilación
rm -rf bin/

# Backup de BD
cp src/data/cryptobox.db backup_$(date +%Y%m%d).db
```

## Notas de Desarrollo

### Mejoras Implementadas
1. **Reutilización de claves**: Evita regeneración innecesaria de claves RSA
2. **Contraseñas seguras**: Sistema robusto de hashing y validación
3. **Base de datos local**: Almacenamiento persistente y eficiente
4. **Interfaz mejorada**: Opciones claras para todas las funcionalidades
5. **Manejo de recursos**: Cierre apropiado de conexiones de BD

### Consideraciones de Rendimiento
- Claves RSA se generan solo una vez por alias
- Base de datos SQLite optimizada para consultas locales
- Hashing intensivo solo durante autenticación
- Reutilización de conexiones donde es posible

## Contribución

Para contribuir al proyecto:
1. Fork el repositorio
2. Crea una rama para tu feature
3. Implementa mejoras manteniendo compatibilidad
4. Ejecuta tests de compatibilidad
5. Envía pull request

---

**CryptoBox** - Cifrado híbrido seguro y eficiente con gestión avanzada de contraseñas.
