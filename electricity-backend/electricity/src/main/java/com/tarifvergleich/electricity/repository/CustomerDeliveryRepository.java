package com.tarifvergleich.electricity.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tarifvergleich.electricity.model.CustomerDelivery;

@Repository
public interface CustomerDeliveryRepository extends JpaRepository<CustomerDelivery, Integer> {

	Page<CustomerDelivery> findAllByAdminAdminId(Integer adminId, Pageable pageable);

	List<CustomerDelivery> findAllByAdminAdminIdOrderByOrderPlacedOnDesc(Integer adminId);

	Optional<CustomerDelivery> findByIdAndAdminAdminId(Integer id, Integer adminId);

	List<CustomerDelivery> findAllByAdminAdminIdAndCustomerIdCustomerIdAndOrderPlacedOrderByOrderPlacedOnDesc(
			Integer adminId, Integer customerId, Boolean orderPlaced);

	List<CustomerDelivery> findAllByCustomerIdCustomerIdAndIsExpiredAndIsCancelledAndDeliveryTypeAndOrderPlacedAndAddressIdIn(
			Integer customerId, Boolean isExpired, Boolean isCancelled, String deliveryType, Boolean orderPlaced,
			List<Integer> addressIds);
}
