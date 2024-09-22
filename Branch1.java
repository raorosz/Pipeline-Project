import java.io.*;
import java.net.*;
import java.rmi.Naming;
import java.sql.*;

public class Branch1 {
    private static final String RMI_SERVER = "//localhost/BankService";
    private static final String DB_URL = "jdbc:mysql://localhost:3306/banksystem";
    private static final String USER = "root";
    private static final String PASSWORD = "P@ssword";

    public static void main(String[] args) {
        try {
            // Load the MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
    
            try (ServerSocket serverSocket = new ServerSocket(12345)) {
                System.out.println("Branch 1 is running and waiting for client connections on port 12345...");
    
                System.out.println("Attempting to connect to BankService at " + RMI_SERVER);
                BankService centralServer = (BankService) Naming.lookup(RMI_SERVER);
                System.out.println("Successfully connected to BankService.");
    
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Accepted connection from client: " + clientSocket.getRemoteSocketAddress());
                    new Thread(new ClientHandler(clientSocket, centralServer)).start();
                }
            }
        } catch (Exception e) {
            System.err.println("Branch1 encountered an error:");
            e.printStackTrace();
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket clientSocket;
        private BankService centralServer;

        public ClientHandler(Socket clientSocket, BankService centralServer) {
            this.clientSocket = clientSocket;
            this.centralServer = centralServer;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                 Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {

                String request = in.readLine();
                System.out.println("Received request from client: " + request);

                String response = handleRequest(request, connection);
                System.out.println("Received response from Central Server: " + response);

                out.println(response);
                System.out.println("Sent response to client: " + response);

            } catch (IOException | SQLException e) {
                e.printStackTrace();
            }
        }

        private String handleRequest(String request, Connection connection) {
            try {
                String[] parts = request.split(" ");
                String transactionType = parts[0];
                String result = "";

                switch (transactionType) {
                    case "CREATE_ACCOUNT":
                        result = centralServer.createAccount(parts[1], Double.parseDouble(parts[2]));
                        logTransaction(connection, 0, 0, Double.parseDouble(parts[2]), "CREATE_ACCOUNT"); // Account creation
                        break;
                    case "DEPOSIT":
                        result = centralServer.depositMoney(Integer.parseInt(parts[1]), Double.parseDouble(parts[2]));
                        logTransaction(connection, Integer.parseInt(parts[1]), 0, Double.parseDouble(parts[2]), "DEPOSIT");
                        break;
                    case "WITHDRAW":
                        result = centralServer.withdrawMoney(Integer.parseInt(parts[1]), Double.parseDouble(parts[2]));
                        logTransaction(connection, Integer.parseInt(parts[1]), 0, Double.parseDouble(parts[2]), "WITHDRAW");
                        break;
                    case "CHECK_BALANCE":
                        result = centralServer.checkBalance(Integer.parseInt(parts[1]));
                        // No need to log balance checks as transactions
                        break;
                    default:
                        result = "Unknown command.";
                }

                return result;
            } catch (Exception e) {
                return "Error processing request: " + e.getMessage();
            }
        }

        private void logTransaction(Connection connection, int accountNumber, int transactionID, double amount, String transactionType) throws SQLException {
            String query = "INSERT INTO transactions (account_number, amount, type) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                pstmt.setInt(1, accountNumber+1);
                pstmt.setDouble(2, amount);
                pstmt.setString(3, transactionType);
                pstmt.executeUpdate();
            }
        }
    }
}
