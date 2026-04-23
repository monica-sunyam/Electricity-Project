package com.tarifvergleich.electricity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tarifvergleich.electricity.model.CustomerServiceRequest;

@Repository
public interface CustomerServiceRequestRepository extends JpaRepository<CustomerServiceRequest, Integer> {

}
