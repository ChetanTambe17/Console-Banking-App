package com.BankingApp.Service;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import com.BankingApp.Entities.Customer;
import com.BankingApp.Util.HibernateUtil;

import java.util.List;
import java.util.regex.Pattern;

public class CustomerService {

	private static final Pattern PAN_PATTERN = Pattern.compile("[A-Z]{5}[0-9]{4}[A-Z]{1}");
	private static final Pattern AADHAR_PATTERN = Pattern.compile("^[2-9]{1}[0-9]{11}$");

	public Customer createCustomer(String firstName, String lastName, String email, String panNumber,
			String aadharNumber, String phone, String address) {

		if (!isValidPan(panNumber)) {
			throw new IllegalArgumentException("Invalid PAN number format");
		}

		// Validate Aadhar format
		if (!isValidAadhar(aadharNumber)) {
			throw new IllegalArgumentException("Invalid Aadhar number format");
		}

		// Check if PAN or Aadhar already exists
		if (isPanExists(panNumber)) {
			throw new IllegalArgumentException("PAN number already exists");
		}

		if (isAadharExists(aadharNumber)) {
			throw new IllegalArgumentException("Aadhar number already exists");
		}

		Customer customer = new Customer(firstName, lastName, email, panNumber, aadharNumber);
		customer.setPhone(phone);
		customer.setAddress(address);

		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction transaction = null;

		try {
			transaction = session.beginTransaction();
			session.persist(customer);
			transaction.commit();
			return customer;
		} catch (Exception e) {
			if (transaction != null)
				transaction.rollback();
			throw new RuntimeException("Error creating customer: " + e.getMessage(), e);
		} finally {
			session.close();

		}
	}

	public Customer getCustomerById(Long id) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		try {
			return session.get(Customer.class, id);
		} finally {
			session.close();
		}
	}

	public Customer getCustomerByPan(String panNumber) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		try {
			Query<Customer> query = session.createQuery("FROM Customer WHERE panNumber = :panNumber", Customer.class);
			query.setParameter("panNumber", panNumber.toUpperCase());
			return query.uniqueResult();
		} finally {
			session.close();
		}
	}

	public List<Customer> getAllCustomers() {
		Session session = HibernateUtil.getSessionFactory().openSession();
		try {
			// Use JOIN FETCH to load accounts in the same query
			return session.createQuery("SELECT DISTINCT c FROM Customer c LEFT JOIN FETCH c.accounts", Customer.class)
					.list();
		} finally {
			session.close();
		}
	}

	private boolean isValidPan(String panNumber) {
		return PAN_PATTERN.matcher(panNumber.toUpperCase()).matches();
	}

	private boolean isValidAadhar(String aadharNumber) {
		return AADHAR_PATTERN.matcher(aadharNumber).matches();
	}

	private boolean isPanExists(String panNumber) {
		// 1. Open Hibernate Session
		Session session = HibernateUtil.getSessionFactory().openSession();

		try {
			// 2. Create HQL Query to count customers with this PAN
			Query<Long> query = session.createQuery("SELECT COUNT(c) FROM Customer c WHERE panNumber = :panNumber",
					Long.class);
			// 3. Set parameter (auto-convert to uppercase)
			query.setParameter("panNumber", panNumber.toUpperCase());

			// 4. Execute query and check if count > 0
			return query.uniqueResult() > 0;
		} finally {
			session.close();
		}
	}

	private boolean isAadharExists(String aadharNumber) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		try {
			Query<Long> query = session
					.createQuery("SELECT COUNT(c) FROM Customer c WHERE aadharNumber = :aadharNumber", Long.class);
			query.setParameter("aadharNumber", aadharNumber);
			return query.uniqueResult() > 0;
		} finally {
			session.close();
		}
	}
}
