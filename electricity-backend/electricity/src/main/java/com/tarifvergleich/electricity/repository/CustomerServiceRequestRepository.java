package com.tarifvergleich.electricity.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tarifvergleich.electricity.model.CustomerServiceRequest;

@Repository
public interface CustomerServiceRequestRepository extends JpaRepository<CustomerServiceRequest, Integer> {

	Long countByIsOpenAndCustomerCustomerId(Boolean isOpen, Integer customerId);

	Long countByInProgressAndCustomerCustomerId(Boolean inProgress, Integer customerId);

	Long countByIsClosedAndCustomerCustomerId(Boolean isClosed, Integer customerId);
	
	Optional<CustomerServiceRequest> findByIdAndAdminAdminId(Integer id, Integer adminId);
	
	List<CustomerServiceRequest> findAllByCustomerCustomerIdOrderByCreatedOnDesc(Integer customerId);

	List<CustomerServiceRequest> findAllByIsOpenAndCustomerCustomerIdOrderByCreatedOnDesc(Boolean isOpen, Integer customerId);

	List<CustomerServiceRequest> findAllByInProgressAndCustomerCustomerIdOrderByCreatedOnDesc(Boolean inProgress, Integer customerId);

	List<CustomerServiceRequest> findAllByIsClosedAndCustomerCustomerIdOrderByCreatedOnDesc(Boolean isClosed, Integer customerId);

	Page<CustomerServiceRequest> findAllByIsOpenAndCustomerCustomerId(Boolean isOpen, Integer customerId, Pageable pageable);

	Page<CustomerServiceRequest> findAllByInProgressAndCustomerCustomerId(Boolean inProgress, Integer customerId, Pageable pageable);

	Page<CustomerServiceRequest> findAllByIsClosedAndCustomerCustomerId(Boolean isClosed, Integer customerId, Pageable pageable);
	
	List<CustomerServiceRequest> findAllByAdminAdminIdOrderByCreatedOnDesc(Integer adminId);
	Page<CustomerServiceRequest> findAllByAdminAdminId(Integer adminId, Pageable pageable);
	Page<CustomerServiceRequest> findAllByIsOpenAndAdminAdminId(Boolean isOpen, Integer adminId, Pageable pageable);
	Page<CustomerServiceRequest> findAllByInProgressAndAdminAdminId(Boolean inProgress, Integer adminId, Pageable pageable);
	Page<CustomerServiceRequest> findAllByIsClosedAndAdminAdminId(Boolean isClosed, Integer adminId, Pageable pageable);
}
