package com.tarifvergleich.electricity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tarifvergleich.electricity.model.CustomerLoginHistory;

@Repository
public interface CustomerLoginHistoryRepository extends JpaRepository<CustomerLoginHistory, Integer> {

}
