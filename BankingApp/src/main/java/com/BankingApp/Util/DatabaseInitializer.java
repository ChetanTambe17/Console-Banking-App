package com.BankingApp.Util;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.BankingApp.Entities.Account;
import com.BankingApp.Entities.Transactions;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DatabaseInitializer {
	private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);

	public static void initializeDatabase() {
		try {
			// Step 0: Create database if it doesn't exist
			createDatabaseIfNotExists();

			// Step 1: Check database connection
			checkDatabaseStatus();

			// Step 2: Let Hibernate create schema
			createSchema();

			// Step 3: Verify tables were created using Native SQL
			verifySchema();

			logger.info("✅ Database initialization completed successfully!");

		} catch (Exception e) {
			logger.error("❌ Database initialization failed: " + e.getMessage());
			throw new RuntimeException("Database initialization failed", e);
		}
	}

	// NEW METHOD: Create database if it doesn't exist
	private static void createDatabaseIfNotExists() {
		System.out.println("🗄️  Checking database existence...");

		// First, try to connect without specifying the database
		String urlWithoutDb = "jdbc:mysql://127.0.0.1:3307/";
		String username = "root";
		String password = ""; // Replace with your actual password
		String dbName = "bank_db";

		try (Connection connection = DriverManager.getConnection(urlWithoutDb, username, password);
				Statement statement = connection.createStatement()) {

			// Create database if it doesn't exist
			String createDbSQL = "CREATE DATABASE IF NOT EXISTS " + dbName;
			statement.executeUpdate(createDbSQL);
			System.out.println("✅ Database created/verified: " + dbName);

		} catch (Exception e) {
			System.out.println("❌ Failed to create database: " + e.getMessage());
			throw new RuntimeException("Database creation failed: " + e.getMessage(), e);
		}
	}

	public static void checkDatabaseStatus() {
		System.out.println("🔍 Checking database connection...");

		Session session = null;
		try {
			session = HibernateUtil.getSessionFactory().openSession();

			// Simple test query
			String result = session.createNativeQuery("SELECT 'CONNECTED' as status", String.class).getSingleResult();

			if ("CONNECTED".equals(result)) {
				System.out.println("✅ Database connection successful");
				logger.info("✅ Database is ready and accessible");
			}

		} catch (Exception e) {
			System.out.println("❌ Database connection failed: " + e.getMessage());
			throw new RuntimeException("Cannot connect to database", e);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
	}

	private static void createSchema() {
		System.out.println("🏗️  Creating database schema...");
		// Hibernate will automatically create tables based on hibernate.hbm2ddl.auto
		// setting
		Session session = HibernateUtil.getSessionFactory().openSession();
		session.close();
		System.out.println("✅ Database schema creation initiated");
	}

	private static void verifySchema() {
		System.out.println("🔍 Verifying database tables...");

		Session session = null;
		try {
			session = HibernateUtil.getSessionFactory().openSession();

			// Check table existence using native SQL
			String[] tables = { "customers", "accounts", "transactions" };

			for (String table : tables) {
				boolean tableExists = checkTableExists(session, table);
				if (tableExists) {
					System.out.println("✅ " + table + " table: EXISTS");
				} else {
					System.out.println("❌ " + table + " table: MISSING");
					throw new RuntimeException("Table " + table + " does not exist");
				}
			}

			System.out.println("✅ All database tables verified successfully!");

		} catch (Exception e) {
			System.out.println("❌ Schema verification failed: " + e.getMessage());
			throw new RuntimeException("Schema verification failed", e);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
	}

	private static boolean checkTableExists(Session session, String tableName) {
		try {
			// MySQL specific query to check table existence
			String sql = "SELECT COUNT(*) FROM information_schema.tables "
					+ "WHERE table_schema = DATABASE() AND table_name = :tableName";

			Long count = session.createNativeQuery(sql, Long.class).setParameter("tableName", tableName).uniqueResult();

			return count != null && count > 0;
		} catch (Exception e) {
			System.out.println("⚠️  Error checking table " + tableName + ": " + e.getMessage());
			return false;
		}
	}

	public static void createSampleData() {
		System.out.println("📊 Creating sample data...");

		Session session = null;
		Transaction transaction = null;
		try {
			session = HibernateUtil.getSessionFactory().openSession();
			transaction = session.beginTransaction();

			// Check if sample data already exists
			Long customerCount = session.createQuery("SELECT COUNT(c) FROM Customer c", Long.class).uniqueResult();

			if (customerCount == 0) {
				// Create sample customer
				com.BankingApp.Entities.Customer sampleCustomer = new com.BankingApp.Entities.Customer("John", "Doe",
						"john.doe@email.com", "ABCDE1234F", "234567890123");
				sampleCustomer.setPhone("9876543210");
				sampleCustomer.setAddress("123 Main Street, Mumbai");
				session.persist(sampleCustomer);

				// Create sample account
				Account sampleAccount = new Account("ACC1000001", "SAVINGS", sampleCustomer);
				sampleAccount.deposit(new BigDecimal("10000.00"));
				session.persist(sampleAccount);

				// Create sample transaction
				Transactions sampleTransaction = new Transactions("TXN1000001", "DEPOSIT", new BigDecimal("10000.00"),
						"Initial deposit", sampleAccount);
				sampleTransaction.setBalanceAfterTransaction(sampleAccount.getBalance());
				session.persist(sampleTransaction);

				transaction.commit();
				System.out.println("✅ Sample data created successfully!");
			} else {
				System.out.println("ℹ️  Database already contains data. Skipping sample data creation.");
			}

		} catch (Exception e) {
			if (transaction != null)
				transaction.rollback();
			System.out.println("⚠️  Sample data creation skipped: " + e.getMessage());
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
	}
}