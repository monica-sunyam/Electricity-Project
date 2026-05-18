package com.tarifvergleich.electricity.repository;

import com.tarifvergleich.electricity.model.CustomerQueryContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerQueryContactRepository extends JpaRepository<CustomerQueryContact, Integer> {
}
