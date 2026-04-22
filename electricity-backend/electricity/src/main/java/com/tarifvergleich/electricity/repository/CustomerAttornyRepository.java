package com.tarifvergleich.electricity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tarifvergleich.electricity.model.CustomerAttorny;

@Repository
public interface CustomerAttornyRepository extends JpaRepository<CustomerAttorny, Integer> {

}
