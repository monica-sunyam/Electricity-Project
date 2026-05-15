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

	@Query("SELECT cd FROM CustomerDelivery cd " + "LEFT JOIN cd.customerId cust " + "LEFT JOIN cd.address addr "
			+ "LEFT JOIN cd.customerOrder co " + "LEFT JOIN cd.customerBookingDocument doc "
			+ "WHERE cd.admin.adminId = :adminId " + "AND ("
			+ "   LOWER(cd.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR "
			+ "   LOWER(cd.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR "
			+ "   LOWER(cust.email) LIKE LOWER(CONCAT('%', :search, '%')) OR "
			+ "   LOWER(cd.mobile) LIKE LOWER(CONCAT('%', :search, '%')) OR "
			+ "   LOWER(addr.zip) LIKE LOWER(CONCAT('%', :search, '%')) OR "
			+ "   LOWER(addr.city) LIKE LOWER(CONCAT('%', :search, '%')) OR "
			+ "   LOWER(addr.street) LIKE LOWER(CONCAT('%', :search, '%'))" + ") "
			+ "AND (:orderPlaced IS NULL OR cd.orderPlaced = :orderPlaced) "
			+ "AND (:pending IS NULL OR co IS NULL) "
			+ "AND (:openOrder IS NULL OR (co IS NOT NULL AND co.orderStatus = 0 AND co.orderId IS NULL))"
			+ "AND (:orderPlacedInEgon IS NULL OR (co IS NOT NULL AND co.adminPlacedOrder = :orderPlacedInEgon)) "
			+ "AND (:docUploaded IS NULL OR (doc IS NOT NULL AND doc.signedFileUrl IS NOT NULL)) "
			+ "AND (:isExpired IS NULL OR (co IS NOT NULL AND co.isExpired = :isExpired))")
	Page<CustomerDelivery> findByConditions(@Param("adminId") Integer adminId, @Param("search") String search,
			@Param("orderPlaced") Boolean orderPlaced, @Param("pending") Boolean pending,
			@Param("openOrder") Boolean openOrder, @Param("orderPlacedInEgon") Boolean orderPlacedInEgon,
			@Param("docUploaded") Boolean docUploaded, @Param("isExpired") Boolean isExpired, Pageable pageable);
}
