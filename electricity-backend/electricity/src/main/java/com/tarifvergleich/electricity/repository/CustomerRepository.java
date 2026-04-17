package com.tarifvergleich.electricity.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tarifvergleich.electricity.model.Customer;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {

	boolean existsByEmail(String email);
    Optional<Customer> findByEmail(String email);
    
    Page<Customer> findAll(Pageable  pageable);
    
    Page<Customer> findAllByAdminAdminId(Integer adminId, Pageable pageable);
    List<Customer> findAllByAdminAdminIdOrderByJoinedOnDesc(Integer adminId);
}
