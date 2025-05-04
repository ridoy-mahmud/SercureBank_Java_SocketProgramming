package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientGUI {
    private Socket socket;
    private DataInputStream input;
    private DataOutputStream output;
    private JFrame frame;
    private JPanel mainPanel;
    private CardLayout cardLayout;
    private JTextArea outputArea;
    private String accountNumber = null;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ClientGUI::new);
    }

    public ClientGUI() {
        try {
            socket = new Socket("localhost", 4000);
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());
            createGUI();
            listenToServer();
        } catch (IOException e) {
            showError("Connection Error: " + e.getMessage());
        }
    }

    private void createGUI() {
        frame = new JFrame("Secure Banking System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        mainPanel.add(createLoginPanel(), "login");
        mainPanel.add(createDashboardPanel(), "dashboard");

        frame.add(mainPanel);
        frame.setVisible(true);
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel userLabel = new JLabel("Username:");
        JLabel passLabel = new JLabel("Password:");
        JTextField userField = new JTextField(15);
        JPasswordField passField = new JPasswordField(15);
        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(userLabel, gbc);

        gbc.gridx = 1;
        panel.add(userField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(passLabel, gbc);

        gbc.gridx = 1;
        panel.add(passField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(loginButton, gbc);

        gbc.gridx = 1;
        panel.add(registerButton, gbc);

        loginButton.addActionListener(e -> sendCommand("LOGIN:" + userField.getText().trim() + ":" + new String(passField.getPassword())));
        registerButton.addActionListener(e -> sendCommand("REGISTER:" + userField.getText().trim() + ":" + new String(passField.getPassword())));

        return panel;
    }

    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel buttonPanel = new JPanel(new GridLayout(2, 3, 10, 10));
        JButton depositButton = new JButton("Deposit");
        JButton withdrawButton = new JButton("Withdraw");
        JButton balanceButton = new JButton("Check Balance");
        JButton transactionsButton = new JButton("View Transactions");
        JButton logoutButton = new JButton("Logout");
        JButton exitButton = new JButton("Exit");

        buttonPanel.add(depositButton);
        buttonPanel.add(withdrawButton);
        buttonPanel.add(balanceButton);
        buttonPanel.add(transactionsButton);
        buttonPanel.add(logoutButton);
        buttonPanel.add(exitButton);

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(outputArea);

        panel.add(buttonPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        depositButton.addActionListener(e -> {
            String amt = JOptionPane.showInputDialog(frame, "Enter deposit amount:");
            if (amt != null && !amt.trim().isEmpty()) sendCommand("DEPOSIT:" + amt.trim());
        });

        withdrawButton.addActionListener(e -> {
            String amt = JOptionPane.showInputDialog(frame, "Enter withdrawal amount:");
            if (amt != null && !amt.trim().isEmpty()) sendCommand("WITHDRAW:" + amt.trim());
        });

        balanceButton.addActionListener(e -> sendCommand("BALANCE"));
        transactionsButton.addActionListener(e -> sendCommand("TRANSACTIONS"));
        logoutButton.addActionListener(e -> {
            accountNumber = null;
            showText("Logged out.");
            cardLayout.show(mainPanel, "login");
        });
        exitButton.addActionListener(e -> {
            sendCommand("EXIT");
            try {
                socket.close();
            } catch (IOException ex) {
                showError("Error closing socket: " + ex.getMessage());
            }
            System.exit(0);
        });

        return panel;
    }

    private void sendCommand(String cmd) {
        try {
            output.writeUTF(cmd);
        } catch (IOException e) {
            showError("Failed to send command: " + e.getMessage());
        }
    }

    private void listenToServer() {
        new Thread(() -> {
            try {
                while (true) {
                    String message = input.readUTF().trim();

                    if (message.startsWith("SUCCESS: Logged in.")) {
                        accountNumber = message.split("Account: ")[1];
                        SwingUtilities.invokeLater(() -> cardLayout.show(mainPanel, "dashboard"));
                    } else if (message.startsWith("TRANSACTIONS:")) {
                        showText(message.replace("TRANSACTIONS:", "").trim());
                    } else {
                        showMessage(message);
                    }
                }
            } catch (IOException e) {
                showError("Connection lost: " + e.getMessage());
            }
        }).start();
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(frame, message);
    }

    private void showError(String error) {
        JOptionPane.showMessageDialog(frame, error, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showText(String text) {
        outputArea.setText(text);
    }
}