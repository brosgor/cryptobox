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
                System.out.println("\nSelecciona una opción:");
                System.out.println("1. Cifrar un archivo (modo básico)");
                System.out.println("2. Cifrar un archivo con contraseña personalizada");
                System.out.println("3. Descifrar un archivo (modo básico)");
                System.out.println("4. Descifrar un archivo con contraseña personalizada");
                System.out.println("5. Leer un archivo .lock en la consola");
                System.out.println("6. Cambiar la extensión de un archivo .unlocked");
                System.out.println("7. Gestionar contraseñas");
                System.out.println("8. Ver aliases almacenados");
                System.out.println("9. Salir");

                String option = scanner.nextLine();

                switch (option) {
                    case "1":
                        Utils.clearConsole();
                        File sourceFile = Utils.listFiles(ORIGINALS_DIR, scanner);
                        if (sourceFile != null) {
                            System.out.print("Ingresa el nombre del archivo cifrado (sin extensión): ");
                            String encryptedFileName = scanner.nextLine();
                            cipherBox.lockFile(sourceFile.getPath(), encryptedFileName);
                            System.out.println("Archivo cifrado exitosamente.");
                            Utils.pauseForKeyPress(scanner);
                        }
                        break;
                    case "2":
                        Utils.clearConsole();
                        File sourceFileCustom = Utils.listFiles(ORIGINALS_DIR, scanner);
                        if (sourceFileCustom != null) {
                            System.out.print("Ingresa el nombre del archivo cifrado (sin extensión): ");
                            String encryptedFileNameCustom = scanner.nextLine();
                            System.out.print("Ingresa tu contraseña personalizada: ");
                            String userPassword = scanner.nextLine();
                            cipherBox.lockFileWithPassword(sourceFileCustom.getPath(), encryptedFileNameCustom, userPassword);
                            System.out.println("Archivo cifrado exitosamente con contraseña personalizada.");
                            Utils.pauseForKeyPress(scanner);
                        }
                        break;
                    case "3":
                        Utils.clearConsole();
                        File encryptedFile = Utils.listFiles(DATA_DIR_ENCRYPT, scanner);
                        if (encryptedFile != null) {
                            System.out.print("Ingresa el nombre del archivo descifrado (con extensión): ");
                            String decryptedFileName = scanner.nextLine();
                            DataFile data = cipherBox.unlockFile(encryptedFile.getPath(), decryptedFileName);
                            System.out.println("Archivo descifrado exitosamente. La extensión del archivo es: " +
                                    data.getExtension());

                            if ("txt".equalsIgnoreCase(data.getExtension())) {
                                Utils.readFileIfText(data.getExtension(), data.getFile(), scanner);
                            } else {
                                Utils.pauseForKeyPress(scanner);
                            }
                        }
                        break;
                    case "4":
                        Utils.clearConsole();
                        File encryptedFileCustom = Utils.listFiles(DATA_DIR_ENCRYPT, scanner);
                        if (encryptedFileCustom != null) {
                            String aliasFromFile = encryptedFileCustom.getName().split("\\.")[0];
                            System.out.print("Ingresa tu contraseña para el alias '" + aliasFromFile + "': ");
                            String userPasswordDecrypt = scanner.nextLine();
                            
                            try {
                                DataFile dataCustom = cipherBox.unlockFileWithPassword(encryptedFileCustom.getPath(), aliasFromFile, userPasswordDecrypt);
                                System.out.println("Archivo descifrado exitosamente. La extensión del archivo es: " +
                                        dataCustom.getExtension());

                                if ("txt".equalsIgnoreCase(dataCustom.getExtension())) {
                                    Utils.readFileIfText(dataCustom.getExtension(), dataCustom.getFile(), scanner);
                                } else {
                                    Utils.pauseForKeyPress(scanner);
                                }
                            } catch (SecurityException e) {
                                System.out.println("Error: " + e.getMessage());
                                Utils.pauseForKeyPress(scanner);
                            }
                        }
                        break;
                    case "5":
                        Utils.clearConsole();
                        File lockedFile = Utils.listFiles(DATA_DIR_ENCRYPT, scanner);
                        if (lockedFile != null) {
                            String alias = lockedFile.getName().split("\\.")[0];
                            DataFile data = cipherBox.unlockFile(lockedFile.getPath(), alias);
                            String extension = data.getExtension();
                            File unlockedFile = data.getFile();
                            System.out.println("Archivo descifrado exitosamente. La extensión del archivo es: " +
                                    data.getExtension());
                            if ("txt".equalsIgnoreCase(data.getExtension())
                                    || "unlocked".equalsIgnoreCase(data.getExtension())) {
                                Utils.readFileIfText(extension, unlockedFile, scanner);
                                // Leer el contenido del archivo .unlocked
                                byte[] fileContent = Files.readAllBytes(unlockedFile.toPath());

                                // Decodificar el contenido desde Base64
                                byte[] decodedContent = Base64.getDecoder().decode(fileContent);

                                // Escribir el contenido decodificado de nuevo en el archivo
                                Files.write(unlockedFile.toPath(), decodedContent);

                                // Continuar con el proceso de cifrado y eliminación del archivo
                                cipherBox.lockFile(unlockedFile.getPath(), alias);
                                unlockedFile.delete();

                            } else {
                                Utils.pauseForKeyPress(scanner);
                            }
                        }
                        break;
                    case "6":
                        Utils.clearConsole();
                        File unlockedFile = Utils.listFiles(DATA_DIR_DECRYPT, scanner);
                        String alias = unlockedFile.getName().split("\\.")[0];
                        if (unlockedFile != null) {
                            String extension = cipherBox.decryptExtension(alias);
                            System.out.print(
                                    "La extensión original del archivo es: " + extension
                                            + ". ¿Deseas cambiarla? (s/n): ");
                            String choice2 = scanner.nextLine();
                            if (choice2.equalsIgnoreCase("s")) {
                                Utils.convertExtension(unlockedFile, extension);
                            }
                        }
                        break;
                    case "7":
                        Utils.clearConsole();
                        handlePasswordManagement(cipherBox, scanner);
                        break;
                    case "8":
                        Utils.clearConsole();
                        displayStoredAliases(cipherBox, scanner);
                        break;
                    case "9":
                        Utils.clearConsole();
                        System.out.println("Cerrando conexiones y saliendo del programa...");
                        cipherBox.close();
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
}