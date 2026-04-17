package com.tarifvergleich.electricity.controller.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tarifvergleich.electricity.dto.CustomerDto;
import com.tarifvergleich.electricity.service.admin.AdminCustomerManagementService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
@CrossOrigin(origins = "*")
public class AdminCustomerManagementController {

	
	private final AdminCustomerManagementService adminCustomerManagementService;
	
	@PostMapping("/fetch-customer-details")
	public ResponseEntity<?> getCustomer(@RequestBody CustomerDto payload){
		return ResponseEntity.ok(adminCustomerManagementService.getCustomers(payload));
	}
}
