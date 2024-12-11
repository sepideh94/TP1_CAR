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
            System.out.println("Serveur est ici pour accepter des connexion sur le port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();  
                System.out.println("Client connecté : " + clientSocket.getInetAddress());
                
                new ClientHandler(clientSocket).start();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


static class ClientHandler extends Thread {
    private Socket clientSocket;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }
    
public void run() {
    try (
        BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)
    ) {
        writer.println("Entrez votre nom d'utilisateur : ");
        String username = reader.readLine();  

        writer.println("Entrez votre mot de passe : ");
        String password = reader.readLine();  

        System.out.println("Nom d'utilisateur : " + username);
        System.out.println("Mot de passe : " + password);

        writer.println("Nom d'utilisateur et mot de passe reçus.");

    } catch (IOException e) {
        e.printStackTrace();
    } finally {
        try {
            clientSocket.close();  
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

	
	
	
	
}
