import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    private static final String BRANCH1_HOST = "localhost";
    private static final int BRANCH1_PORT = 12345;
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        boolean running = true;
        
        while (running) {
            System.out.println("\n--- Bank Operations Menu ---");
            System.out.println("1. Create Account");
            System.out.println("2. Deposit Money");
            System.out.println("3. Withdraw Money");
            System.out.println("4. Check Balance");
            System.out.println("5. Exit");
            System.out.print("Please choose an option (1-5): ");

            // Check if there is an integer input available
            if (!scanner.hasNextInt()) {
                System.out.println("No more input available. Exiting...");
                break;
            }

            int choice = scanner.nextInt();

            // After reading the integer, check if there's a newline to consume
            if (scanner.hasNextLine()) {
                scanner.nextLine();  // Consume newline
            }

            if (choice == 5) {
                System.out.println("Exiting...");
                running = false; // Exit the loop
                break;
            }

            String request = handleUserChoice(choice);
            if (request != null) {
                String response = sendRequestToBranch1(request);
                System.out.println("Server Response: " + response);
            }
        }

        // Close the scanner resource after the loop
        scanner.close();
        System.exit(0); // Exit the program gracefully
    }

    private static String handleUserChoice(int choice) {
        switch (choice) {
            case 1:
                System.out.print("Enter account holder's name: ");
                if (!scanner.hasNextLine()) {
                    System.out.println("No input available for account holder's name.");
                    return null;
                }
                String name = scanner.nextLine();

                System.out.print("Enter initial balance: ");
                if (!scanner.hasNextDouble()) {
                    System.out.println("Invalid input for initial balance.");
                    return null;
                }
                double initialBalance = scanner.nextDouble();

                // Consume the remaining newline
                if (scanner.hasNextLine()) {
                    scanner.nextLine();
                }

                return "CREATE_ACCOUNT " + name + " " + initialBalance;

            case 2:
                System.out.print("Enter account number: ");
                if (!scanner.hasNextInt()) {
                    System.out.println("Invalid input for account number.");
                    return null;
                }
                int depositAccountNumber = scanner.nextInt();

                System.out.print("Enter amount to deposit: ");
                if (!scanner.hasNextDouble()) {
                    System.out.println("Invalid input for deposit amount.");
                    return null;
                }
                double depositAmount = scanner.nextDouble();

                // Consume the remaining newline
                if (scanner.hasNextLine()) {
                    scanner.nextLine();
                }

                return "DEPOSIT " + depositAccountNumber + " " + depositAmount;

            case 3:
                System.out.print("Enter account number: ");
                if (!scanner.hasNextInt()) {
                    System.out.println("Invalid input for account number.");
                    return null;
                }
                int withdrawAccountNumber = scanner.nextInt();

                System.out.print("Enter amount to withdraw: ");
                if (!scanner.hasNextDouble()) {
                    System.out.println("Invalid input for withdrawal amount.");
                    return null;
                }
                double withdrawAmount = scanner.nextDouble();

                // Consume the remaining newline
                if (scanner.hasNextLine()) {
                    scanner.nextLine();
                }

                return "WITHDRAW " + withdrawAccountNumber + " " + withdrawAmount;

            case 4:
                System.out.print("Enter account number: ");
                if (!scanner.hasNextInt()) {
                    System.out.println("Invalid input for account number.");
                    return null;
                }
                int checkAccountNumber = scanner.nextInt();

                // Consume the remaining newline
                if (scanner.hasNextLine()) {
                    scanner.nextLine();
                }

                return "CHECK_BALANCE " + checkAccountNumber;

            default:
                System.out.println("Invalid choice. Please select a valid option.");
                return null;
        }
    }

    private static String sendRequestToBranch1(String request) {
        try (Socket socket = new Socket(BRANCH1_HOST, BRANCH1_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            System.out.println("Connecting to Branch 1...");
            out.println(request);
            System.out.println("Sent request to Branch 1: " + request);

            String response = in.readLine();
            System.out.println("Received response from Branch 1: " + response);
            return response;

        } catch (IOException e) {
            return "Error communicating with Branch 1: " + e.getMessage();
        }
    }
}
