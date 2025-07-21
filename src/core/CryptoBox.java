package core;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import core.models.DataFile;

import javax.crypto.spec.PBEKeySpec;
import javax.crypto.SecretKeyFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;

public class CryptoBox {
    private final String vaultDir;
    private String password; // Variable de contraseña dinámica
    private final PasswordManager passwordManager;

    public CryptoBox() {
        this.vaultDir = "src/data/";
        this.password = "BROSGOR123"; // Contraseña por defecto
        this.passwordManager = new PasswordManager();
        
        // Limpieza automática de archivos antiguos al inicializar
        autoCleanup();
    }

    public CryptoBox(String vaultDir, String password) {
        this.vaultDir = vaultDir;
        this.password = password;
        this.passwordManager = new PasswordManager();
        
        // Limpieza automática de archivos antiguos al inicializar
        autoCleanup();
    }

    public CryptoBox(String vaultDir, String password, PasswordManager passwordManager) {
        this.vaultDir = vaultDir;
        this.password = password;
        this.passwordManager = passwordManager != null ? passwordManager : new PasswordManager();
        
        // Limpieza automática de archivos antiguos al inicializar
        autoCleanup();
    }

    // Método para generar claves RSA (solo si no existen)
    public void generateRSAKeys(String alias) throws Exception {
        String privateKeyPath = vaultDir + "key/" + alias + ".private.key";
        String publicKeyPath = vaultDir + "key/" + alias + ".public.key";
        
        // Verificar si las claves ya existen
        File privateKeyFile = new File(privateKeyPath);
        File publicKeyFile = new File(publicKeyPath);
        
        if (privateKeyFile.exists() && publicKeyFile.exists()) {
            System.out.println("Las claves RSA ya existen para el alias: " + alias + ". Reutilizando claves existentes.");
            return;
        }
        
        // Generar nuevas claves solo si no existen
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
        keyPairGen.initialize(2048);
        KeyPair pair = keyPairGen.generateKeyPair();
        PrivateKey privateKey = pair.getPrivate();
        PublicKey publicKey = pair.getPublic();

        saveKey(privateKey.getEncoded(), privateKeyPath);
        saveKey(publicKey.getEncoded(), publicKeyPath);

        System.out.println("Nuevas claves RSA generadas y guardadas para el alias: " + alias);
    }

    // Método para verificar si existen claves para un alias
    public boolean hasKeys(String alias) {
        File privateKeyFile = new File(vaultDir + "key/" + alias + ".private.key");
        File publicKeyFile = new File(vaultDir + "key/" + alias + ".public.key");
        return privateKeyFile.exists() && publicKeyFile.exists();
    }

    // Método para guardar las claves en archivos (Codificación Base64 y cifrado con
    // contraseña)
    private void saveKey(byte[] key, String path) throws Exception {
        byte[] salt = new byte[16];
        SecureRandom random = new SecureRandom();
        random.nextBytes(salt);

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
        SecretKey tmp = factory.generateSecret(spec);
        SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secret);
        byte[] iv = cipher.getIV();
        byte[] encryptedKey = cipher.doFinal(key);

