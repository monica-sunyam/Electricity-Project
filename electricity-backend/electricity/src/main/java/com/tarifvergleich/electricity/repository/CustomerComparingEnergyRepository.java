package com.tarifvergleich.electricity.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tarifvergleich.electricity.model.CustomerComparingEnergy;

@Repository
public interface CustomerComparingEnergyRepository extends JpaRepository<CustomerComparingEnergy, Integer> {

	Page<CustomerComparingEnergy> findAllByAdminAdminId(Integer adminId, Pageable pageable);

	List<CustomerComparingEnergy> findAllByAdminAdminIdOrderByIdDesc(Integer adminId);

	@Query(value = """
			SELECT ce.*
			FROM customers_comparing_electricity ce
			LEFT JOIN customer c ON c.customer_id = ce.customer_id
			WHERE
			    (ce.zip LIKE CONCAT('%', :search, '%')
			    OR LOWER(ce.city) LIKE CONCAT('%', :search, '%')
			    OR LOWER(ce.street) LIKE CONCAT('%', :search, '%')
			    OR LOWER(ce.house_number) LIKE CONCAT('%', :search, '%')
			    OR LOWER(ce.energy_branch) LIKE LOWER(CONCAT('%', :search, '%'))
			    OR LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%'))
			    OR LOWER(c.mobile_number) LIKE LOWER(CONCAT('%', :search, '%'))
			    OR LOWER(c.first_name) LIKE LOWER(CONCAT('%', :search, '%'))
			    OR LOWER(c.last_name) LIKE LOWER(CONCAT('%', :search, '%'))
			    OR EXISTS (
			        SELECT 1
			        FROM jsonb_array_elements(
			            ce.rate_response -> 'result'
			        ) elem
			        WHERE LOWER(elem ->> 'providerName')
			              LIKE LOWER(CONCAT('%', :search, '%'))
			    )) AND ce.admin_id = :adminId
			""", countQuery = """
			SELECT COUNT(ce.*)
			FROM customers_comparing_electricity ce
			LEFT JOIN customer c ON c.customer_id = ce.customer_id
			WHERE
			    (ce.zip LIKE CONCAT('%', :search, '%')
			    OR LOWER(ce.city) LIKE CONCAT('%', :search, '%')
			    OR LOWER(ce.street) LIKE CONCAT('%', :search, '%')
			    OR LOWER(ce.house_number) LIKE CONCAT('%', :search, '%')
			    OR LOWER(ce.energy_branch) LIKE LOWER(CONCAT('%', :search, '%'))
			    OR LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%'))
			    OR LOWER(c.mobile_number) LIKE LOWER(CONCAT('%', :search, '%'))
			    OR LOWER(c.first_name) LIKE LOWER(CONCAT('%', :search, '%'))
			    OR LOWER(c.last_name) LIKE LOWER(CONCAT('%', :search, '%'))
			    OR EXISTS (
			        SELECT 1
			        FROM jsonb_array_elements(
			            ce.rate_response -> 'result'
			        ) elem
			        WHERE LOWER(elem ->> 'providerName')
			              LIKE LOWER(CONCAT('%', :search, '%'))
			    )) AND ce.admin_id = :adminId
			""", nativeQuery = true)
	Page<CustomerComparingEnergy> findAllByDifferentFilter(@Param("search") String search, @Param("adminId") Integer adminId, Pageable pageable);
}
