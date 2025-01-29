package com.TP.server;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Server {

    private static final String USERNAME = "miage";
    private static final String PASSWORD = "sepideh";

    public static void main(String[] args) throws IOException {

        int port = 2121;

        ServerSocket controlSocket = new ServerSocket(port);
        System.out.println("Server ready to accept connections on port " + port);

        Socket socket = controlSocket.accept();
        InputStream input = socket.getInputStream();
        OutputStream output = socket.getOutputStream();
        Scanner scanner = new Scanner(input);
        PrintWriter writer = new PrintWriter(output, true);

        writer.println("220 Service ready.");

        boolean authenticated = false;
        String inputUser = null;
        
        while (scanner.hasNextLine()) {
            String message = scanner.nextLine();

            // Handle OPTS (special case for Windows clients)
            if (message.startsWith("OPTS")) {
                writer.println("200 OPTS command ignored.");
                continue;
            }

            if (message.startsWith("USER")) {
                inputUser = message.substring(5).trim();
                if (USERNAME.equals(inputUser)) {
                    writer.println("331 User name ok.");
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
                    writer.println("230 User logged in.");
                    System.out.println("PASS " + PASSWORD);
                } else {
                    writer.println("430 Invalid password.");
                    break;
                }
                continue;
            }

            if (message.equalsIgnoreCase("QUIT")) {
                writer.println("221 Service closing connection.");
                System.out.println("QUIT");
                break;
            }

            if (!authenticated) {
                writer.println("530 Not logged in.");
                continue;
            }

            if (message.startsWith("RETR")) {
                String inputFile = message.substring(5).trim();
                File file = new File(System.getProperty("user.dir"), inputFile);
                System.out.println("RETR " + inputFile);

                if (!file.exists() || !file.isFile()) {
                    writer.println("550 File not found.");
                    continue;
                }

                try (ServerSocket passiveServerSocket = new ServerSocket(0)) {
                    int passivePort = passiveServerSocket.getLocalPort();
                    writer.println("227 Entering Extended Passive Mode (|||" + passivePort + "|).\r\n");
                    
                    Socket dataSocket = passiveServerSocket.accept();  

                    try (InputStream fileInput = new FileInputStream(file);
                         OutputStream dataOutput = dataSocket.getOutputStream()) {

                        writer.println("150 Opening data connection.");
                        
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = fileInput.read(buffer)) != -1) {
                            dataOutput.write(buffer, 0, bytesRead);
                        }

                        dataOutput.flush();
                        writer.println("226 Transfer complete.");
                    }
                } catch (IOException e) {
                    writer.println("550 Error transferring file.");
                }
                continue;
            }

            if (message.startsWith("CWD")) {
                String inputDir = message.substring(4).trim();
                File dir = new File(inputDir);

                if (dir.exists() && dir.isDirectory()) {
                    System.setProperty("user.dir", inputDir);
                    writer.println("250 Directory successfully changed.");
                    System.out.println("CWD " + inputDir);
                } else {
                    writer.println("550 Failed to change directory.");
                }
                continue;
            }

            if (message.equalsIgnoreCase("LIST")) {
                File currentDir = new File(System.getProperty("user.dir"));
                File[] files = currentDir.listFiles();
                System.out.println("LIST");

                try (ServerSocket passiveServerSocket = new ServerSocket(0)) {
                    int passivePort = passiveServerSocket.getLocalPort();
                    writer.println("227 Entering Extended Passive Mode (|||" + passivePort + "|).\r\n");
                    Socket dataSocket = passiveServerSocket.accept();

                    try (PrintWriter dataWriter = new PrintWriter(dataSocket.getOutputStream(), true)) {
                        writer.println("150 Opening data connection.");
                        
                        if (files != null && files.length > 0) {
                            for (File file : files) {
                                String fileType = file.isDirectory() ? "d" : "-";
                                dataWriter.println(fileType + " " + file.getName());
                            }
                            writer.println("226 Directory listing complete.");
                        } else {
                            writer.println("550 No files in directory.");
                        }
                    }
                } catch (IOException e) {
                    writer.println("550 Error listing directory.");
                }
                continue;
            }

            else {
                  writer.println("502 Command not implemented.");
                  writer.flush();  }
        }


        socket.close();
        controlSocket.close();
        scanner.close();
    }
}
