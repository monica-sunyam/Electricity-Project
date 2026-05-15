package com.tarifvergleich.electricity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tarifvergleich.electricity.model.CustomerContractSignature;

@Repository
public interface CustomerContractSignatureRepository extends JpaRepository<CustomerContractSignature, Integer> {

}
