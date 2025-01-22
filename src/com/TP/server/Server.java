package com.TP.server;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Server {

    private static final String USERNAME = "miage";
    private static final String PASSWORD = "sepideh";

    public static void main(String[] args) throws IOException {

        int port = 2121;

        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Server is running in directory: " + System.getProperty("user.dir"));
        System.out.println("Server ready to accept connections on port " + port);

        Socket socket = serverSocket.accept();
        InputStream input = socket.getInputStream();
        OutputStream output = socket.getOutputStream();
        Scanner scanner = new Scanner(input);
        PrintWriter writer = new PrintWriter(output, true);

        writer.println("220 Service ready.");

        boolean authenticated = false;
        String inputUser = null;
        ServerSocket passiveServerSocket = null; 

        while (scanner.hasNextLine()) {
            String message = scanner.nextLine(); 
            System.out.println("Received message: '" + message + "'");

            //  OPTS command for Windows
            if (message.startsWith("OPTS")) {
                writer.println("200 OPTS command ignored.");
                continue;
            }

            if (message.startsWith("USER")) {
                inputUser = message.substring(5).trim();
                if (USERNAME.equals(inputUser)) {
                    writer.println("331 User name okay, need password.");
                    System.out.println("USER " + USERNAME);
                } else {
                    writer.println("430 Invalid username.");
                    break;
                }
                continue;
            }

            if (message.startsWith("PASS")) {
                String inputPass = message.substring(5).trim();
                if (PASSWORD.equals(inputPass) && USERNAME.equals(inputUser)) {
                    authenticated = true;
                    writer.println("230 User logged in, proceed.");
                    System.out.println("PASS " + PASSWORD);
                } else {
                    writer.println("430 Invalid password.");
                    break;
                }
                continue;
            }

            if (message.equalsIgnoreCase("QUIT")) {
                writer.println("221 Service closing control connection.");
                System.out.println("QUIT");
                break;
            }

           
            // get command is RETR in windows
            if (authenticated && message.startsWith("RETR")) {
                String inputFile = message.substring(5).trim();
                File file = new File(inputFile);

                if (!file.exists() || !file.isFile()) {
                    writer.println("550 File not found.");
                    System.out.println("File " + inputFile + " not found.");
                    continue; }
                
                passiveServerSocket = new ServerSocket(0); 
                int passivePort = passiveServerSocket.getLocalPort();

                String serverIp = InetAddress.getLocalHost().getHostAddress();
                String[] ipParts = serverIp.split("\\.");

                int p1 = passivePort / 256;
                int p2 = passivePort % 256;

                writer.println(String.format(
                    "227 Entering Passive Mode (%s,%s,%s,%s,%d,%d)",
                    ipParts[0], ipParts[1], ipParts[2], ipParts[3], p1, p2));
                
                System.out.println("PASV response sent with dynamic port: " + passivePort);

                    writer.println("150 File status okay; about to open data connection.");//blocs here
                    System.out.println("Starting file transfer for: " + inputFile);

                   Socket dataSocket =  passiveServerSocket.accept(); 

                    System.out.println("accepted");

                    InputStream fileInput = new FileInputStream(file);
                    OutputStream dataOutput = dataSocket.getOutputStream();

                    
                    int data; 
                    while ((data = fileInput.read()) != -1) { 
                        dataOutput.write(data); 
                    }

                    writer.println("226 Transfer complete.");
                    System.out.println("File " + inputFile + " successfully sent to the client.");

                    if (passiveServerSocket != null && !passiveServerSocket.isClosed()) {
                        passiveServerSocket.close();
                        System.out.println("Passive server socket closed.");
                    }

                    continue;  
            }


            if (!authenticated) {
                writer.println("530 Not logged in.");
            } else {
                writer.println("502 Command not implemented.");
            }
        }

        socket.close();
        serverSocket.close();
    }
}

