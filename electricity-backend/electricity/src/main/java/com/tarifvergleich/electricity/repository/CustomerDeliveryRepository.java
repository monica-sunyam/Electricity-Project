package com.tarifvergleich.electricity.repository;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tarifvergleich.electricity.model.CustomerDelivery;

@Repository
public interface CustomerDeliveryRepository extends JpaRepository<CustomerDelivery, Integer> {
	
	Boolean existsByIdAndAdminAdminIdAndOrderPlaced(Integer id, Integer adminId, Boolean orderPlaced);

	Page<CustomerDelivery> findAllByAdminAdminId(Integer adminId, Pageable pageable);


	Optional<CustomerDelivery> findByIdAndAdminAdminId(Integer id, Integer adminId);

	Optional<CustomerDelivery> findByIdAndCustomerIdCustomerId(Integer id, Integer customerId);
	
	

	List<CustomerDelivery> findAllByAdminAdminIdOrderByOrderPlacedOnDesc(Integer adminId);
	List<CustomerDelivery> findAllByAdminAdminIdAndCustomerIdCustomerIdAndOrderPlacedOrderByOrderPlacedOnDesc(
			Integer adminId, Integer customerId, Boolean orderPlaced);

	List<CustomerDelivery> findAllByCustomerIdCustomerIdAndIsExpiredAndIsCancelledAndDeliveryTypeAndOrderPlacedAndAddressIdIn(
			Integer customerId, Boolean isExpired, Boolean isCancelled, String deliveryType, Boolean orderPlaced,
			List<Integer> addressIds);

	@Query("SELECT cd FROM CustomerDelivery cd " + "WHERE " + "cd.expiryOn IS NOT NULL AND "
			+ "cd.isExpired = :isExpired " + "AND (cd.expiryOn - :currentDate) <= :timeline "
			+ "AND cd.expiryOn >= :currentDate")
	List<CustomerDelivery> findRecentExpiryDelivery(@Param("isExpired") Boolean isExpired,
			@Param("currentDate") BigInteger currentDate, @Param("timeline") BigInteger timeline);
}
