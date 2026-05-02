package com.tarifvergleich.electricity.controller.customer;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarifvergleich.electricity.dto.CustomerAddressDto;
import com.tarifvergleich.electricity.dto.CustomerAttornyDto;
import com.tarifvergleich.electricity.dto.CustomerConnectWrapper;
import com.tarifvergleich.electricity.dto.CustomerContactScheduleRequestDto;
import com.tarifvergleich.electricity.dto.CustomerDeliveryRequestWrapper;
import com.tarifvergleich.electricity.dto.CustomerDto;
import com.tarifvergleich.electricity.dto.CustomerPaymentRequestDto;
import com.tarifvergleich.electricity.dto.CustomerServiceRequestDto;
import com.tarifvergleich.electricity.dto.CustomerServicesDto;
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
	private final ObjectMapper objectMapper;

	@PostMapping("/fetch-customer-detail")
	public ResponseEntity<?> fetchCustomer(@RequestBody CustomerDto customerDto) {
		return ResponseEntity.ok(customerDetailService.getCustomerDetails(customerDto.getId()));
	}

	@PostMapping("/add-delivery")
	public ResponseEntity<?> addDelivery(@RequestBody CustomerDeliveryRequestWrapper deliveryWrapper) {
		return ResponseEntity.ok(customerBookingService.saveDelivery(deliveryWrapper.getCustomerId(),
				deliveryWrapper.getDeliveryId(), deliveryWrapper.getDeliveryAddress(),
				deliveryWrapper.getBillingAddress(), deliveryWrapper.getProvider()));
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
	public ResponseEntity<?> submitDeclaration(@RequestBody Map<String, Object> payload) {
		Integer customerId = payload.get("customerId") != null ? (Integer) payload.get("customerId") : 0;
		Integer deliveryId = payload.get("deliveryId") != null ? (Integer) payload.get("deliveryId") : 0;

		return ResponseEntity.ok(customerBookingService.submit(customerId, deliveryId));
	}

	@PostMapping("/add-schedule")
	public ResponseEntity<?> addCustomerSchedule(@RequestBody CustomerContactScheduleRequestDto schedule) {
		return ResponseEntity.ok(customerBookingService.submitCustomerSchedule(schedule));
	}

	@PostMapping(value = "/add-attorny")
	public ResponseEntity<?> addCustomerAttorny(@RequestPart("data") String jsonData,
			@RequestPart(value = "file") MultipartFile file) {

		CustomerAttornyDto attornyDto;
		try {
			attornyDto = objectMapper.readValue(jsonData, CustomerAttornyDto.class);
		} catch (JsonMappingException e) {
			e.printStackTrace();
			throw new RuntimeException();
		} catch (JsonProcessingException e) {
			throw new RuntimeException();
		}
		return ResponseEntity.ok(customerDetailService.submitCustomerAttorny(attornyDto, file));
	}

	@PostMapping("/fetch-placed-deliveries")
	public ResponseEntity<?> fetchCustomerWithPlacedDelivery(@RequestBody CustomerDto customerDto) {
		return ResponseEntity.ok(customerDetailService.fetchAllCustomerDeliveries(customerDto.getId()));
	}

	@PostMapping("/fetch-cutomer-service")
	public ResponseEntity<?> fetchCustomerServices(@RequestBody CustomerServicesDto serviceDto) {
		return ResponseEntity
				.ok(customerDetailService.fetchCustomerServices(serviceDto.getAdminId(), serviceDto.getServiceType()));
	}

	@PostMapping("/add-service-request")
	public ResponseEntity<?> addServiceRequestAndMessage(@RequestBody CustomerServiceRequestDto serviceRequestDto) {
		return ResponseEntity.ok(customerDetailService.addRequestAndMessage(serviceRequestDto));
	}

	@PostMapping("/fetch-request-messages")
	public ResponseEntity<?> fetchServiceMessages(@RequestBody CustomerServiceRequestDto serviceRequestDto) {
		return ResponseEntity.ok(customerDetailService.getAllMessages(serviceRequestDto.getServiceRequestId()));
	}

	@PostMapping("/fetch-service-count")
	public ResponseEntity<?> fetchRequestCount(@RequestBody CustomerDto customerDto) {
		return ResponseEntity.ok(customerDetailService.getCountOfRequestInDifferentTabs(customerDto.getId()));
	}

	@PostMapping("/fetch-all-requests")
	public ResponseEntity<?> fetchAllCustomerRequests(@RequestBody CustomerDto customerDto) {
		return ResponseEntity.ok(customerDetailService.fetchAllCustomerServiceRequest(customerDto.getId()));
	}

	@PostMapping("/check-attorny")
	public ResponseEntity<?> checkAttorny(@RequestBody CustomerDto customerDto) {
		return ResponseEntity.ok(customerDetailService.checkAttornyStatus(customerDto.getId()));
	}

	@PostMapping("/revoke-attorny")
	public ResponseEntity<?> revokeAttorny(@RequestBody CustomerDto customerDto) {
		return ResponseEntity.ok(customerDetailService.revokeAttorny(customerDto.getId()));
	}

	@PostMapping("/check-booking")
	public ResponseEntity<?> checkBooking(@RequestBody CustomerAddressDto addressDto) {
		return ResponseEntity.ok(customerDetailService.checkForBookings(addressDto));
	}

	@PostMapping("/send-attachment-mail")
	public ResponseEntity<?> sendAttachmentMail(@RequestBody CustomerDto customerDto) {
		return ResponseEntity
				.ok(customerBookingService.sendUnsignedDocumentByEmail(customerDto.getAdminId(), customerDto.getId()));
	}

}
