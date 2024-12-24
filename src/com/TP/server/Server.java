package com.TP.server;

import java.io.*;
import java.net.*;
import java.util.*;

public class Server {

    private static final Map<String, String> users = new HashMap<>();

    static {
        users.put("Miage", "Sepideh"); 
    }

    public static void main(String[] args) {
        int port = 2121;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Serveur en attente d'une connexion sur le port " + port);

            Socket clientSocket = serverSocket.accept();
            System.out.println("Client connecté : " + clientSocket.getInetAddress());

            new ClientHandler(clientSocket).run();

            System.out.println("Serveur arrêté après déconnexion du client.");
        } catch (IOException e) {
            System.err.println("Erreur serveur: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static class ClientHandler {
        private final Socket clientSocket;
        private BufferedReader reader;
        private PrintWriter writer;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        public void run() {
            try {
                reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                writer = new PrintWriter(clientSocket.getOutputStream(), true);

                writer.println("220 service ready!");
                writer.println("Please enter your username:");

                String username = null;
                String password = null;

                username = reader.readLine().trim(); 
                System.out.println("Nom d'utilisateur reçu : " + username);

                if (users.containsKey(username)) {
                    writer.println("331 User name okay, need password.");
                } else {
                    writer.println("530 User not found.");
                    return; 
                }

                writer.println("Please enter your password:");

                password = reader.readLine().trim(); 
                System.out.println("Mot de passe reçu.");

                if (users.get(username).equals(password)) {
                    writer.println("230 Login successful. Welcome, " + username + "!");
                } else {
                    writer.println("530 Login incorrect.");
                    return;
                }

                String command;
                while ((command = reader.readLine()) != null) {
                    if (command.equalsIgnoreCase("QUIT")) {
                        writer.println("221 Goodbye.");
                        break;
                    } else if (command.toUpperCase().startsWith("GET")) {
                        handleGetCommand(command);
                    } else {
                        writer.println("502 Command not implemented.");
                    }
                }
            } catch (IOException e) {
                System.err.println("Erreur avec le client : " + e.getMessage());
                e.printStackTrace();
            } finally {
                cleanUp();
            }
        }

        private void handleGetCommand(String command) {
            String[] parts = command.split("\\s+");
            if (command.equals("get")){
            	//le problème ici, c'est que si le client fait entrer GET ou get avec 
            	//Space avant ou après, le code, il ne le comprend pas.
                sendAllFiles();
            } else if (parts.length == 2) {
                String fileName = parts[1];
                sendFile(fileName);
            } else {
                writer.println("501 Syntax error.");
            }
        }

        private void sendAllFiles() {
            File directory = new File("data"); 

            File[] files = directory.listFiles();
            if (files == null || files.length == 0) {
                writer.println("226 No files available.");
                System.out.println("Aucun fichier disponible dans le dossier 'data'.");
                return;
            }

            writer.println("150 Opening data connection for all files.");
            System.out.println("Transfert de tous les fichiers...");

            for (File file : files) {
                if (file.isFile()) {
                    writer.println("File: " + file.getName());
                    sendFileContent(file); 
                }
            }

            writer.println("226 All files transferred successfully.");
            System.out.println("Transfert terminé pour tous les fichiers.");
        }

        private void sendFileContent(File file) {
            try (BufferedReader fileReader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = fileReader.readLine()) != null) {
                    writer.println(line); 
                }
            } catch (IOException e) {
                writer.println("550 Error reading file: " + file.getName());
                System.err.println("Erreur lors de la lecture du fichier " + file.getName() + " : " + e.getMessage());
            }
        }
        
        private void sendFile(String fileName) {
            File file = new File("data", fileName); 

            if (!file.exists() || !file.isFile()) {
                writer.println("550 File not found: " + fileName);
                System.err.println("Fichier introuvable : " + fileName);
                return;
            }

            try (BufferedReader fileReader = new BufferedReader(new FileReader(file))) {
                writer.println("150 Opening data connection for file transfer: " + fileName);
                System.out.println("Transfert du fichier : " + fileName);

                String line;
                while ((line = fileReader.readLine()) != null) {
                    writer.println(line); 
                }

                writer.println("226 Transfer complete for file: " + fileName);
                System.out.println("Transfert terminé pour le fichier : " + fileName);
            } catch (IOException e) {
                writer.println("550 Error reading file: " + fileName);
                System.err.println("Erreur lors de la lecture du fichier " + fileName + " : " + e.getMessage());
            }
        }
        
        private void cleanUp() {
            try {
                if (reader != null) reader.close();
                if (writer != null) writer.close();
                if (clientSocket != null && !clientSocket.isClosed()) {
                    clientSocket.close();
                    System.out.println("Connexion fermée.");
                }
            } catch (IOException e) {
                System.err.println("Erreur lors de la fermeture des ressources : " + e.getMessage());
            }
        }
    }
}
