package com.tarifvergleich.electricity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tarifvergleich.electricity.model.CustomerSelectedProvider;

@Repository
public interface CustomerSelectedProviderRepository extends JpaRepository<CustomerSelectedProvider, Integer> {

}
