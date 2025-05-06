package org.example;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer; // Added this import
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
    private JTable transactionTable;
    private JLabel statusLabel;
    private JLabel profileLabel;

    public static void main(String[] args) {
        try {
            // Set system look and feel for better integration
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

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
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);

        JPanel container = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gp = new GradientPaint(0, 0, new Color(44, 62, 80), 0, getHeight(), new Color(52, 152, 219));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        container.setLayout(new BorderLayout(15, 15));
        container.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Secure Banking System", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.CENTER);
        container.add(header, BorderLayout.NORTH);

        statusLabel = new JLabel("Ready", SwingConstants.RIGHT);
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        header.add(statusLabel, BorderLayout.EAST);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setOpaque(false);
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 100), 1),
                new EmptyBorder(15, 15, 15, 15)));

        mainPanel.add(createLoginPanel(), "login");
        mainPanel.add(createDashboardPanel(), "dashboard");

        container.add(mainPanel, BorderLayout.CENTER);
        frame.add(container);
        frame.setVisible(true);
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 150)),
                new EmptyBorder(30, 40, 30, 40)
        ));
        formPanel.setBackground(new Color(255, 255, 255, 30));
        formPanel.setOpaque(true);

        JLabel userLabel = new JLabel("Username:");
        userLabel.setForeground(Color.WHITE);
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 24));
        JLabel passLabel = new JLabel("Password:");
        passLabel.setForeground(Color.WHITE);
        passLabel.setFont(new Font("Segoe UI", Font.PLAIN, 24));
        JTextField userField = new JTextField(20);
        userField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JPasswordField passField = new JPasswordField(20);
        passField.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JButton loginButton = createStyledButton("Login");
        loginButton.setToolTipText("Log in to your account");
        JButton registerButton = createStyledButton("Register");
        registerButton.setToolTipText("Create a new account");
        JButton clearButton = createStyledButton("Clear");
        clearButton.setToolTipText("Clear input fields");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(userLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        formPanel.add(userField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(passLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        formPanel.add(passField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);
        formPanel.add(buttonPanel, gbc);

        gbc.gridy = 3;
        formPanel.add(clearButton, gbc);

        loginButton.addActionListener(e -> {
            if (validateInputs(userField, passField)) {
                showLoading(true);
                sendCommand("LOGIN:" + userField.getText().trim() + ":" + new String(passField.getPassword()));
            }
        });
        registerButton.addActionListener(e -> {
            if (validateInputs(userField, passField)) {
                showLoading(true);
                sendCommand("REGISTER:" + userField.getText().trim() + ":" + new String(passField.getPassword()));
            }
        });
        clearButton.addActionListener(e -> {
            userField.setText("");
            passField.setText("");
        });

        GridBagConstraints mainGbc = new GridBagConstraints();
        mainGbc.gridx = 0;
        mainGbc.gridy = 0;
        panel.add(formPanel, mainGbc);

        return panel;
    }

    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel profilePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        profilePanel.setOpaque(false);
        profileLabel = new JLabel("Account: Not logged in");
        profileLabel.setForeground(Color.WHITE);
        profileLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        profilePanel.add(profileLabel);

        JPanel buttonPanel = new JPanel(new GridLayout(2, 3, 15, 15));
        buttonPanel.setOpaque(false);
        JButton depositButton = createStyledButton("Deposit");
        depositButton.setToolTipText("Deposit money into your account");
        JButton withdrawButton = createStyledButton("Withdraw");
        withdrawButton.setToolTipText("Withdraw money from your account");
        JButton balanceButton = createStyledButton("Check Balance");
        balanceButton.setToolTipText("View your current balance");
        JButton transactionsButton = createStyledButton("View Transactions");
        transactionsButton.setToolTipText("View your transaction history");
        JButton logoutButton = createStyledButton("Logout");
        logoutButton.setToolTipText("Log out of your account");
        JButton exitButton = createStyledButton("Exit");
        exitButton.setToolTipText("Close the application");

        buttonPanel.add(depositButton);
        buttonPanel.add(withdrawButton);
        buttonPanel.add(balanceButton);
        buttonPanel.add(transactionsButton);
        buttonPanel.add(logoutButton);
        buttonPanel.add(exitButton);

        // Create the transaction table with appropriate column names
        String[] columnNames = {"Date", "Type", "Amount"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make cells non-editable
            }
        };

        transactionTable = new JTable(model);
        transactionTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        transactionTable.setRowHeight(30);
        transactionTable.setFillsViewportHeight(true);
        transactionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane tableScrollPane = new JScrollPane(transactionTable);
        tableScrollPane.setBorder(BorderFactory.createLineBorder(new Color(255, 255, 255, 100)));
        tableScrollPane.setBackground(new Color(255, 255, 255, 50));

        // Style table header
        JTableHeader header = transactionTable.getTableHeader();
        header.setBackground(new Color(220, 53, 69));
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 16));
        header.setReorderingAllowed(false);
        header.setResizingAllowed(true);

        // Style table cells
        transactionTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(240, 240, 240));
                }
                setHorizontalAlignment(column == 2 ? SwingConstants.RIGHT : SwingConstants.LEFT);
                return c;
            }
        });
        transactionTable.setGridColor(new Color(200, 200, 200));
        transactionTable.setShowGrid(true);

        // Create a panel for the transaction display with a title
        JPanel transactionPanel = new JPanel(new BorderLayout(0, 10));
        transactionPanel.setOpaque(false);

        JLabel transactionTitle = new JLabel("Transaction History");
        transactionTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        transactionTitle.setForeground(Color.WHITE);

        transactionPanel.add(transactionTitle, BorderLayout.NORTH);
        transactionPanel.add(tableScrollPane, BorderLayout.CENTER);

        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setOpaque(false);
        topPanel.add(profilePanel, BorderLayout.NORTH);
        topPanel.add(buttonPanel, BorderLayout.CENTER);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(transactionPanel, BorderLayout.CENTER);

        depositButton.addActionListener(e -> {
            String amt = JOptionPane.showInputDialog(frame, "Enter deposit amount:");
            if (amt != null && !amt.trim().isEmpty() && amt.matches("\\d+(\\.\\d{1,2})?")) {
                showLoading(true);
                sendCommand("DEPOSIT:" + amt.trim());
            } else if (amt != null) {
                showError("Invalid amount format.");
            }
        });

        withdrawButton.addActionListener(e -> {
            String amt = JOptionPane.showInputDialog(frame, "Enter withdrawal amount:");
            if (amt != null && !amt.trim().isEmpty() && amt.matches("\\d+(\\.\\d{1,2})?")) {
                showLoading(true);
                sendCommand("WITHDRAW:" + amt.trim());
            } else if (amt != null) {
                showError("Invalid amount format.");
            }
        });

        balanceButton.addActionListener(e -> {
            showLoading(true);
            sendCommand("BALANCE");
        });

        // Fixed the transactions button to not include account number since server already knows it
        transactionsButton.addActionListener(e -> {
            showLoading(true);
            sendCommand("TRANSACTIONS");
        });

        logoutButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(frame, "Are you sure you want to logout?", "Confirm Logout", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                accountNumber = null;
                profileLabel.setText("Account: Not logged in");
                clearTransactionTable();
                cardLayout.show(mainPanel, "login");
            }
        });
        exitButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(frame, "Are you sure you want to exit?", "Confirm Exit", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                sendCommand("EXIT");
                try {
                    socket.close();
                } catch (IOException ex) {
                    showError("Error closing socket: " + ex.getMessage());
                }
                System.exit(0);
            }
        });

        return panel;
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color color;
                if (getModel().isPressed()) {
                    color = new Color(200, 35, 51);
                } else if (getModel().isRollover()) {
                    color = new Color(200, 35, 51);
                } else {
                    color = getBackground();
                }
                g2.setColor(color);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.dispose();
                super.paintComponent(g);
            }

            @Override
            protected void paintBorder(Graphics g) {
                // No border painting
            }
        };
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setBackground(new Color(220, 53, 69));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBorder(BorderFactory.createEmptyBorder(12, 30, 12, 30));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private boolean validateInputs(JTextField userField, JPasswordField passField) {
        String username = userField.getText().trim();
        String password = new String(passField.getPassword()).trim();
        if (username.isEmpty() || password.isEmpty()) {
            showError("Username and password cannot be empty.");
            return false;
        }
        if (!username.matches("[a-zA-Z0-9]{3,20}")) {
            showError("Username must be 3-20 alphanumeric characters.");
            return false;
        }
        return true;
    }

    private void sendCommand(String cmd) {
        try {
            output.writeUTF(cmd);
        } catch (IOException e) {
            showError("Failed to send command: " + e.getMessage());
            showLoading(false);
        }
    }

    private void listenToServer() {
        new Thread(() -> {
            try {
                while (true) {
                    String message = input.readUTF().trim();
                    System.out.println("Server message: " + message); // Debug message

                    if (message.startsWith("SUCCESS: Logged in.")) {
                        accountNumber = message.split("Account: ")[1];
                        SwingUtilities.invokeLater(() -> {
                            profileLabel.setText("Account: " + accountNumber);
                            cardLayout.show(mainPanel, "dashboard");
                            showLoading(false);
                        });
                    } else if (message.startsWith("TRANSACTIONS:")) {
                        SwingUtilities.invokeLater(() -> {
                            updateTransactionTable(message.replace("TRANSACTIONS:", "").trim());
                            showLoading(false);
                        });
                    } else {
                        SwingUtilities.invokeLater(() -> {
                            showMessage(message);
                            showLoading(false);
                        });
                    }
                }
            } catch (IOException e) {
                SwingUtilities.invokeLater(() -> {
                    showError("Connection lost: " + e.getMessage());
                    showLoading(false);
                });
            }
        }).start();
    }

    private void updateTransactionTable(String transactions) {
        DefaultTableModel model = (DefaultTableModel) transactionTable.getModel();
        model.setRowCount(0); // Clear existing rows

        if (transactions.isEmpty()) {
            // Show empty message in table
            model.addRow(new Object[]{"No transactions found", "", ""});
            return;
        }

        // Handle different possible transaction formats
        if (transactions.contains(";")) {
            // Format: date;type;amount format
            for (String transaction : transactions.split(";")) {
                if (transaction.trim().isEmpty()) continue;

                String[] parts = transaction.split(",");
                if (parts.length >= 3) {
                    model.addRow(new Object[]{parts[0], parts[1], parts[2]});
                }
            }
        } else if (transactions.contains("\n")) {
            // Format: Line by line with whitespace
            for (String line : transactions.split("\n")) {
                if (line.trim().isEmpty()) continue;

                // Try to parse each line into components
                String date = "N/A";
                String type = "N/A";
                String amount = "N/A";

                if (line.contains("DEPOSIT")) {
                    type = "DEPOSIT";
                    String[] parts = line.split("DEPOSIT");
                    if (parts.length > 0) {
                        date = parts[0].trim();
                    }
                    if (parts.length > 1) {
                        amount = parts[1].trim();
                    }
                } else if (line.contains("WITHDRAW")) {
                    type = "WITHDRAW";
                    String[] parts = line.split("WITHDRAW");
                    if (parts.length > 0) {
                        date = parts[0].trim();
                    }
                    if (parts.length > 1) {
                        amount = parts[1].trim();
                    }
                } else {
                    // Just add the whole line as type
                    type = line.trim();
                }

                model.addRow(new Object[]{date, type, amount});
            }
        } else {
            // Just show the raw text in the first column if we can't parse it
            model.addRow(new Object[]{transactions, "", ""});
        }

        // Resize columns to fit content
        for (int column = 0; column < transactionTable.getColumnCount(); column++) {
            int width = 15;
            for (int row = 0; row < transactionTable.getRowCount(); row++) {
                TableCellRenderer renderer = transactionTable.getCellRenderer(row, column);
                Component comp = transactionTable.prepareRenderer(renderer, row, column);
                width = Math.max(comp.getPreferredSize().width + 20, width);
            }
            transactionTable.getColumnModel().getColumn(column).setPreferredWidth(width);
        }
    }

    private void clearTransactionTable() {
        DefaultTableModel model = (DefaultTableModel) transactionTable.getModel();
        model.setRowCount(0);
    }

    private void showMessage(String message) {
        int messageType = JOptionPane.INFORMATION_MESSAGE;
        String title = "Message";

        if (message.startsWith("ERROR") || message.contains("failed") || message.contains("invalid")) {
            messageType = JOptionPane.ERROR_MESSAGE;
            title = "Error";
        } else if (message.startsWith("SUCCESS")) {
            title = "Success";
        }

        JOptionPane.showMessageDialog(frame, message, title, messageType);
    }

    private void showError(String error) {
        JOptionPane.showMessageDialog(frame, error, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showLoading(boolean loading) {
        statusLabel.setText(loading ? "Processing..." : "Ready");
        statusLabel.setForeground(loading ? new Color(255, 204, 0) : Color.WHITE);
    }
}