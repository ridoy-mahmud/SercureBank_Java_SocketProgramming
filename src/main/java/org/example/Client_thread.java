package org.example;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client_thread {
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost", 4000);
        DataInputStream input = new DataInputStream(socket.getInputStream());
        DataOutputStream output = new DataOutputStream(socket.getOutputStream());
        Scanner scanner = new Scanner(System.in);

        while (true) {
            String serverPrompt = input.readUTF();
            System.out.println(serverPrompt);

            String userInput = scanner.nextLine().trim();
            String[] parts = userInput.split(" ");
            String command = String.join(":", parts);

            output.writeUTF(command);
            if (command.equalsIgnoreCase("EXIT")) break;

            String response = input.readUTF();
            System.out.println(response);
        }
        socket.close();
    }
}