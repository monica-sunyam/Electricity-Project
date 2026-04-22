package com.tarifvergleich.electricity.service.admin;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.tarifvergleich.electricity.dto.CustomerComparingEnergyDto;
import com.tarifvergleich.electricity.dto.CustomerDeliveryDto;
import com.tarifvergleich.electricity.dto.CustomerDeliveryResponseDto;
import com.tarifvergleich.electricity.dto.CustomerDeliveryResponseDto.CustomerDeliveryResponseAll;
import com.tarifvergleich.electricity.dto.CustomerDto;
import com.tarifvergleich.electricity.dto.CustomerDto.AdminCustomerResponse;
import com.tarifvergleich.electricity.dto.CustomerDto.SingleCustomerResponseDelivery;
import com.tarifvergleich.electricity.exception.InternalServerException;
import com.tarifvergleich.electricity.model.Customer;
import com.tarifvergleich.electricity.model.CustomerComparingEnergy;
import com.tarifvergleich.electricity.model.CustomerDelivery;
import com.tarifvergleich.electricity.repository.CustomerComparingEnergyRepository;
import com.tarifvergleich.electricity.repository.CustomerDeliveryRepository;
import com.tarifvergleich.electricity.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminCustomerManagementService {

	private final CustomerRepository customerRepo;
	private final CustomerDeliveryRepository customerDeliveryRepo;
	private final CustomerComparingEnergyRepository customerComparingEnergyRepo;

	public Map<String, Object> getCustomers(CustomerDto customerReq) {

		if (customerReq.getAdminId() == null || customerReq.getAdminId() <= 0)
			throw new InternalServerException("Admin id missing", HttpStatus.OK);

		if (customerReq.getId() != null && customerReq.getId() > 0) {
			Customer customer = customerRepo.findById(customerReq.getId()).orElseThrow(
					() -> new InternalServerException("Customer not found with this credential", HttpStatus.OK));

			if (customer.getAdmin().getAdminId() != customerReq.getAdminId())
				throw new InternalServerException("Not authorised to access customer details", HttpStatus.OK);

			SingleCustomerResponseDelivery customerRes = CustomerDto.getCustomerResponseDto(customer);

			return Map.of("res", true, "data", customerRes);

		} else if (customerReq.getPage() != null) {

			Pageable pageable = PageRequest.of(customerReq.getPage() - 1, 5, Sort.by("joinedOn").descending());

			Page<Customer> customers = null;

			String search = customerReq.getSearch();
			String userType = customerReq.getUserType();
			Boolean isVerified = customerReq.getIsVerified();
			Integer adminId = customerReq.getAdminId();

			if (search != null && userType != null && !userType.isEmpty() && isVerified != null)
				customers = customerRepo.searchByUserTypeAndVerifiedAndTerms(adminId, search.trim(),
						userType.trim().toUpperCase(), isVerified, pageable);
			else if (search != null && userType != null && !userType.isEmpty())
				customers = customerRepo.searchByUserTypeAndTerms(adminId, search.trim(), userType.trim().toUpperCase(),
						pageable);
			else if (search != null && isVerified != null)
				customers = customerRepo.searchVerifiedCustomers(adminId, search.trim(), isVerified, pageable);
			else if (search != null)
				customers = customerRepo.searchByAdminAndTerm(adminId, search.trim(), pageable);
			else
				customers = customerRepo.findAllByAdminAdminId(customerReq.getAdminId(), pageable);

			Page<AdminCustomerResponse> customerRes = customers.map(CustomerDto::getCustomerDtoResponseForAdmin);

			return Map.of("res", true, "data", customerRes.getContent(), "page",
					customerRes.getPageable().getPageNumber() + 1, "totalPage", customerRes.getTotalPages());

		}

		List<Customer> customers = customerRepo.findAllByAdminAdminIdOrderByJoinedOnDesc(customerReq.getAdminId());

		List<AdminCustomerResponse> customerRes = customers.stream().map(CustomerDto::getCustomerDtoResponseForAdmin)
				.toList();

		return Map.of("res", true, "data", customerRes);

	}

	public Map<String, Object> getAllDeliveries(CustomerDeliveryDto deliveryReq) {

		if (deliveryReq.getAdminId() == null || deliveryReq.getAdminId() <= 0)
			throw new InternalServerException("Admin id missing", HttpStatus.OK);

		if (deliveryReq.getPage() != null) {

			Pageable pageable = PageRequest.of(deliveryReq.getPage() - 1, 5, Sort.by("orderPlacedOn").descending());

			Page<CustomerDelivery> customerDeliveries = customerDeliveryRepo.findAll(pageable);

			Page<CustomerDeliveryResponseAll> customerDeliveryResponse = customerDeliveries
					.map(CustomerDeliveryResponseDto::getDeliveryResponse);

			return Map.of("res", true, "data", customerDeliveryResponse.getContent(), "page",
					customerDeliveryResponse.getPageable().getPageNumber() + 1, "totalPage",
					customerDeliveryResponse.getTotalPages());

		}

		List<CustomerDelivery> customerDeliveries = customerDeliveryRepo
				.findAllByAdminAdminIdOrderByOrderPlacedOnDesc(deliveryReq.getAdminId());
		List<CustomerDeliveryResponseAll> customerDeliveryResponse = customerDeliveries.stream()
				.map(CustomerDeliveryResponseDto::getDeliveryResponse).toList();

		return Map.of("res", true, "data", customerDeliveryResponse);
	}

	public Map<String, Object> getAllComparison(Integer adminId, Integer page) {

		if (adminId == null || adminId <= 0)
			throw new InternalServerException("Admin id missing", HttpStatus.OK);

		if (page != null && page > 0) {

			Pageable pageable = PageRequest.of(page - 1, 5, Sort.by("comparedOn").descending());

			Page<CustomerComparingEnergy> energyComparisons = customerComparingEnergyRepo.findAll(pageable);

			Page<CustomerComparingEnergyDto> energyComparisonResp = energyComparisons
					.map(CustomerComparingEnergyDto::customerComparisonResponse);

			return Map.of("res", true, "data", energyComparisonResp.getContent(), "page",
					energyComparisonResp.getPageable().getPageNumber() + 1, "totalPage",
					energyComparisonResp.getTotalPages());
		}

		List<CustomerComparingEnergy> energyComparisons = customerComparingEnergyRepo
				.findAllByAdminAdminIdOrderByIdDesc(adminId);
		List<CustomerComparingEnergyDto> energyComparisonResp = energyComparisons.stream()
				.map(CustomerComparingEnergyDto::customerComparisonResponse).toList();

		return Map.of("res", true, "data", energyComparisonResp);
	}

}
