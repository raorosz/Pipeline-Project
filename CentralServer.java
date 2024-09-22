import java.io.IOException;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.sql.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class CentralServer extends UnicastRemoteObject implements BankService {
    private static final String DB_URL = "jdbc:mysql://127.0.0.1:3306";
    private static final String USER = "root";
    private static final String PASSWORD = "P@ssword";

    protected CentralServer() throws RemoteException {}

    public static void main(String[] args) {
        try {
            // Load the MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("MySQL Driver Registered!");

            // Establish a connection to the BankSystem database
            Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);
            CentralServer server = new CentralServer();
            server.initializeDatabase(connection); // Initialize the database and tables

            Registry registry = LocateRegistry.createRegistry(1099);
            registry.bind("BankService", server);

            System.out.println("Central Server is running...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method to initialize the database by executing SQL from DBinit.sql
    private void initializeDatabase(Connection connection) throws SQLException, IOException {
        String sql = new String(Files.readAllBytes(Paths.get("DBinit.sql")));
        try (Statement statement = connection.createStatement()) {
            for (String sqlStatement : sql.split(";")) {
                if (!sqlStatement.trim().isEmpty()) {
                    statement.execute(sqlStatement.trim() + ";");
                }
            }
            System.out.println("Database and tables initialized successfully.");
        }
    }

    @Override
    public String createAccount(String name, double initialBalance) throws RemoteException {
        String query = "INSERT INTO BankSystem.accounts (name, balance) VALUES (?, ?)";
        try (Connection connection = DriverManager.getConnection(DB_URL + "/BankSystem", USER, PASSWORD);
             PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, name);
            pstmt.setDouble(2, initialBalance);
            pstmt.executeUpdate();
            return "Account created successfully for " + name;
        } catch (SQLException e) {
            return "Error creating account: " + e.getMessage();
        }
    }

    @Override
    public String depositMoney(int accountNumber, double amount) throws RemoteException {
        String query = "UPDATE BankSystem.accounts SET balance = balance + ? WHERE account_number = ?";
        try (Connection connection = DriverManager.getConnection(DB_URL + "/BankSystem", USER, PASSWORD);
             PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setDouble(1, amount);
            pstmt.setInt(2, accountNumber);
            int rowsUpdated = pstmt.executeUpdate();
            return rowsUpdated > 0 ? "Successfully deposited $" + amount : "Account not found.";
        } catch (SQLException e) {
            return "Error depositing money: " + e.getMessage();
        }
    }

    @Override
    public String withdrawMoney(int accountNumber, double amount) throws RemoteException {
        String checkBalanceQuery = "SELECT balance FROM BankSystem.accounts WHERE account_number = ?";
        String updateBalanceQuery = "UPDATE BankSystem.accounts SET balance = balance - ? WHERE account_number = ?";
        try (Connection connection = DriverManager.getConnection(DB_URL + "/BankSystem", USER, PASSWORD);
             PreparedStatement checkStmt = connection.prepareStatement(checkBalanceQuery);
             PreparedStatement updateStmt = connection.prepareStatement(updateBalanceQuery)) {
            checkStmt.setInt(1, accountNumber);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                double currentBalance = rs.getDouble("balance");
                if (currentBalance >= amount) {
                    updateStmt.setDouble(1, amount);
                    updateStmt.setInt(2, accountNumber);
                    updateStmt.executeUpdate();
                    return "Successfully withdrew $" + amount;
                } else {
                    return "Insufficient funds.";
                }
            } else {
                return "Account not found.";
            }
        } catch (SQLException e) {
            return "Error withdrawing money: " + e.getMessage();
        }
    }

    @Override
    public String checkBalance(int accountNumber) throws RemoteException {
        String query = "SELECT balance FROM BankSystem.accounts WHERE account_number = ?";
        try (Connection connection = DriverManager.getConnection(DB_URL + "/BankSystem", USER, PASSWORD);
             PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, accountNumber);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                double balance = rs.getDouble("balance");
                return "The balance for account " + accountNumber + " is $" + balance;
            } else {
                return "Account not found.";
            }
        } catch (SQLException e) {
            return "Error checking balance: " + e.getMessage();
        }
    }
}
