package com.tarifvergleich.electricity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tarifvergleich.electricity.model.CustomerAttorny;

@Repository
public interface CustomerAttornyRepository extends JpaRepository<CustomerAttorny, Integer> {

	List<CustomerAttorny> findAllByCustomerCustomerIdAndIsRevokedOrderBySubmittedOnDesc(Integer customerId,
			Boolean isRevoked);

}
