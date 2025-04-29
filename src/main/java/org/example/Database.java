package org.example;

import java.sql.*;
import java.util.*;
import org.mindrot.jbcrypt.BCrypt;

public class Database {
    private static final String URL = "jdbc:mysql://localhost:3306/secure_bank";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static boolean registerUser(String username, String password) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            conn.setAutoCommit(false);

            if (userExists(conn, username)) return false;

            int userId = insertUser(conn, username, password);
            if (userId == -1) return false;

            String accountNumber = generateAccountNumber();
            if (!createAccount(conn, userId, accountNumber)) return false;

            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static Map<String, String> loginUser(String username, String enteredPassword) {
        Map<String, String> result = new HashMap<>();
        String sql = "SELECT u.password, a.account_number FROM users u JOIN accounts a ON u.user_id = a.user_id WHERE u.username = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password");
                String accountNumber = rs.getString("account_number");

                if (BCrypt.checkpw(enteredPassword, storedHash)) {
                    result.put("status", "SUCCESS");
                    result.put("accountNumber", accountNumber);
                } else {
                    result.put("status", "FAILURE");
                    result.put("message", "Invalid password");
                }
            } else {
                result.put("status", "FAILURE");
                result.put("message", "User not found");
            }
        } catch (SQLException e) {
            result.put("status", "ERROR");
            result.put("message", "Database error");
        }
        return result;
    }

    public static boolean deposit(String accountNumber, double amount) {
        String sql = "UPDATE accounts SET balance = balance + ? WHERE account_number = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, amount);
            stmt.setString(2, accountNumber);
            return stmt.executeUpdate() == 1 && recordTransaction(conn, accountNumber, "DEPOSIT", amount);
        } catch (SQLException e) {
            return false;
        }
    }

    public static boolean withdraw(String accountNumber, double amount) {
        String checkBalance = "SELECT balance FROM accounts WHERE account_number = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            PreparedStatement checkStmt = conn.prepareStatement(checkBalance);
            checkStmt.setString(1, accountNumber);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next() && rs.getDouble("balance") >= amount) {
                String update = "UPDATE accounts SET balance = balance - ? WHERE account_number = ?";
                PreparedStatement updateStmt = conn.prepareStatement(update);
                updateStmt.setDouble(1, amount);
                updateStmt.setString(2, accountNumber);
                return updateStmt.executeUpdate() == 1 && recordTransaction(conn, accountNumber, "WITHDRAW", amount);
            }
            return false;
        } catch (SQLException e) {
            return false;
        }
    }

    public static double getBalance(String accountNumber) {
        String sql = "SELECT balance FROM accounts WHERE account_number = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, accountNumber);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getDouble("balance") : -1;
        } catch (SQLException e) {
            return -1;
        }
    }

    public static List<String> getTransactions(String accountNumber) {
        List<String> transactions = new ArrayList<>();
        String sql = "SELECT t.type, t.amount, t.timestamp FROM transactions t JOIN accounts a ON t.account_id = a.account_id WHERE a.account_number = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, accountNumber);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                transactions.add(String.format("%s: %.2f at %s",
                        rs.getString("type"),
                        rs.getDouble("amount"),
                        rs.getTimestamp("timestamp")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transactions;
    }

    private static boolean userExists(Connection conn, String username) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT username FROM users WHERE username = ?");
        stmt.setString(1, username);
        return stmt.executeQuery().next();
    }

    private static int insertUser(Connection conn, String username, String password) throws SQLException {
        String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
        PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        stmt.setString(1, username);
        stmt.setString(2, BCrypt.hashpw(password, BCrypt.gensalt()));
        stmt.executeUpdate();
        ResultSet rs = stmt.getGeneratedKeys();
        return rs.next() ? rs.getInt(1) : -1;
    }

    private static boolean createAccount(Connection conn, int userId, String accountNumber) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO accounts (user_id, account_number) VALUES (?, ?)");
        stmt.setInt(1, userId);
        stmt.setString(2, accountNumber);
        return stmt.executeUpdate() == 1;
    }

    private static String generateAccountNumber() {
        return String.valueOf(100000 + new Random().nextInt(900000));
    }

    private static boolean recordTransaction(Connection conn, String accountNumber, String type, double amount) throws SQLException {
        String getAccountId = "SELECT account_id FROM accounts WHERE account_number = ?";
        PreparedStatement stmt = conn.prepareStatement(getAccountId);
        stmt.setString(1, accountNumber);
        ResultSet rs = stmt.executeQuery();
        if (!rs.next()) return false;

        String insert = "INSERT INTO transactions (account_id, type, amount) VALUES (?, ?, ?)";
        PreparedStatement insertStmt = conn.prepareStatement(insert);
        insertStmt.setInt(1, rs.getInt("account_id"));
        insertStmt.setString(2, type);
        insertStmt.setDouble(3, amount);
        return insertStmt.executeUpdate() == 1;
    }
}

//mysql-connector-j-9.3.0.jar