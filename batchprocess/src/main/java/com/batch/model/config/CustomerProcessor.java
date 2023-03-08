package com.batch.model.config;

import org.springframework.batch.item.ItemProcessor;
import com.batch.model.Customer;

public class CustomerProcessor implements ItemProcessor<Customer, Customer> {

	public Customer process(Customer customer) throws Exception {
//		if(customer.getAge().equals("30")) {
		return customer;
//	}
//		else {
//			return null;
//		}
	}

}
