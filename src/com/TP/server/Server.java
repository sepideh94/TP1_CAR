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
            System.out.println("Serveur en attente de connexions sur le port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connecté : " + clientSocket.getInetAddress());

                new ClientHandler(clientSocket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler extends Thread {
        private final Socket clientSocket;
        private BufferedReader reader;
        private PrintWriter writer;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try {
                reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                writer = new PrintWriter(clientSocket.getOutputStream(), true);


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
                            writer.println("230 Login successful.");
                        } else {
                            writer.println("530 Login incorrect.");
                        }
                    }
                    else if (command.equals("QUIT")) {
                        writer.println("221 Deconnexion.");
                        break;
                    }
                    else {
                        writer.println("502 Command not implemented.");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (clientSocket != null && !clientSocket.isClosed()) {
                        clientSocket.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
