package com.BankingApp.Entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transactions {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "transaction_id", unique = true, nullable = false, length = 50)
    private String transactionId;
    
    @Column(name = "type", nullable = false, length = 20)
    private String type; // DEPOSIT, WITHDRAWAL, TRANSFER
    
    @Column(name = "amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal amount;
    
    @Column(name = "description", length = 255)
    private String description;
    
    @Column(name = "transaction_date")
    private LocalDateTime transactionDate;
    
    @Column(name = "balance_after_transaction", precision = 15, scale = 2)
    private BigDecimal balanceAfterTransaction;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;
    
    @Column(name = "related_account_number", length = 20)
    private String relatedAccountNumber; // For transfer transactions
    
    // Constructors
    public Transactions() {
        this.transactionDate = LocalDateTime.now();
    }
    
    public Transactions(String transactionId, String type, BigDecimal amount, 
                      String description, Account account) {
        this();
        this.transactionId = transactionId;
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.account = account;
        this.balanceAfterTransaction = account.getBalance();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public LocalDateTime getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDateTime transactionDate) { this.transactionDate = transactionDate; }
    
    public BigDecimal getBalanceAfterTransaction() { return balanceAfterTransaction; }
    public void setBalanceAfterTransaction(BigDecimal balanceAfterTransaction) { 
        this.balanceAfterTransaction = balanceAfterTransaction; 
    }
    
    public Account getAccount() { return account; }
    public void setAccount(Account account) { this.account = account; }
    
    public String getRelatedAccountNumber() { return relatedAccountNumber; }
    public void setRelatedAccountNumber(String relatedAccountNumber) { 
        this.relatedAccountNumber = relatedAccountNumber; 
    }
    
    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", transactionId='" + transactionId + '\'' +
                ", type='" + type + '\'' +
                ", amount=" + amount +
                ", description='" + description + '\'' +
                ", transactionDate=" + transactionDate +
                ", balanceAfterTransaction=" + balanceAfterTransaction +
                '}';
    }
}