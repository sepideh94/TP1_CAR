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

                String username = null;
                String password = null;
                String command;

                while ((command = reader.readLine()) != null) {
                    System.out.println("Command received: " + command);

                    if (command.startsWith("USER ")) {
                        username = command.substring(5).trim();
                        System.out.println("Nom d'utilisateur reçu : " + username);
                        writer.println("331 User name okay, need password.");
                    } 
                    else if (command.startsWith("PASS ")) {
                        password = command.substring(5).trim();

                        if (username != null && users.containsKey(username) && users.get(username).equals(password)) {
                            writer.println("230 Login successful. Welcome, " + username + "!");
                        } else {
                            writer.println("530 Login incorrect.");
                        }
                    } 
                    else if (command.equalsIgnoreCase("QUIT")) {
                        writer.println("221 Goodbye.");
                        break;
                    } 
                    else {
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

        // Close the socket and other resources
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
