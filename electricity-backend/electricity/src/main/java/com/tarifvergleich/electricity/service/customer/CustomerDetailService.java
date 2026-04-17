package com.tarifvergleich.electricity.service.customer;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.tarifvergleich.electricity.dto.CustomerDto;
import com.tarifvergleich.electricity.exception.InternalServerException;
import com.tarifvergleich.electricity.model.Customer;
import com.tarifvergleich.electricity.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomerDetailService {

	private final CustomerRepository customerRepo;

	public Map<String, Object> getCustomerDetails(Integer customerId) {

		if (customerId == null || customerId <= 0)
			throw new InternalServerException("Customer id missing", HttpStatus.OK);

		Customer customer = customerRepo.findById(customerId).orElseThrow(
				() -> new InternalServerException("Customer missing with this credentials", HttpStatus.OK));

		return Map.of("res", true, "data", CustomerDto.getCustomerResponseDto(customer));
	}
}
