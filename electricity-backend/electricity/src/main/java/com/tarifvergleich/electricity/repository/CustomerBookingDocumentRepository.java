package com.tarifvergleich.electricity.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tarifvergleich.electricity.model.CustomerBookingDocument;

@Repository
public interface CustomerBookingDocumentRepository extends JpaRepository<CustomerBookingDocument, Integer> {

	Optional<CustomerBookingDocument> findByCustomerDeliveryIdAndAdminAdminId(Integer customerDeliveryId,
			Integer adminId);
}
