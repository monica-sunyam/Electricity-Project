package com.tarifvergleich.electricity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tarifvergleich.electricity.model.CustomerServices;

@Repository
public interface CustomerServicesRepository extends JpaRepository<CustomerServices, Integer> {

	List<CustomerServices> findAllByAdminAdminIdAndStatus(Integer adminId, Boolean status);
}
