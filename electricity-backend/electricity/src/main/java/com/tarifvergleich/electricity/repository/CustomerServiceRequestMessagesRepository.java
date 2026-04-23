package com.tarifvergleich.electricity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tarifvergleich.electricity.model.CustomerServiceRequestMessages;

@Repository
public interface CustomerServiceRequestMessagesRepository
		extends JpaRepository<CustomerServiceRequestMessages, Integer> {

}
