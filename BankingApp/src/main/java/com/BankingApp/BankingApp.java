package com.BankingApp;

import com.BankingApp.Entities.*;
import com.BankingApp.Service.AccountService;
import com.BankingApp.Service.CustomerService;
import com.BankingApp.Util.HibernateUtil;
import com.BankingApp.Util.DatabaseInitializer;

import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

public class BankingApp {
    private static final Scanner scanner = new Scanner(System.in);
    private static final CustomerService customerService = new CustomerService();
    private static final AccountService accountService = new AccountService();

    public static void main(String[] args) {
        System.out.println("=== Banking Transaction Management System ===");
        System.out.println("Initializing database connection...");
        
        initializeSystem();

        while (true) {
            showMainMenu();
            int choice = getIntInput("Enter your choice: ");

            switch (choice) {
                case 1:
                    createCustomer();
                    break;
                case 2:
                    createAccount();
                    break;
                case 3:
                    depositMoney();
                    break;
                case 4:
                    withdrawMoney();
                    break;
                case 5:
                    transferMoney();
                    break;
                case 6:
                    viewTransactionHistory();
                    break;
                case 7:
                    viewAccountBalance();
                    break;
                case 8:
                    listAllCustomers();
                    break;
                case 0:
                    System.out.println("Thank you for using Banking System. Goodbye!");
                    HibernateUtil.shutdown();
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void initializeSystem() {
        try {
            // Initialize database automatically
            DatabaseInitializer.initializeDatabase();
            
            // Create sample data
            DatabaseInitializer.createSampleData();
            
            System.out.println("✅ System initialization completed successfully!");
            
        } catch (Exception e) {
            System.out.println("❌ System initialization failed: " + e.getMessage());
            System.out.println("Please check:");
            System.out.println("1. MySQL server is running");
            System.out.println("2. Database credentials in hibernate.cfg.xml are correct");
            System.out.println("3. You have privileges to create databases");
            System.exit(1);
        }
    }

    private static void showMainMenu() {
        System.out.println("\n===== MAIN MENU =====");
        System.out.println("1. Create Customer");
        System.out.println("2. Create Account");
        System.out.println("3. Deposit Money");
        System.out.println("4. Withdraw Money");
        System.out.println("5. Transfer Money");
        System.out.println("6. View Transaction History");
        System.out.println("7. View Account Balance");
        System.out.println("8. List All Customers");
        System.out.println("0. Exit");
        System.out.println("=====================");
    }

    private static void createCustomer() {
        System.out.println("\n----- Create New Customer -----");

        System.out.print("First Name: ");
        String firstName = scanner.nextLine();

        System.out.print("Last Name: ");
        String lastName = scanner.nextLine();

        System.out.print("Email: ");
        String email = scanner.nextLine();

        System.out.print("PAN Number: ");
        String panNumber = scanner.nextLine();

        System.out.print("Aadhar Number: ");
        String aadharNumber = scanner.nextLine();

        System.out.print("Phone: ");
        String phone = scanner.nextLine();

        System.out.print("Address: ");
        String address = scanner.nextLine();

        try {
            Customer customer = customerService.createCustomer(firstName, lastName, email, panNumber, aadharNumber,
                    phone, address);
            System.out.println("✅ Customer created successfully!");
            System.out.println("   Customer ID: " + customer.getId());
            System.out.println("   Name: " + customer.getFirstName() + " " + customer.getLastName());
            System.out.println("   PAN: " + customer.getPanNumber());
        } catch (Exception e) {
            System.out.println("❌ Error creating customer: " + e.getMessage());
        }
    }

    private static void createAccount() {
        System.out.println("\n----- Create New Account -----");

        System.out.print("Customer PAN Number: ");
        String panNumber = scanner.nextLine();

        Customer customer = customerService.getCustomerByPan(panNumber);
        if (customer == null) {
            System.out.println("❌ Customer not found with PAN: " + panNumber);
            return;
        }

        System.out.println("✅ Customer found: " + customer.getFirstName() + " " + customer.getLastName());

        System.out.print("Account Type (SAVINGS/CURRENT/SALARY): ");
        String accountType = scanner.nextLine();

        try {
            Account account = accountService.createAccount(customer, accountType);
            System.out.println("✅ Account created successfully!");
            System.out.println("   Account Number: " + account.getAccountNumber());
            System.out.println("   Account Type: " + account.getAccountType());
            System.out.println("   Customer: " + customer.getFirstName() + " " + customer.getLastName());
        } catch (Exception e) {
            System.out.println("❌ Error creating account: " + e.getMessage());
        }
    }

    private static void depositMoney() {
        System.out.println("\n----- Deposit Money -----");

        System.out.print("Account Number: ");
        String accountNumber = scanner.nextLine();

        BigDecimal amount = getBigDecimalInput("Amount to deposit: ");
        System.out.print("Description: ");
        String description = scanner.nextLine();

        try {
            Transactions transaction = accountService.deposit(accountNumber, amount, description);
            System.out.println("✅ Deposit successful!");
            System.out.println("   Transaction ID: " + transaction.getTransactionId());
            System.out.println("   Amount: " + transaction.getAmount());
            System.out.println("   New Balance: " + transaction.getBalanceAfterTransaction());
        } catch (Exception e) {
            System.out.println("❌ Error processing deposit: " + e.getMessage());
        }
    }

    private static void withdrawMoney() {
        System.out.println("\n----- Withdraw Money -----");

        System.out.print("Account Number: ");
        String accountNumber = scanner.nextLine();

        BigDecimal amount = getBigDecimalInput("Amount to withdraw: ");
        System.out.print("Description: ");
        String description = scanner.nextLine();

        try {
            Transactions transaction = accountService.withdraw(accountNumber, amount, description);
            System.out.println("✅ Withdrawal successful!");
            System.out.println("   Transaction ID: " + transaction.getTransactionId());
            System.out.println("   Amount: " + transaction.getAmount());
            System.out.println("   New Balance: " + transaction.getBalanceAfterTransaction());
        } catch (Exception e) {
            System.out.println("❌ Error processing withdrawal: " + e.getMessage());
        }
    }

    private static void transferMoney() {
        System.out.println("\n----- Transfer Money -----");

        System.out.print("From Account Number: ");
        String fromAccount = scanner.nextLine();

        System.out.print("To Account Number: ");
        String toAccount = scanner.nextLine();

        BigDecimal amount = getBigDecimalInput("Amount to transfer: ");
        System.out.print("Description: ");
        String description = scanner.nextLine();

        try {
            Transactions transaction = accountService.transfer(fromAccount, toAccount, amount, description);
            System.out.println("✅ Transfer successful!");
            System.out.println("   Transaction ID: " + transaction.getTransactionId());
            System.out.println("   From Account: " + fromAccount);
            System.out.println("   To Account: " + toAccount);
            System.out.println("   Amount: " + transaction.getAmount());
            System.out.println("   New Balance: " + transaction.getBalanceAfterTransaction());
        } catch (Exception e) {
            System.out.println("❌ Error processing transfer: " + e.getMessage());
        }
    }

    private static void viewTransactionHistory() {
        System.out.println("\n----- Transaction History -----");

        System.out.print("Account Number: ");
        String accountNumber = scanner.nextLine();

        try {
            List<Transactions> transactions = accountService.getTransactionHistory(accountNumber);

            if (transactions.isEmpty()) {
                System.out.println("No transactions found for account: " + accountNumber);
            } else {
                System.out.println("\nTransaction History for Account: " + accountNumber);
                System.out.println("==================================================================================");
                System.out.printf("%-20s %-12s %-12s %-25s %-15s\n", "Date", "Type", "Amount", "Description", "Balance");
                System.out.println("==================================================================================");

                for (Transactions t : transactions) {
                    System.out.printf("%-20s %-12s %-12s %-25s %-15s\n", 
                        t.getTransactionDate().toLocalDate(),
                        t.getType(), 
                        t.getAmount(),
                        t.getDescription().length() > 25 ? 
                            t.getDescription().substring(0, 22) + "..." : t.getDescription(),
                        t.getBalanceAfterTransaction());
                }
            }
        } catch (Exception e) {
            System.out.println("❌ Error retrieving transaction history: " + e.getMessage());
        }
    }

    private static void viewAccountBalance() {
        System.out.println("\n----- Account Balance -----");

        System.out.print("Account Number: ");
        String accountNumber = scanner.nextLine();

        try {
            BigDecimal balance = accountService.getAccountBalance(accountNumber);
            System.out.println("💰 Current Balance: " + balance);
        } catch (Exception e) {
            System.out.println("❌ Error retrieving balance: " + e.getMessage());
        }
    }

    private static void listAllCustomers() {
        System.out.println("\n----- All Customers -----");

        try {
            List<Customer> customers = customerService.getAllCustomers();

            if (customers.isEmpty()) {
                System.out.println("No customers found.");
            } else {
                for (Customer customer : customers) {
                    System.out.println("\n📋 Customer: " + customer.getFirstName() + " " + customer.getLastName());
                    System.out.println("   ID: " + customer.getId());
                    System.out.println("   PAN: " + customer.getPanNumber());
                    System.out.println("   Email: " + customer.getEmail());
                    System.out.println("   Accounts:");

                    if (customer.getAccounts().isEmpty()) {
                        System.out.println("     No accounts");
                    } else {
                        for (Account account : customer.getAccounts()) {
                            System.out.println("     💳 " + account.getAccountNumber() + 
                                             " (" + account.getAccountType() + ") - Balance: " + 
                                             account.getBalance());
                        }
                    }
                    System.out.println("---");
                }
            }
        } catch (Exception e) {
            System.out.println("❌ Error retrieving customers: " + e.getMessage());
        }
    }

    private static int getIntInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("❌ Please enter a valid number.");
            }
        }
    }

    private static BigDecimal getBigDecimalInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return new BigDecimal(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("❌ Please enter a valid amount.");
            }
        }
    }
}