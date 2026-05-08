package com.tarifvergleich.electricity.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tarifvergleich.electricity.model.CustomerOrder;

@Repository
public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Integer> {

	Optional<CustomerOrder> findByIdAndAdminAdminId(Integer id, Integer adminId);
}
