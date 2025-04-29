package org.example;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;

public class Server_Thread {
    public static void main(String[] args) throws IOException {
        ServerSocket ss = new ServerSocket(4000);
        System.out.println("Banking Server started on port 4000");

        while (true) {
            Socket clientSocket = ss.accept();
            System.out.println("New client connected: " + clientSocket);
            ClientHandler clientHandler = new ClientHandler(clientSocket);
            clientHandler.start();
        }
    }
}

class ClientHandler extends Thread {
    private final Socket clientSocket;
    private DataInputStream input;
    private DataOutputStream output;
    private boolean isAuthenticated = false;
    private String accountNumber = null;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    public void run() {
        try {
            input = new DataInputStream(clientSocket.getInputStream());
            output = new DataOutputStream(clientSocket.getOutputStream());

            while (true) {
                output.writeUTF("Enter command (REGISTER, LOGIN, DEPOSIT, WITHDRAW, BALANCE, TRANSACTIONS, EXIT):");
                String received = input.readUTF();
                String[] parts = received.split(":");
                String command = parts[0].toUpperCase();

                switch (command) {
                    case "REGISTER":
                        handleRegistration(parts);
                        break;
                    case "LOGIN":
                        handleLogin(parts);
                        break;
                    case "DEPOSIT":
                        handleDeposit(parts);
                        break;
                    case "WITHDRAW":
                        handleWithdraw(parts);
                        break;
                    case "BALANCE":
                        handleBalance();
                        break;
                    case "TRANSACTIONS":
                        handleTransactions();
                        break;
                    case "EXIT":
                        clientSocket.close();
                        return;
                    default:
                        output.writeUTF("ERROR: Invalid command");
                }
            }
        } catch (IOException e) {
            System.out.println("Client disconnected: " + clientSocket);
        }
    }

    private void handleRegistration(String[] parts) throws IOException {
        if (parts.length != 3) {
            output.writeUTF("ERROR: Usage - REGISTER:username:password");
            return;
        }
        boolean success = Database.registerUser(parts[1], parts[2]);
        output.writeUTF(success ? "SUCCESS: Registration successful" : "ERROR: Registration failed");
    }

    private void handleLogin(String[] parts) throws IOException {
        if (parts.length != 3) {
            output.writeUTF("ERROR: Usage - LOGIN:username:password");
            return;
        }
        Map<String, String> result = Database.loginUser(parts[1], parts[2]);
        if ("SUCCESS".equals(result.get("status"))) {
            isAuthenticated = true;
            accountNumber = result.get("accountNumber");
            output.writeUTF("SUCCESS: Logged in. Account: " + accountNumber);
        } else {
            output.writeUTF("ERROR: " + result.get("message"));
        }
    }

    private void handleDeposit(String[] parts) throws IOException {
        if (!checkAuth()) return;
        try {
            double amount = Double.parseDouble(parts[1]);
            if (Database.deposit(accountNumber, amount)) {
                output.writeUTF("SUCCESS: Deposited " + amount);
            } else {
                output.writeUTF("ERROR: Deposit failed");
            }
        } catch (Exception e) {
            output.writeUTF("ERROR: Invalid amount");
        }
    }

    private void handleWithdraw(String[] parts) throws IOException {
        if (!checkAuth()) return;
        try {
            double amount = Double.parseDouble(parts[1]);
            if (Database.withdraw(accountNumber, amount)) {
                output.writeUTF("SUCCESS: Withdrew " + amount);
            } else {
                output.writeUTF("ERROR: Withdrawal failed");
            }
        } catch (Exception e) {
            output.writeUTF("ERROR: Invalid amount");
        }
    }

    private void handleBalance() throws IOException {
        if (!checkAuth()) return;
        double balance = Database.getBalance(accountNumber);
        output.writeUTF("SUCCESS: Current balance: " + balance);
    }

    private void handleTransactions() throws IOException {
        if (!checkAuth()) return;
        List<String> transactions = Database.getTransactions(accountNumber);
        output.writeUTF("TRANSACTIONS:\n" + String.join("\n", transactions));
    }

    private boolean checkAuth() throws IOException {
        if (!isAuthenticated) output.writeUTF("ERROR: Authentication required");
        return isAuthenticated;
    }
}