        try (FileOutputStream fos = new FileOutputStream(path)) {
            fos.write(salt);
            fos.write(iv);
            fos.write(encryptedKey);
        }
    }

    // Método para cargar clave pública desde archivo (Decodificación Base64 y
    // descifrado con contraseña)
    private PublicKey loadPublicKey(String filePath) throws Exception {
        byte[] fileContent = Files.readAllBytes(new File(filePath).toPath());
        byte[] salt = Arrays.copyOfRange(fileContent, 0, 16);
        byte[] iv = Arrays.copyOfRange(fileContent, 16, 32);
        byte[] encryptedKey = Arrays.copyOfRange(fileContent, 32, fileContent.length);

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
        SecretKey tmp = factory.generateSecret(spec);
        SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(iv));
        byte[] decodedKey = cipher.doFinal(encryptedKey);

        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }

    // Método para cargar clave privada desde archivo (Decodificación Base64 y
    // descifrado con contraseña)
    private PrivateKey loadPrivateKey(String filePath) throws Exception {
        byte[] fileContent = Files.readAllBytes(new File(filePath).toPath());
        byte[] salt = Arrays.copyOfRange(fileContent, 0, 16);
        byte[] iv = Arrays.copyOfRange(fileContent, 16, 32);
        byte[] encryptedKey = Arrays.copyOfRange(fileContent, 32, fileContent.length);

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
        SecretKey tmp = factory.generateSecret(spec);
        SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(iv));
        byte[] decodedKey = cipher.doFinal(encryptedKey);

        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decodedKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }

    // Método para cifrar un archivo usando AES y RSA
    public void lockFile(String originalFilePath, String alias) throws Exception {
        // Generar claves RSA solo si no existen (reutilización)
        generateRSAKeys(alias);

        // Leer clave pública
        PublicKey publicKey = loadPublicKey(vaultDir + "key/" + alias + ".public.key");

        // Generar clave AES
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        SecretKey aesKey = keyGen.generateKey();

        // Cifrar contenido del archivo original
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        Cipher cipherAES = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipherAES.init(Cipher.ENCRYPT_MODE, aesKey, new IvParameterSpec(iv));

        byte[] originalData = Files.readAllBytes(new File(originalFilePath).toPath());
        byte[] encryptedData = cipherAES.doFinal(originalData);

        // Guardar archivo cifrado
        String encryptedFilePath = vaultDir + "encrypt/" + alias + ".lock";
        try (FileOutputStream fos = new FileOutputStream(encryptedFilePath)) {
            fos.write(iv);
            fos.write(encryptedData);
        }

        // Guardar la extensión del archivo original en un archivo cifrado .extinfo
        String extinfoPath = vaultDir + "extension/" + alias + ".extinfo";
        String fileExtension = getFileExtension(originalFilePath);
        byte[] extinfoData = cipherAES.doFinal(fileExtension.getBytes());
        try (FileOutputStream fos = new FileOutputStream(extinfoPath)) {
            fos.write(iv);
            fos.write(extinfoData);
        }

        // Cifrar la clave AES con RSA
        Cipher cipherRSA = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipherRSA.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedAESKey = cipherRSA.doFinal(aesKey.getEncoded());

        // Guardar clave AES cifrada
        String aesKeyPath = vaultDir + "key/" + alias + ".key";
        try (FileOutputStream fos = new FileOutputStream(aesKeyPath)) {
            fos.write(encryptedAESKey);
        }

        System.out.println("Archivo cifrado y clave AES guardada.");
    }

    // Método para descifrar un archivo
    public DataFile unlockFile(String encryptedFilePath, String alias) throws Exception {
        // Leer clave privada
        PrivateKey privateKey = loadPrivateKey(vaultDir + "key/" + alias + ".private.key");

        // Leer clave AES cifrada
        byte[] encryptedAESKey = Files.readAllBytes(new File(vaultDir + "key/" + alias + ".key").toPath());

        // Descifrar clave AES con RSA
        Cipher cipherRSA = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipherRSA.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] aesKeyBytes = cipherRSA.doFinal(encryptedAESKey);

        SecretKey aesKey = new SecretKeySpec(aesKeyBytes, "AES");

        // Leer archivo cifrado
        try (FileInputStream fis = new FileInputStream(encryptedFilePath)) {
            byte[] iv = new byte[16];
            fis.read(iv);
            byte[] encryptedData = fis.readAllBytes();

            // Descifrar datos
            Cipher cipherAES = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipherAES.init(Cipher.DECRYPT_MODE, aesKey, new IvParameterSpec(iv));
            byte[] originalData = cipherAES.doFinal(encryptedData);

            String decryptedFilePath = vaultDir + "decrypt/" + alias + ".unlocked";
            Files.write(new File(decryptedFilePath).toPath(), originalData);
            System.out.println("Archivo descifrado en: " + decryptedFilePath);

            // Codificar archivo descifrado en binario
            byte[] encodedData = Base64.getEncoder().encode(originalData);
            Files.write(new File(decryptedFilePath).toPath(), encodedData);

            System.out.println("Archivo codificado en: " + decryptedFilePath);
        }

        // Leer y descifrar el archivo .extinfo para obtener la extensión original
        String originalExtension = decryptExtension(alias);
        String decryptedFilePath = vaultDir + "decrypt/" + alias + ".unlocked";
        DataFile data = new DataFile(originalExtension, new File(decryptedFilePath));

        return data;
    }

    // desencripta el archivo .extinfo
    public String decryptExtension(String alias) throws Exception {
        try {
            // Intentar con contraseña de la base de datos primero
            if (passwordManager.hasStoredPasswords()) {
                // Verificar si el alias existe en la BD
                DatabaseManager.PasswordRecord record = passwordManager.getDatabaseManager().getPasswordRecord(alias);
                if (record != null) {
                    // Este archivo fue cifrado con contraseña personalizada
                    System.out.println("Archivo cifrado con contraseña personalizada detectado.");
                    // Para descifrar la extensión, necesitaríamos la contraseña del usuario
                    // Por ahora, asumimos que es txt por seguridad
                    return "txt";
                }
            }
        } catch (Exception e) {
            // Si hay error accediendo a la BD, continuar con método tradicional
        }

        // Método tradicional con contraseña por defecto
        try {
            // Leer clave privada
            PrivateKey privateKey = loadPrivateKey(vaultDir + "key/" + alias + ".private.key");

            // Leer clave AES cifrada
            byte[] encryptedAESKey = Files.readAllBytes(new File(vaultDir + "key/" + alias + ".key").toPath());

            // Descifrar clave AES con RSA
            Cipher cipherRSA = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipherRSA.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] aesKeyBytes = cipherRSA.doFinal(encryptedAESKey);

            SecretKey aesKey = new SecretKeySpec(aesKeyBytes, "AES");
            // Leer y descifrar el archivo .extinfo para obtener la extensión original
            String extinfoPath = vaultDir + "extension/" + alias + ".extinfo";

            try (FileInputStream fis = new FileInputStream(extinfoPath)) {
                byte[] iv = new byte[16];
                fis.read(iv);
                byte[] encryptedExtinfo = fis.readAllBytes();

                Cipher cipherAES = Cipher.getInstance("AES/CBC/PKCS5Padding");
                cipherAES.init(Cipher.DECRYPT_MODE, aesKey, new IvParameterSpec(iv));
                byte[] extinfoData = cipherAES.doFinal(encryptedExtinfo);

                String originalExtension = new String(extinfoData).trim();
                return originalExtension;
            }
        } catch (Exception e) {
            // Si falla el descifrado, probablemente es un archivo con contraseña personalizada
            System.out.println("No se puede descifrar la extensión con contraseña por defecto.");
            return "txt"; // Asumir txt por seguridad
        }
    }

    // Método para descifrar extensión con contraseña personalizada
    public String decryptExtensionWithPassword(String alias, String userPassword) throws Exception {
        // Verificar la contraseña contra la base de datos
        if (!passwordManager.verifyPasswordFromDB(alias, userPassword)) {
            throw new SecurityException("Contraseña incorrecta para el alias: " + alias);
        }
        
        // Usar la contraseña personalizada temporalmente
        String originalPassword = this.password;
        this.password = passwordManager.getEncryptionPassword(alias, userPassword);
        
        try {
            return decryptExtension(alias);
        } finally {
            // Restaurar la contraseña original
            this.password = originalPassword;
        }
    }

    // Método para obtener la extensión de un archivo
    private String getFileExtension(String filePath) {
        String extension = "";
        int i = filePath.lastIndexOf('.');
        if (i >= 0) {
            extension = filePath.substring(i + 1);
        }
        return extension;
    }

    // Método para cifrar un archivo con contraseña personalizada
    public void lockFileWithPassword(String originalFilePath, String alias, String userPassword) throws Exception {
        try {
            // Verificar si la contraseña ya existe para este alias
            if (!passwordManager.verifyPasswordFromDB(alias, userPassword)) {
                // Si no existe, guardar la nueva contraseña
                System.out.println("🔐 Guardando nueva contraseña para alias: " + alias);
                boolean saved = passwordManager.savePassword(alias, userPassword);
                if (!saved) {
                    throw new Exception("No se pudo guardar la contraseña en la base de datos");
                }
            } else {
                System.out.println("🔑 Usando contraseña existente para alias: " + alias);
            }
            
            // Usar la contraseña personalizada temporalmente
            String originalPassword = this.password;
            this.password = passwordManager.getEncryptionPassword(alias, userPassword);
            
            try {
                lockFile(originalFilePath, alias);
            } finally {
                // Restaurar la contraseña original
                this.password = originalPassword;
            }
        } catch (Exception e) {
            System.err.println("❌ Error en el proceso de cifrado con contraseña: " + e.getMessage());
            throw e;
        }
    }

    // Método para descifrar un archivo con contraseña personalizada
    public DataFile unlockFileWithPassword(String encryptedFilePath, String alias, String userPassword) throws Exception {
        // Verificar la contraseña contra la base de datos
        if (!passwordManager.verifyPasswordFromDB(alias, userPassword)) {
            throw new SecurityException("Contraseña incorrecta para el alias: " + alias);
        }
        
        // Usar la contraseña personalizada temporalmente
        String originalPassword = this.password;
        this.password = passwordManager.getEncryptionPassword(alias, userPassword);
        
        try {
            return unlockFile(encryptedFilePath, alias);
        } finally {
            // Restaurar la contraseña original
            this.password = originalPassword;
        }
    }

    // Método para obtener contraseña derivada (compatible con BD y archivos)
    public String getEffectivePassword(String alias, String userPassword) {
        try {
            // Intentar usar la base de datos primero
            if (passwordManager.hasStoredPasswords()) {
                String derivedPassword = passwordManager.getEncryptionPassword(alias, userPassword);
                if (derivedPassword != null) {
                    return derivedPassword;
                }
            }
        } catch (Exception e) {
            System.out.println("Error al acceder a BD, usando contraseña por defecto: " + e.getMessage());
        }
        
        // Fallback: usar contraseña por defecto para compatibilidad con archivos existentes
        return this.password;
    }

    // Método para listar aliases disponibles en la base de datos
    public java.util.List<String> getStoredAliases() {
        return passwordManager.getAllAliases();
    }

    // Método para verificar si hay contraseñas almacenadas
    public boolean hasStoredPasswords() {
        return passwordManager.hasStoredPasswords();
    }

    // Método para guardar nueva contraseña
    public boolean savePassword(String alias, String password) {
        return passwordManager.savePassword(alias, password);
    }

    // Método para eliminar contraseña
    public boolean deletePassword(String alias) {
        return passwordManager.deletePassword(alias);
    }

    // Método para cerrar recursos
    public void close() {
        // Limpiar archivos temporales antes de cerrar
        cleanupTemporaryFiles();
        
        if (passwordManager != null) {
            passwordManager.close();
        }
        
        System.out.println("🔒 Recursos cerrados y archivos temporales limpiados.");
    }

    // Método para leer archivo cifrado de forma segura (solo en memoria)
    public String readFileSecurely(String encryptedFilePath, String alias) throws Exception {
        // Leer clave privada
        PrivateKey privateKey = loadPrivateKey(vaultDir + "key/" + alias + ".private.key");

        // Leer clave AES cifrada
        byte[] encryptedAESKey = Files.readAllBytes(new File(vaultDir + "key/" + alias + ".key").toPath());

        // Descifrar clave AES con RSA
        Cipher cipherRSA = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipherRSA.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] aesKeyBytes = cipherRSA.doFinal(encryptedAESKey);

        SecretKey aesKey = new SecretKeySpec(aesKeyBytes, "AES");

        // Leer archivo cifrado
        try (FileInputStream fis = new FileInputStream(encryptedFilePath)) {
            byte[] iv = new byte[16];
            fis.read(iv);
            byte[] encryptedData = fis.readAllBytes();

            // Descifrar datos EN MEMORIA SOLAMENTE
            Cipher cipherAES = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipherAES.init(Cipher.DECRYPT_MODE, aesKey, new IvParameterSpec(iv));
            byte[] originalData = cipherAES.doFinal(encryptedData);

            // Decodificar desde Base64 si es necesario
            try {
                byte[] decodedData = Base64.getDecoder().decode(originalData);
                return new String(decodedData);
            } catch (IllegalArgumentException e) {
                // Si no es Base64, devolver como string directo
                return new String(originalData);
            }
        }
    }

    // Método para leer archivo cifrado con contraseña de forma segura (solo en memoria)
    public String readFileSecurelyWithPassword(String encryptedFilePath, String alias, String userPassword) throws Exception {
        // Verificar la contraseña contra la base de datos
        if (!passwordManager.verifyPasswordFromDB(alias, userPassword)) {
            throw new SecurityException("Contraseña incorrecta para el alias: " + alias);
        }
        
        // Usar la contraseña personalizada temporalmente
        String originalPassword = this.password;
        this.password = passwordManager.getEncryptionPassword(alias, userPassword);
        
        try {
            return readFileSecurely(encryptedFilePath, alias);
        } finally {
            // Restaurar la contraseña original
            this.password = originalPassword;
        }
    }

    // Método para limpiar archivos temporales de forma segura
    public void cleanupTemporaryFiles() {
        try {
            File decryptDir = new File(vaultDir + "decrypt/");
            if (decryptDir.exists()) {
                File[] tempFiles = decryptDir.listFiles();
                if (tempFiles != null) {
                    for (File tempFile : tempFiles) {
                        if (tempFile.isFile() && tempFile.getName().endsWith(".unlocked")) {
                            // Sobrescribir el archivo antes de eliminarlo para mayor seguridad
                            overwriteAndDelete(tempFile);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Advertencia: No se pudieron limpiar todos los archivos temporales: " + e.getMessage());
        }
    }

    // Método para sobrescribir y eliminar archivos de forma segura
    private void overwriteAndDelete(File file) {
        try {
            if (file.exists()) {
                long fileSize = file.length();
                
                // Sobrescribir con datos aleatorios 3 veces
                SecureRandom random = new SecureRandom();
                for (int pass = 0; pass < 3; pass++) {
                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        byte[] randomData = new byte[1024];
                        long remaining = fileSize;
                        
                        while (remaining > 0) {
                            int toWrite = (int) Math.min(randomData.length, remaining);
                            random.nextBytes(randomData);
                            fos.write(randomData, 0, toWrite);
                            remaining -= toWrite;
                        }
                        fos.flush();
                        fos.getFD().sync(); // Forzar escritura al disco
                    }
                }
                
                // Finalmente eliminar el archivo
                boolean deleted = file.delete();
                if (!deleted) {
                    System.err.println("Advertencia: No se pudo eliminar el archivo temporal: " + file.getPath());
                }
            }
        } catch (Exception e) {
            System.err.println("Error al eliminar archivo de forma segura: " + file.getPath() + " - " + e.getMessage());
        }
    }

    // Método para verificar y limpiar archivos antiguos automáticamente
    public void autoCleanup() {
        try {
            long currentTime = System.currentTimeMillis();
            long maxAge = 24 * 60 * 60 * 1000; // 24 horas en millisegundos
            
            File decryptDir = new File(vaultDir + "decrypt/");
            if (decryptDir.exists()) {
                File[] files = decryptDir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (file.isFile() && file.getName().endsWith(".unlocked")) {
                            long fileAge = currentTime - file.lastModified();
                            if (fileAge > maxAge) {
                                System.out.println("🧹 Limpiando archivo temporal antiguo: " + file.getName());
                                overwriteAndDelete(file);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error en limpieza automática: " + e.getMessage());
        }
    }
}