package com.tarifvergleich.electricity.service.admin;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.tarifvergleich.electricity.dto.CustomerDto;
import com.tarifvergleich.electricity.dto.CustomerDto.AdminCustomerResponse;
import com.tarifvergleich.electricity.dto.CustomerDto.SingleCustomerResponseDelivery;
import com.tarifvergleich.electricity.exception.InternalServerException;
import com.tarifvergleich.electricity.model.AdminUser;
import com.tarifvergleich.electricity.model.Customer;
import com.tarifvergleich.electricity.repository.AdminUserRepository;
import com.tarifvergleich.electricity.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminCustomerManagementService {

	private final CustomerRepository customerRepo;
	private final AdminUserRepository adminRepo;

	public Map<String, Object> getCustomers(CustomerDto customerReq) {

		if (customerReq.getAdminId() == null || customerReq.getAdminId() <= 0)
			throw new InternalServerException("Admin id missing", HttpStatus.OK);

		if (customerReq.getPage() != null && customerReq.getSize() != null) {

			Pageable pageable = PageRequest.of(customerReq.getPage(), customerReq.getSize(),
					Sort.by("joinedOn").descending());

			Page<Customer> customers = customerRepo.findAllByAdminAdminId(customerReq.getAdminId(), pageable);

			Page<AdminCustomerResponse> customerRes = customers.map(CustomerDto::getCustomerDtoResponseForAdmin);

			return Map.of("res", true, "data", customerRes);

		} else if (customerReq.getId() != null && customerReq.getId() > 0) {
			Customer customer = customerRepo.findById(customerReq.getId()).orElseThrow(
					() -> new InternalServerException("Customer not found with this credential", HttpStatus.OK));

			if (customer.getAdmin().getAdminId() != customerReq.getAdminId())
				throw new InternalServerException("Not authorised to access customer details", HttpStatus.OK);

			SingleCustomerResponseDelivery customerRes = CustomerDto.getCustomerResponseDto(customer);

			return Map.of("res", true, "data", customerRes);
		}

		List<Customer> customers = customerRepo.findAllByAdminAdminIdOrderByJoinedOnDesc(customerReq.getAdminId());

		List<AdminCustomerResponse> customerRes = customers.stream().map(CustomerDto::getCustomerDtoResponseForAdmin)
				.toList();

		return Map.of("res", true, "data", customerRes);

	}
	
	public Map<String, Object> getAllDeliveries(Integer adminId){
		
		if(adminId == null || adminId <= 0)
			throw new InternalServerException("Admin id missing", HttpStatus.OK);
		
		AdminUser admin = adminRepo.findById(adminId).orElseThrow(() -> new InternalServerException("Admin not found with this credential", HttpStatus.OK));
		
//		List<CustomerDelivery>
		
		return Map.of();
	}
	
	
}
