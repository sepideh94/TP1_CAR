package com.TP.server;

import java.io.*;
import java.net.*;
import java.util.*;

public class Server {

	 private static final String[] user = {"miage"};
	 private static final String[] password = {"sepideh"};

    public static void main(String[] args) throws IOException {
        int port = 2121;

        ServerSocket server = new ServerSocket(port);
        System.out.println("Serveur prêt à accepter des connexions sur le port " + port);

        Socket socket = server.accept();

        InputStream input = socket.getInputStream();
        OutputStream output = socket.getOutputStream();

        output.write("220 Service ready.\r\n".getBytes());
        output.write("username:\r\n".getBytes());

        Scanner scanner = new Scanner(input);
        String message = scanner.nextLine();

        if (user[0].equals(message)) {
            output.write("331 User name okay, need password.\r\n".getBytes());
            System.out.println("USER " + user[0] );
        } else {
            output.write("430 Invalid username.\r\n".getBytes());
            socket.close();
            return;
        }

        output.write("password:\r\n".getBytes());
        message = scanner.nextLine();

        if (password[0].equals(message)) {
            output.write("230 User logged in, proceed.\r\n".getBytes());
            System.out.println("PASS " + password[0] );
        } else {
            output.write("430 Invalid password.\r\n".getBytes());
            socket.close();
            return;
        }

        while ((message = scanner.nextLine()) != null) {
            if (message.equalsIgnoreCase("QUIT")) {
                output.write("221 Service closed.\r\n".getBytes());
                System.out.println("QUIT" );
                break;
            } else {
                output.write("502 Command not implemented.\r\n".getBytes());
            }
        }

        socket.close();
        server.close();
    }
}