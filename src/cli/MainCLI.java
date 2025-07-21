package cli;

import core.CryptoBox;
import core.models.DataFile;

import java.io.File;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Scanner;
import utils.Utils;

public class MainCLI {
    private static final String DATA_DIR = "src/data/";
    private static final String DATA_DIR_KEY = DATA_DIR + "key/";
    private static final String DATA_DIR_DECRYPT = DATA_DIR + "decrypt/";
    private static final String DATA_DIR_ENCRYPT = DATA_DIR + "encrypt/";
    private static final String DATA_DIR_EXT = DATA_DIR + "extension/";
    private static final String ORIGINALS_DIR = "src/central/";

    public static void main(String[] args) {
        Utils.createDirectories(DATA_DIR_KEY, DATA_DIR_DECRYPT, DATA_DIR_ENCRYPT, DATA_DIR_EXT, ORIGINALS_DIR);

        Utils.animateBrosgor();
        CryptoBox cipherBox = new CryptoBox();
        try (Scanner scanner = new Scanner(System.in)) {
            Utils.clearConsole();
            System.out.println("Bienvenido al sistema de cifrado híbrido BROSGOR.");
            System.out.println("Este programa utiliza un método de cifrado híbrido que combina RSA y AES.");
            System.out.println("Puedes cifrar y descifrar archivos con alta seguridad.");
            System.out.println("Ahora con soporte para contraseñas personalizadas y base de datos local!");

            while (true) {
                displayMainMenu();
                String option = scanner.nextLine().toLowerCase().trim();

                switch (option) {
                    case "1":
                    case "c":
                    case "cifrar":
                        Utils.clearConsole();
                        handleEncryptionMenu(cipherBox, scanner);
                        break;
                    case "2":
                    case "d":
                    case "descifrar":
                        Utils.clearConsole();
                        handleDecryptionMenu(cipherBox, scanner);
                        break;
                    case "3":
                    case "l":
                    case "leer":
                        Utils.clearConsole();
                        handleReadingMenu(cipherBox, scanner);
                        break;
                    case "7":
                        Utils.clearConsole();
                        File unlockedFile = Utils.listFiles(DATA_DIR_DECRYPT, scanner);
                        String alias = unlockedFile.getName().split("\\.")[0];
                        if (unlockedFile != null) {
                            try {
                                String extension = cipherBox.decryptExtension(alias);
                                System.out.print(
                                        "La extensión original del archivo es: " + extension
                                                + ". ¿Deseas cambiarla? (s/n): ");
                                String choice2 = scanner.nextLine();
                                if (choice2.equalsIgnoreCase("s")) {
                                    Utils.convertExtension(unlockedFile, extension);
                                }
                            } catch (Exception e) {
                                System.err.println("❌ Error al procesar el archivo: " + e.getMessage());
                                Utils.pauseForKeyPress(scanner);
                            }
                        }
                        break;
                    case "4":
                    case "g":
                    case "gestion":
                        Utils.clearConsole();
                        handlePasswordManagement(cipherBox, scanner);
                        break;
                    case "5":
                    case "a":
                    case "aliases":
                        Utils.clearConsole();
                        displayStoredAliases(cipherBox, scanner);
                        break;
                    case "6":
                    case "e":
                    case "extension":
                        Utils.clearConsole();
                        handleExtensionConversion(cipherBox, scanner);
                        break;
                    case "0":
                    case "s":
                    case "salir":
                    case "exit":
                        Utils.clearConsole();
                        System.out.println("🔐 Cerrando conexiones y limpiando archivos temporales...");
                        cipherBox.close();
                        System.out.println("✅ Programa cerrado de forma segura.");
                        Utils.pauseForKeyPress(scanner);
                        return;

                    default:
                        Utils.clearConsole();
                        System.out.println("Opción inválida. Por favor, intenta nuevamente.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Asegurar que se cierren las conexiones
            if (cipherBox != null) {
                cipherBox.close();
            }
        }
    }

    private static void handlePasswordManagement(CryptoBox cipherBox, Scanner scanner) {
        System.out.println("=== GESTIÓN DE CONTRASEÑAS ===");
        System.out.println("1. Guardar nueva contraseña");
        System.out.println("2. Eliminar contraseña");
        System.out.println("3. Ver todas las contraseñas almacenadas");
        System.out.println("4. Volver al menú principal");
        
        System.out.print("Selecciona una opción: ");
        String choice = scanner.nextLine();
        
        switch (choice) {
            case "1":
                System.out.print("Ingresa el alias para la contraseña: ");
                String alias = scanner.nextLine();
                System.out.print("Ingresa la contraseña: ");
                String password = scanner.nextLine();
                
                if (cipherBox.savePassword(alias, password)) {
                    System.out.println("Contraseña guardada exitosamente!");
                } else {
                    System.out.println("Error al guardar la contraseña.");
                }
                Utils.pauseForKeyPress(scanner);
                break;
                
            case "2":
                java.util.List<String> aliases = cipherBox.getStoredAliases();
                if (aliases.isEmpty()) {
                    System.out.println("No hay contraseñas almacenadas.");
                } else {
                    System.out.println("Aliases disponibles:");
                    for (int i = 0; i < aliases.size(); i++) {
                        System.out.println((i + 1) + ". " + aliases.get(i));
                    }
                    System.out.print("Ingresa el número del alias a eliminar: ");
                    try {
                        int index = Integer.parseInt(scanner.nextLine()) - 1;
                        if (index >= 0 && index < aliases.size()) {
                            String aliasToDelete = aliases.get(index);
                            if (cipherBox.deletePassword(aliasToDelete)) {
                                System.out.println("Contraseña eliminada exitosamente!");
                            } else {
                                System.out.println("Error al eliminar la contraseña.");
                            }
                        } else {
                            System.out.println("Selección inválida.");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Entrada inválida.");
                    }
                }
                Utils.pauseForKeyPress(scanner);
                break;
                
            case "3":
                displayStoredAliases(cipherBox, scanner);
                break;
                
            case "4":
                return;
                
            default:
                System.out.println("Opción inválida.");
                Utils.pauseForKeyPress(scanner);
        }
    }

    private static void displayStoredAliases(CryptoBox cipherBox, Scanner scanner) {
        System.out.println("=== ALIASES ALMACENADOS ===");
        
        java.util.List<String> aliases = cipherBox.getStoredAliases();
        if (aliases.isEmpty()) {
            System.out.println("No hay contraseñas almacenadas en la base de datos.");
            System.out.println("El sistema usará las claves generadas en archivos para compatibilidad.");
        } else {
            System.out.println("Aliases con contraseñas personalizadas:");
            for (int i = 0; i < aliases.size(); i++) {
                String aliasName = aliases.get(i);
                boolean hasKeys = cipherBox.hasKeys(aliasName);
                System.out.printf("%d. %s %s\n", 
                    i + 1, 
                    aliasName, 
                    hasKeys ? "[Claves disponibles]" : "[Sin claves RSA]"
                );
            }
        }
        
        System.out.println("\nEstado de la base de datos: " + 
            (cipherBox.hasStoredPasswords() ? "Activa con datos" : "Vacía"));
        
        Utils.pauseForKeyPress(scanner);
    }

    // Método para mostrar el menú principal interactivo
    private static void displayMainMenu() {
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                🔐 CRYPTOBOX INTERACTIVO 🔐                  ║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.println("║  1/C  🔒 CIFRAR archivos                                    ║");
        System.out.println("║  2/D  🔓 DESCIFRAR archivos                                 ║");
        System.out.println("║  3/L  👁️  LEER archivos (solo memoria, seguro)             ║");
        System.out.println("║  4/G  🔑 GESTIÓN de contraseñas                             ║");
        System.out.println("║  5/A  📋 Ver ALIASES almacenados                           ║");
        System.out.println("║  6/E  🔧 Cambiar EXTENSIÓN de archivos                     ║");
        System.out.println("║  0/S  🚪 SALIR del programa                                 ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        System.out.print("Selecciona una opción [1-6/C/D/L/G/A/E/S]: ");
    }

    // Método para manejar el menú de cifrado
    private static void handleEncryptionMenu(CryptoBox cipherBox, Scanner scanner) {
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                    🔒 MENÚ DE CIFRADO 🔒                    ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        
        File sourceFile = Utils.listFiles(ORIGINALS_DIR, scanner);
        if (sourceFile == null) return;
        
        String originalName = sourceFile.getName();
        String nameWithoutExt = originalName.contains(".") ? 
            originalName.substring(0, originalName.lastIndexOf('.')) : originalName;
        
        System.out.println("\n📁 Archivo seleccionado: " + originalName);
        System.out.print("🏷️  Nombre del archivo cifrado [" + nameWithoutExt + "]: ");
        String encryptedFileName = scanner.nextLine().trim();
        
        if (encryptedFileName.isEmpty()) {
            encryptedFileName = nameWithoutExt;
        }
        
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║          TIPO DE CIFRADO               ║");
        System.out.println("╠════════════════════════════════════════╣");
        System.out.println("║  S  🔑 Con contraseña PERSONALIZADA    ║");
        System.out.println("║  N  🔒 Cifrado BÁSICO (por defecto)    ║");
        System.out.println("╚════════════════════════════════════════╝");
        System.out.print("¿Usar contraseña personalizada? [S/n]: ");
        
        String useCustomPassword = scanner.nextLine().toLowerCase().trim();
        
        try {
            if (useCustomPassword.equals("s") || useCustomPassword.equals("si") || useCustomPassword.equals("yes")) {
                System.out.print("🔐 Ingresa tu contraseña personalizada: ");
                String userPassword = scanner.nextLine();
                
                if (userPassword.trim().isEmpty()) {
                    System.out.println("❌ La contraseña no puede estar vacía.");
                    Utils.pauseForKeyPress(scanner);
                    return;
                }
                
                cipherBox.lockFileWithPassword(sourceFile.getPath(), encryptedFileName, userPassword);
                System.out.println("✅ Archivo cifrado exitosamente con contraseña personalizada: " + encryptedFileName + ".lock");
            } else {
                cipherBox.lockFile(sourceFile.getPath(), encryptedFileName);
                System.out.println("✅ Archivo cifrado exitosamente (modo básico): " + encryptedFileName + ".lock");
            }
        } catch (SecurityException e) {
            System.err.println("❌ Error de seguridad: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ Error al cifrar el archivo: " + e.getMessage());
        }
        
        Utils.pauseForKeyPress(scanner);
    }

    // Método para manejar el menú de descifrado
    private static void handleDecryptionMenu(CryptoBox cipherBox, Scanner scanner) {
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                   🔓 MENÚ DE DESCIFRADO 🔓                  ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        
        File encryptedFile = Utils.listFiles(DATA_DIR_ENCRYPT, scanner);
        if (encryptedFile == null) return;
        
        String aliasFromFileName = encryptedFile.getName().split("\\.")[0];
        System.out.println("\n📁 Archivo cifrado seleccionado: " + encryptedFile.getName());
        System.out.print("🏷️  Nombre del archivo descifrado [" + aliasFromFileName + "]: ");
        String decryptedFileName = scanner.nextLine().trim();
        
        if (decryptedFileName.isEmpty()) {
            decryptedFileName = aliasFromFileName;
        }
        
        // Verificar si el archivo tiene contraseña personalizada
        boolean hasCustomPassword = cipherBox.hasStoredPasswords() && 
                                  cipherBox.getStoredAliases().contains(aliasFromFileName);
        
        if (hasCustomPassword) {
            System.out.println("\n🔑 Este archivo fue cifrado con contraseña personalizada.");
            System.out.print("🔐 Ingresa la contraseña para '" + aliasFromFileName + "': ");
            String userPassword = scanner.nextLine();
            
            if (userPassword.trim().isEmpty()) {
                System.out.println("❌ La contraseña no puede estar vacía.");
                Utils.pauseForKeyPress(scanner);
                return;
            }
            
            try {
                DataFile dataCustom = cipherBox.unlockFileWithPassword(encryptedFile.getPath(), aliasFromFileName, userPassword);
                System.out.println("✅ Archivo descifrado exitosamente. Extensión: " + dataCustom.getExtension());
                
                if ("txt".equalsIgnoreCase(dataCustom.getExtension())) {
                    Utils.readFileIfText(dataCustom.getExtension(), dataCustom.getFile(), scanner);
                } else {
                    System.out.println("ℹ️  Archivo guardado en: " + dataCustom.getFile().getPath());
                }
            } catch (SecurityException e) {
                System.err.println("❌ Error de autenticación: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("❌ Error al descifrar: " + e.getMessage());
            }
        } else {
            try {
                DataFile data = cipherBox.unlockFile(encryptedFile.getPath(), decryptedFileName);
                System.out.println("✅ Archivo descifrado exitosamente. Extensión: " + data.getExtension());
                
                if ("txt".equalsIgnoreCase(data.getExtension())) {
                    Utils.readFileIfText(data.getExtension(), data.getFile(), scanner);
                } else {
                    System.out.println("ℹ️  Archivo guardado en: " + data.getFile().getPath());
                }
            } catch (Exception e) {
                System.err.println("❌ Error al descifrar: " + e.getMessage());
                System.err.println("💡 Verifica que tengas las claves correctas o prueba con contraseña personalizada.");
            }
        }
        
        Utils.pauseForKeyPress(scanner);
    }

    // Método para manejar el menú de lectura segura
    private static void handleReadingMenu(CryptoBox cipherBox, Scanner scanner) {
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║               👁️  MENÚ DE LECTURA SEGURA 👁️                ║");
        System.out.println("║                (Solo memoria - Sin archivos)               ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        
        File lockedFile = Utils.listFiles(DATA_DIR_ENCRYPT, scanner);
        if (lockedFile == null) return;
        
        String alias = lockedFile.getName().split("\\.")[0];
        System.out.println("\n📁 Archivo cifrado seleccionado: " + lockedFile.getName());
        
        // Verificar si tiene contraseña personalizada
        boolean hasCustomPassword = cipherBox.hasStoredPasswords() && 
                                  cipherBox.getStoredAliases().contains(alias);
        
        String content = null;
        String extension = "";
        
        try {
            if (hasCustomPassword) {
                System.out.println("\n🔑 Este archivo requiere contraseña personalizada.");
                System.out.print("🔐 Ingresa la contraseña para '" + alias + "': ");
                String userPassword = scanner.nextLine();
                
                if (userPassword.trim().isEmpty()) {
                    System.out.println("❌ La contraseña no puede estar vacía.");
                    Utils.pauseForKeyPress(scanner);
                    return;
                }
                
                extension = cipherBox.decryptExtensionWithPassword(alias, userPassword);
                content = cipherBox.readFileSecurelyWithPassword(lockedFile.getPath(), alias, userPassword);
            } else {
                extension = cipherBox.decryptExtension(alias);
                content = cipherBox.readFileSecurely(lockedFile.getPath(), alias);
            }
            
            System.out.println("\n📄 Extensión original: " + extension);
            System.out.println("\n📖 CONTENIDO DEL ARCHIVO (solo lectura):");
            System.out.println("═".repeat(60));
            
            if ("txt".equalsIgnoreCase(extension) || extension.isEmpty()) {
                String[] lines = content.split("\n");
                for (int i = 0; i < lines.length; i++) {
                    System.out.printf("%3d: %s\n", i + 1, lines[i]);
                }
            } else {
                if (content.length() > 500) {
                    System.out.println(content.substring(0, 500) + "...");
                    System.out.println("\n[Contenido truncado - " + content.length() + " caracteres totales]");
                } else {
                    System.out.println(content);
                }
            }
            
            System.out.println("═".repeat(60));
            System.out.println("✅ Lectura completada. No se crearon archivos temporales.");
            
        } catch (SecurityException e) {
            System.err.println("❌ Error de autenticación: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ Error al leer el archivo: " + e.getMessage());
        }
        
        Utils.pauseForKeyPress(scanner);
    }

    // Método para manejar conversión de extensiones
    private static void handleExtensionConversion(CryptoBox cipherBox, Scanner scanner) {
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║              🔧 CAMBIAR EXTENSIÓN DE ARCHIVO 🔧             ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        
        File unlockedFile = Utils.listFiles(DATA_DIR_DECRYPT, scanner);
        if (unlockedFile == null) return;
        
        String alias = unlockedFile.getName().split("\\.")[0];
        
        try {
            String extension = cipherBox.decryptExtension(alias);
            System.out.println("\n📄 Extensión original del archivo: " + extension);
            System.out.print("¿Deseas cambiar la extensión? [S/n]: ");
            String choice = scanner.nextLine().toLowerCase().trim();
            
            if (choice.equals("s") || choice.equals("si") || choice.equals("yes")) {
                Utils.convertExtension(unlockedFile, extension);
            } else {
                System.out.println("ℹ️  Operación cancelada.");
            }
        } catch (Exception e) {
            System.err.println("❌ Error al procesar el archivo: " + e.getMessage());
        }
        
        Utils.pauseForKeyPress(scanner);
    }
}