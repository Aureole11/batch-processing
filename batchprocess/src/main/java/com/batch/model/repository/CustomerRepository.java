package com.batch.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.batch.model.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {
	
}
