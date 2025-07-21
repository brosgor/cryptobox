package core;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String DB_PATH = "src/data/cryptobox.db";
    private Connection connection;

    public DatabaseManager() {
        initializeDatabase();
    }

    private void initializeDatabase() {
        try {
            // Asegurar que el directorio existe
            java.io.File dbFile = new java.io.File(DB_PATH);
            java.io.File dbDir = dbFile.getParentFile();
            if (!dbDir.exists()) {
                dbDir.mkdirs();
                System.out.println("Directorio de base de datos creado: " + dbDir.getPath());
            }
            
            // Cargar el driver SQLite explícitamente
            Class.forName("org.sqlite.JDBC");
            
            // Crear conexión a SQLite
            connection = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH);
            
            if (connection != null) {
                System.out.println("Conexión a base de datos establecida: " + DB_PATH);
                
                // Crear tabla de contraseñas si no existe
                String createTableSQL = """
                    CREATE TABLE IF NOT EXISTS passwords (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        alias TEXT UNIQUE NOT NULL,
                        password_hash TEXT NOT NULL,
                        salt TEXT NOT NULL,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    )
                    """;
                
                Statement stmt = connection.createStatement();
                stmt.execute(createTableSQL);
                stmt.close();
                
                System.out.println("✅ Base de datos inicializada correctamente.");
            } else {
                System.err.println("❌ No se pudo establecer conexión a la base de datos.");
            }
            
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Driver SQLite no encontrado: " + e.getMessage());
            System.err.println("💡 Verifica que sqlite-jdbc.jar esté en el classpath.");
        } catch (SQLException e) {
            System.err.println("❌ Error al inicializar la base de datos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean savePasswordHash(String alias, String passwordHash, String salt) {
        if (connection == null) {
            System.err.println("❌ Base de datos no inicializada. Reintentando...");
            initializeDatabase();
            if (connection == null) {
                return false;
            }
        }
        
        String sql = "INSERT OR REPLACE INTO passwords (alias, password_hash, salt) VALUES (?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, alias);
            pstmt.setString(2, passwordHash);
            pstmt.setString(3, salt);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("❌ Error al guardar contraseña: " + e.getMessage());
            return false;
        }
    }

    public PasswordRecord getPasswordRecord(String alias) {
        if (connection == null) {
            System.err.println("❌ Base de datos no inicializada. Reintentando...");
            initializeDatabase();
            if (connection == null) {
                return null;
            }
        }
        
        String sql = "SELECT password_hash, salt FROM passwords WHERE alias = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, alias);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new PasswordRecord(
                    rs.getString("password_hash"),
                    rs.getString("salt")
                );
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error al obtener contraseña: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }

    public List<String> getAllAliases() {
        List<String> aliases = new ArrayList<>();
        String sql = "SELECT alias FROM passwords ORDER BY created_at DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                aliases.add(rs.getString("alias"));
            }
            
        } catch (SQLException e) {
            System.err.println("Error al obtener aliases: " + e.getMessage());
        }
        
        return aliases;
    }

    public boolean hasPasswords() {
        if (connection == null) {
            initializeDatabase();
            if (connection == null) {
                return false;
            }
        }
        
        String sql = "SELECT COUNT(*) as count FROM passwords";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt("count") > 0;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error al verificar contraseñas: " + e.getMessage());
        }
        
        return false;
    }

    public boolean deletePassword(String alias) {
        String sql = "DELETE FROM passwords WHERE alias = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, alias);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error al eliminar contraseña: " + e.getMessage());
            return false;
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Conexión a base de datos cerrada.");
            }
        } catch (SQLException e) {
            System.err.println("Error al cerrar la base de datos: " + e.getMessage());
        }
    }

    // Clase interna para representar un registro de contraseña
    public static class PasswordRecord {
        private final String passwordHash;
        private final String salt;

        public PasswordRecord(String passwordHash, String salt) {
            this.passwordHash = passwordHash;
            this.salt = salt;
        }

        public String getPasswordHash() {
            return passwordHash;
        }

        public String getSalt() {
            return salt;
        }
    }
} 