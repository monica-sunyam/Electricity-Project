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
import com.tarifvergleich.electricity.dto.CustomerServicesDto;
import com.tarifvergleich.electricity.service.admin.AdminCustomerManagementService;
import com.tarifvergleich.electricity.service.admin.AdminServicePointManagementService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
@CrossOrigin(origins = "*")
public class AdminCustomerManagementController {

	
	private final AdminCustomerManagementService adminCustomerManagementService;
	private final AdminServicePointManagementService servicePointManagementService;
	
	@PostMapping("/fetch-customer-details")
	public ResponseEntity<?> getCustomer(@RequestBody CustomerDto payload){
		return ResponseEntity.ok(adminCustomerManagementService.getCustomers(payload));
	}
	
	@PostMapping("/fetch-deliveries")
	public ResponseEntity<?> getDeliveries(@RequestBody CustomerDeliveryDto payload){
		return ResponseEntity.ok(adminCustomerManagementService.getAllDeliveries(payload));
	}
	
	@PostMapping("/fetch-customer-comparisons")
	public ResponseEntity<?> getCustomerComparisons(@RequestBody Map<String, Object> payload){
		Integer adminId = (Integer) payload.get("adminId");
		Integer page = (Integer) payload.get("page");
		Integer size = (Integer) payload.get("size");
		return ResponseEntity.ok(adminCustomerManagementService.getAllComparison(adminId, page, size));
	}
	
	@PostMapping("/add-customer-service")
	public ResponseEntity<?> addCustomerService(@RequestBody CustomerServicesDto servicesDto){
		return ResponseEntity.ok(servicePointManagementService.addCustomerServices(servicesDto));
	}
	
	@PostMapping("/remove-customer-service")
	public ResponseEntity<?> removeCustomerService(@RequestBody CustomerServicesDto servicesDto){
		return ResponseEntity.ok(servicePointManagementService.removeCustomerService(servicesDto));
	}
}
