package com.tarifvergleich.electricity.controller.admin;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tarifvergleich.electricity.dto.CustomerDeliveryDto;
import com.tarifvergleich.electricity.dto.CustomerDto;
import com.tarifvergleich.electricity.dto.CustomerServiceRequestDto;
import com.tarifvergleich.electricity.dto.CustomerServicesDto;
import com.tarifvergleich.electricity.service.admin.AdminCustomerManagementService;
import com.tarifvergleich.electricity.service.admin.AdminServicePointManagementService;
import com.tarifvergleich.electricity.service.customer.CustomerDetailService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
@CrossOrigin(origins = "*")
@Tag(name = "Customer Management", description = "Endpoints for managing customer and customer related operations")
public class AdminCustomerManagementController {

	private final AdminCustomerManagementService adminCustomerManagementService;
	private final AdminServicePointManagementService servicePointManagementService;
	private final CustomerDetailService customerDetailService;

	@Operation(summary = "Fetch customer", description = "Returns a list of customer with there details")
	@PostMapping("/fetch-customer-details")
	public ResponseEntity<?> getCustomer(@RequestBody CustomerDto payload) {
		return ResponseEntity.ok(adminCustomerManagementService.getCustomers(payload));
	}

	@PostMapping("/fetch-deliveries")
	public ResponseEntity<?> getDeliveries(@RequestBody CustomerDeliveryDto payload) {
		return ResponseEntity.ok(adminCustomerManagementService.getAllDeliveries(payload));
	}

	@PostMapping("/fetch-customer-comparisons")
	public ResponseEntity<?> getCustomerComparisons(@RequestBody Map<String, Object> payload) {
		Integer adminId = (Integer) payload.get("adminId");
		Integer page = (Integer) payload.get("page");
		Integer size = (Integer) payload.get("size");
		return ResponseEntity.ok(adminCustomerManagementService.getAllComparison(adminId, page, size));
	}

	@PostMapping("/add-customer-service")
	public ResponseEntity<?> addCustomerService(@RequestBody CustomerServicesDto servicesDto) {
		return ResponseEntity.ok(servicePointManagementService.addCustomerServices(servicesDto));
	}

	@PostMapping("/remove-customer-service")
	public ResponseEntity<?> removeCustomerService(@RequestBody CustomerServicesDto servicesDto) {
		return ResponseEntity.ok(servicePointManagementService.removeCustomerService(servicesDto));
	}

	@PostMapping("/add-service-request-response")
	public ResponseEntity<?> addResponseToCustomerServiceRequest(
			@RequestBody CustomerServiceRequestDto serviceRequestDto) {
		return ResponseEntity.ok(adminCustomerManagementService.addResponseToCustomerServiceRequest(serviceRequestDto));
	}

	@PostMapping("/fetch-request-messages")
	public ResponseEntity<?> fetchServiceMessages(@RequestBody CustomerServiceRequestDto serviceRequestDto) {
		return ResponseEntity.ok(customerDetailService.getAllMessages(serviceRequestDto.getServiceRequestId()));
	}

	@PostMapping("/fetch-service-requests")
	public ResponseEntity<?> fetchServiceRequests(@RequestBody CustomerServiceRequestDto serviceRequestDto) {
		return ResponseEntity.ok(adminCustomerManagementService.fetchCustomerServiceRequests(serviceRequestDto));
	}

	@PostMapping("/fetch-services")
	public ResponseEntity<?> fetchServices(@RequestBody CustomerServicesDto servicesDto) {
		return ResponseEntity.ok(servicePointManagementService.fetchServices(servicesDto));
	}
}
