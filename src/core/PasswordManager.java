package core;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

public class PasswordManager {
    private static final int SALT_LENGTH = 32;
    private static final int HASH_LENGTH = 64;
    private static final int ITERATIONS = 100000;
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";

    private final DatabaseManager dbManager;

    public PasswordManager() {
        this.dbManager = new DatabaseManager();
    }

    /**
     * Genera un hash seguro de la contraseña con salt
     */
    public String hashPassword(String password, String salt) {
        try {
            byte[] saltBytes = Base64.getDecoder().decode(salt);
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), saltBytes, ITERATIONS, HASH_LENGTH * 8);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] hash = factory.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Error al generar hash de contraseña", e);
        }
    }

    /**
     * Genera un salt aleatorio
     */
    public String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * Verifica si una contraseña coincide con el hash almacenado
     */
    public boolean verifyPassword(String password, String storedHash, String salt) {
        String hashOfInput = hashPassword(password, salt);
        return hashOfInput.equals(storedHash);
    }

    /**
     * Guarda una nueva contraseña en la base de datos
     */
    public boolean savePassword(String alias, String password) {
        String salt = generateSalt();
        String hash = hashPassword(password, salt);
        
        boolean saved = dbManager.savePasswordHash(alias, hash, salt);
        if (saved) {
            System.out.println("Contraseña guardada exitosamente para el alias: " + alias);
        } else {
            System.out.println("Error al guardar la contraseña para el alias: " + alias);
        }
        
        return saved;
    }

    /**
     * Verifica una contraseña contra la base de datos
     */
    public boolean verifyPasswordFromDB(String alias, String password) {
        DatabaseManager.PasswordRecord record = dbManager.getPasswordRecord(alias);
        
        if (record == null) {
            System.out.println("No se encontró contraseña para el alias: " + alias);
            return false;
        }
        
        return verifyPassword(password, record.getPasswordHash(), record.getSalt());
    }

    /**
     * Obtiene la contraseña derivada para uso en cifrado
     */
    public String getDerivedPassword(String alias, String inputPassword) {
        DatabaseManager.PasswordRecord record = dbManager.getPasswordRecord(alias);
        
        if (record != null) {
            // Si existe en BD, verificar y devolver la contraseña derivada
            if (verifyPassword(inputPassword, record.getPasswordHash(), record.getSalt())) {
                // Usar la contraseña original para derivar la clave de cifrado
                return hashPassword(inputPassword, record.getSalt());
            } else {
                throw new SecurityException("Contraseña incorrecta para el alias: " + alias);
            }
        } else {
            // Si no existe en BD, crear nueva entrada y usar contraseña por defecto
            System.out.println("Alias no encontrado en BD. Usando contraseña por defecto.");
            return "BROSGOR123"; // Contraseña por defecto para compatibilidad
        }
    }

    /**
     * Verifica si hay contraseñas en la base de datos
     */
    public boolean hasStoredPasswords() {
        return dbManager.hasPasswords();
    }

    /**
     * Obtiene todos los aliases almacenados
     */
    public java.util.List<String> getAllAliases() {
        return dbManager.getAllAliases();
    }

    /**
     * Elimina una contraseña de la base de datos
     */
    public boolean deletePassword(String alias) {
        return dbManager.deletePassword(alias);
    }

    /**
     * Cierra la conexión a la base de datos
     */
    public void close() {
        if (dbManager != null) {
            dbManager.close();
        }
    }

    /**
     * Genera una contraseña derivada específica para el cifrado de archivos
     */
    public String getEncryptionPassword(String alias, String userPassword) {
        DatabaseManager.PasswordRecord record = dbManager.getPasswordRecord(alias);
        
        if (record != null) {
            // Verificar contraseña y generar clave de cifrado
            if (verifyPassword(userPassword, record.getPasswordHash(), record.getSalt())) {
                // Generar una clave específica para cifrado usando el alias como contexto adicional
                String contextSalt = generateContextSalt(alias, record.getSalt());
                return hashPassword(userPassword + alias, contextSalt);
            } else {
                throw new SecurityException("Contraseña incorrecta");
            }
        }
        
        return null;
    }

    /**
     * Genera un salt contextual para el cifrado basado en el alias
     */
    private String generateContextSalt(String alias, String originalSalt) {
        try {
            byte[] originalSaltBytes = Base64.getDecoder().decode(originalSalt);
            byte[] aliasBytes = alias.getBytes();
            
            // Combinar salt original con alias para crear contexto único
            byte[] contextBytes = new byte[originalSaltBytes.length];
            for (int i = 0; i < contextBytes.length; i++) {
                contextBytes[i] = (byte) (originalSaltBytes[i] ^ aliasBytes[i % aliasBytes.length]);
            }
            
            return Base64.getEncoder().encodeToString(contextBytes);
        } catch (Exception e) {
            // Fallback en caso de error
            return originalSalt;
        }
    }
} 