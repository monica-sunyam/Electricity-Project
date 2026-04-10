package com.tarifvergleich.electricity.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tarifvergleich.electricity.dto.CustomerDto;
import com.tarifvergleich.electricity.service.CustomerService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@RequestMapping("/customer")
public class CustomerController {

	private final CustomerService customerService;
	
	@PostMapping("/signup")
	public ResponseEntity<?> customerSignUp(@RequestBody CustomerDto customerDto){
		return ResponseEntity.ok(customerService.customerSignUp(customerDto));
	}
	
	@PostMapping("/verify-otp")
	public ResponseEntity<?> verifyOtp(@RequestBody CustomerDto customerDto){
		return ResponseEntity.ok(customerService.verifyOtp(customerDto.getId(), customerDto.getOtp()));
	}
	
	@PostMapping("/resend-otp")
	public ResponseEntity<?> resendOtp(@RequestBody CustomerDto customerDto){
		return ResponseEntity.ok(customerService.resendOtp(customerDto.getId(), false));
	}
	
	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody CustomerDto customerDto, HttpServletRequest request){
		return ResponseEntity.ok(customerService.login(customerDto.getEmail(), customerDto.getPassword(), request));
	}
	
	@PostMapping("/mark-terms")
	public ResponseEntity<?> markAcknowledgment(@RequestBody CustomerDto customerDto, HttpServletRequest request){
		return ResponseEntity.ok(customerService.markAcknowledgement(customerDto.getId(), request));
	}
	
	@PostMapping("/fetch-customer")
	public ResponseEntity<?> fetchCustomer(@RequestBody CustomerDto customerDto){
		return ResponseEntity.ok(customerService.fetchCustomer(customerDto.getId()));
	}
	
	@PostMapping("/resend-forget-otp")
	public ResponseEntity<?> resendForgetOtp(@RequestBody CustomerDto customerDto){
		return ResponseEntity.ok(customerService.resendOtp(customerDto.getId(), true));
	}
	
	@PostMapping("/forget-password")
	public ResponseEntity<?> forgetPassword(@RequestBody CustomerDto customerDto){
		return ResponseEntity.ok(customerService.forgetPassword(customerDto.getEmail()));
	}
	
	@PostMapping("/reset-password")
	public ResponseEntity<?> resetPassword(@RequestBody CustomerDto customerDto){
		return ResponseEntity.ok(customerService.resetPassword(customerDto.getId(), customerDto.getPassword()));
	}
	
}
