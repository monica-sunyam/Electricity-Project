package com.tarifvergleich.electricity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tarifvergleich.electricity.model.CustomerRequestCounselling;

@Repository
public interface CustomerRequestCounsellingRepository extends JpaRepository<CustomerRequestCounselling, Integer> {

}
