package com.tarifvergleich.electricity.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tarifvergleich.electricity.dto.CustomerConnectWrapper;
import com.tarifvergleich.electricity.dto.CustomerContactScheduleRequestDto;
import com.tarifvergleich.electricity.dto.CustomerDeliveryRequestWrapper;
import com.tarifvergleich.electricity.dto.CustomerDto;
import com.tarifvergleich.electricity.dto.CustomerPaymentRequestDto;
import com.tarifvergleich.electricity.service.customer.CustomerBookingService;
import com.tarifvergleich.electricity.service.customer.CustomerDetailService;

import lombok.RequiredArgsConstructor;

@RestController
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@RequestMapping("/customer")
public class CustomerController {

	private final CustomerBookingService customerBookingService;
	private final CustomerDetailService customerDetailService;

	@PostMapping("/fetch-customer-detail")
	public ResponseEntity<?> fetchCustomer(@RequestBody CustomerDto customerDto) {
		return ResponseEntity.ok(customerDetailService.getCustomerDetails(customerDto.getId()));
	}

	@PostMapping("/add-delivery")
	public ResponseEntity<?> addDelivery(@RequestBody CustomerDeliveryRequestWrapper deliveryWrapper) {
		return ResponseEntity.ok(customerBookingService.saveDelivery(deliveryWrapper.getCustomerId(), deliveryWrapper.getDeliveryId(),
				deliveryWrapper.getDeliveryAddress(), deliveryWrapper.getBillingAddress(), deliveryWrapper.getProvider()));
	}

	@PostMapping("/add-connection")
	public ResponseEntity<?> addConnection(@RequestBody CustomerConnectWrapper payload) {
		return ResponseEntity.ok(customerBookingService.saveConnection(payload.getCustomerId(), payload.getDeliveryId(),
				payload.getConnectionData()));
	}

	@PostMapping("/add-payment")
	public ResponseEntity<?> addPayment(@RequestBody CustomerPaymentRequestDto paymentDto) {
		return ResponseEntity.ok(customerBookingService.savePayment(paymentDto));
	}

	@PostMapping("/fetch-form")
	public ResponseEntity<?> fetchForm(@RequestBody Map<String, Object> payload) {

		Integer customerId = payload.get("customerId") != null ? (Integer) payload.get("customerId") : 0;
		Integer deliveryId = payload.get("deliveryId") != null ? (Integer) payload.get("deliveryId") : 0;
		Integer step = payload.get("step") != null ? (Integer) payload.get("step") : 0;

		return ResponseEntity.ok(customerBookingService.fetchByStep(customerId, deliveryId, step));
	}
	
	@PostMapping("/submit-declaration")
	public ResponseEntity<?> submitDeclaration(@RequestBody Map<String, Object> payload){
		Integer customerId = payload.get("customerId") != null ? (Integer) payload.get("customerId") : 0;
		Integer deliveryId = payload.get("deliveryId") != null ? (Integer) payload.get("deliveryId") : 0;
		
		return ResponseEntity.ok(customerBookingService.submit(customerId, deliveryId));
	}
	
	@PostMapping("/add-schedule")
	public ResponseEntity<?> addCustomerSchedule(@RequestBody CustomerContactScheduleRequestDto schedule){
		return ResponseEntity.ok(customerBookingService.submitCustomerSchedule(schedule));
	}
	
}
