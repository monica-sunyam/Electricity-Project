package com.tarifvergleich.electricity.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tarifvergleich.electricity.model.CustomerServices;

@Repository
public interface CustomerServicesRepository extends JpaRepository<CustomerServices, Integer> {
	
	List<CustomerServices> findAllByAdminAdminIdAndStatus(Integer adminId, Boolean status);

	List<CustomerServices> findAllByAdminAdminIdOrderByAddedOnDesc(Integer adminId);
	
	Page<CustomerServices> findAllByAdminAdminId(Integer adminId, Pageable pageable);
}
