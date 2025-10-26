package com.BankingApp.Service;

import org.hibernate.Session;
import org.hibernate.Transaction; // Import Hibernate Transaction
import org.hibernate.query.Query;
import com.BankingApp.Entities.Account;
import com.BankingApp.Entities.Customer;
import com.BankingApp.Entities.Transactions;
import com.BankingApp.Util.HibernateUtil;

import java.math.BigDecimal; 
import java.util.List;
import java.util.Random;

public class AccountService {

    public Account createAccount(Customer customer, String accountType) {
        String accountNumber = generateAccountNumber();

        Account account = new Account(accountNumber, accountType, customer);

        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction dbTransaction = null; // Now using Hibernate Transaction

        try {
            dbTransaction = session.beginTransaction();
            session.persist(account);
            dbTransaction.commit();
            return account;
        } catch (Exception e) {
            if (dbTransaction != null)
                dbTransaction.rollback();
            throw new RuntimeException("Error creating account: " + e.getMessage(), e);
        } finally {
            session.close();
        }
    }

    public Account getAccountByNumber(String accountNumber) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            Query<Account> query = session.createQuery("FROM Account WHERE accountNumber = :accountNumber",
                    Account.class);
            query.setParameter("accountNumber", accountNumber);
            return query.uniqueResult();
        } finally {
            session.close();
        }
    }

    public Transactions deposit(String accountNumber, BigDecimal amount, String description) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction dbTransaction = null; // Using Hibernate Transaction

        try {
            dbTransaction = session.beginTransaction();

            // Get account
            Account account = session.createQuery("FROM Account WHERE accountNumber = :accountNumber", Account.class)
                    .setParameter("accountNumber", accountNumber)
                    .uniqueResult();

            // Validations
            if (account == null) {
                throw new IllegalArgumentException("Account not found: " + accountNumber);
            }
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Deposit amount must be positive");
            }

            // Update balance
            account.deposit(amount);

            // Create transaction record
            Transactions transaction = new Transactions(
                generateTransactionId(), 
                "DEPOSIT", 
                amount, 
                description, 
                account
            );
            transaction.setBalanceAfterTransaction(account.getBalance());
            session.persist(transaction);

            dbTransaction.commit();
            return transaction;

        } catch (Exception e) {
            if (dbTransaction != null) {
                dbTransaction.rollback();
            }
            throw new RuntimeException("Deposit failed for account " + accountNumber + ": " + e.getMessage(), e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    public Transactions withdraw(String accountNumber, BigDecimal amount, String description) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction dbTransaction = null; // Using Hibernate Transaction

        try {
            dbTransaction = session.beginTransaction();

            // Get account within the same session
            Account account = session.createQuery("FROM Account WHERE accountNumber = :accountNumber", Account.class)
                    .setParameter("accountNumber", accountNumber)
                    .uniqueResult();

            if (account == null) {
                throw new IllegalArgumentException("Account not found: " + accountNumber);
            }

            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Withdrawal amount must be positive");
            }

            // Check sufficient balance
            if (account.getBalance().compareTo(amount) < 0) {
                throw new IllegalArgumentException("Insufficient balance. Available: " + account.getBalance());
            }

            // Update account balance
            boolean success = account.withdraw(amount);
            if (!success) {
                throw new IllegalArgumentException("Withdrawal failed");
            }

            // Create transaction record
            Transactions transaction = new Transactions(
                generateTransactionId(), 
                "WITHDRAWAL", 
                amount, 
                description,
                account
            );
            transaction.setBalanceAfterTransaction(account.getBalance());
            session.persist(transaction);

            dbTransaction.commit();
            return transaction;

        } catch (Exception e) {
            if (dbTransaction != null)
                dbTransaction.rollback();
            throw new RuntimeException("Error processing withdrawal: " + e.getMessage(), e);
        } finally {
            session.close();
        }
    }

    public Transactions transfer(String fromAccountNumber, String toAccountNumber, BigDecimal amount,
            String description) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction dbTransaction = null; // Using Hibernate Transaction

        try {
            dbTransaction = session.beginTransaction();

            // Get both accounts within the same session
            Account fromAccount = session
                    .createQuery("FROM Account WHERE accountNumber = :accountNumber", Account.class)
                    .setParameter("accountNumber", fromAccountNumber)
                    .uniqueResult();

            Account toAccount = session.createQuery("FROM Account WHERE accountNumber = :accountNumber", Account.class)
                    .setParameter("accountNumber", toAccountNumber)
                    .uniqueResult();

            if (fromAccount == null || toAccount == null) {
                throw new IllegalArgumentException("One or both accounts not found");
            }

            if (fromAccountNumber.equals(toAccountNumber)) {
                throw new IllegalArgumentException("Cannot transfer to the same account");
            }

            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Transfer amount must be positive");
            }

            // Check sufficient balance in source account
            if (fromAccount.getBalance().compareTo(amount) < 0) {
                throw new IllegalArgumentException(
                        "Insufficient balance for transfer. Available: " + fromAccount.getBalance());
            }

            // Perform transfer
            fromAccount.withdraw(amount);
            toAccount.deposit(amount);

            // Create transaction for source account
            Transactions debitTransaction = new Transactions(
                generateTransactionId(), 
                "TRANSFER", 
                amount,
                description + " (To: " + toAccountNumber + ")", 
                fromAccount
            );
            debitTransaction.setBalanceAfterTransaction(fromAccount.getBalance());
            debitTransaction.setRelatedAccountNumber(toAccountNumber);
            session.persist(debitTransaction);

            // Create transaction for destination account
            Transactions creditTransaction = new Transactions(
                generateTransactionId(), 
                "TRANSFER", 
                amount,
                description + " (From: " + fromAccountNumber + ")", 
                toAccount
            );
            creditTransaction.setBalanceAfterTransaction(toAccount.getBalance());
            creditTransaction.setRelatedAccountNumber(fromAccountNumber);
            session.persist(creditTransaction);

            dbTransaction.commit();
            return debitTransaction;

        } catch (Exception e) {
            if (dbTransaction != null)
                dbTransaction.rollback();
            throw new RuntimeException("Error processing transfer: " + e.getMessage(), e);
        } finally {
            session.close();
        }
    }

    public List<Transactions> getTransactionHistory(String accountNumber) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            Query<Transactions> query = session
                    .createQuery("FROM Transactions t WHERE t.account.accountNumber = :accountNumber "
                            + "ORDER BY t.transactionDate DESC", Transactions.class);
            query.setParameter("accountNumber", accountNumber);
            return query.list();
        } finally {
            session.close();
        }
    }

    public BigDecimal getAccountBalance(String accountNumber) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            Query<BigDecimal> query = session.createQuery(
                    "SELECT a.balance FROM Account a WHERE a.accountNumber = :accountNumber", BigDecimal.class);
            query.setParameter("accountNumber", accountNumber);
            return query.uniqueResult();
        } finally {
            session.close();
        }
    }

    private String generateAccountNumber() {
        Random random = new Random();
        return "ACC" + System.currentTimeMillis() + random.nextInt(1000);
    }

    private String generateTransactionId() {
        return "TXN" + System.currentTimeMillis();
    }
}