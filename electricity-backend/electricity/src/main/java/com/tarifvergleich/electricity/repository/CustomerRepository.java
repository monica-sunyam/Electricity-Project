package com.tarifvergleich.electricity.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tarifvergleich.electricity.model.Customer;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {

	boolean existsByEmail(String email);

	Optional<Customer> findByEmail(String email);

	Page<Customer> findAll(Pageable pageable);

	Page<Customer> findAllByAdminAdminId(Integer adminId, Pageable pageable);
	
	Optional<Customer> findByCustomerIdAndAdminAdminId(Integer customerId, Integer adminId);

	List<Customer> findAllByAdminAdminIdOrderByJoinedOnDesc(Integer adminId);

	@Query("SELECT c FROM Customer c WHERE c.admin.adminId = :adminId AND ("
	        + "LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%')) OR "
	        + "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR "
	        + "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR "
	        + "LOWER(c.mobileNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR "
	        + "LOWER(c.companyName) LIKE LOWER(CONCAT('%', :search, '%'))"
	        + ")")
	Page<Customer> searchByAdminAndTerm(
	    @Param("adminId") Integer adminId, 
	    @Param("search") String search, 
	    Pageable pageable);

	@Query("SELECT c FROM Customer c WHERE c.admin.adminId = :adminId AND c.isVerified = :isVerified AND ("
			+ "LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%')) OR "
			+ "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR "
			+ "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR "
			+ "LOWER(c.mobileNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR "
			+ "LOWER(c.companyName) LIKE LOWER(CONCAT('%', :search, '%'))"
	        + ")")
	Page<Customer> searchVerifiedCustomers(@Param("adminId") Integer adminId, @Param("search") String search, @Param("isVerified") Boolean isVerified,
			Pageable pageable);

	@Query("SELECT c FROM Customer c WHERE c.admin.adminId = :adminId AND c.userType = :userType AND ("
			+ "LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%')) OR "
			+ "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR "
			+ "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR "
			+ "LOWER(c.mobileNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR "
			+ "LOWER(c.companyName) LIKE LOWER(CONCAT('%', :search, '%'))" 
			+")")
	Page<Customer> searchByUserTypeAndTerms(@Param("adminId") Integer adminId, @Param("search") String search, @Param("userType") String userType,
			Pageable pageable);

	@Query("SELECT c FROM Customer c WHERE c.admin.adminId = :adminId AND c.userType = :userType AND c.isVerified = :isVerified AND ("
			+ "LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%')) OR "
			+ "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR "
			+ "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR "
			+ "LOWER(c.mobileNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR "
			+ "LOWER(c.companyName) LIKE LOWER(CONCAT('%', :search, '%'))"
			+ ")")
	Page<Customer> searchByUserTypeAndVerifiedAndTerms(@Param("adminId") Integer adminId, @Param("search") String search, @Param("userType") String userType, @Param("isVerified") Boolean isVerified, Pageable pageable);

}
