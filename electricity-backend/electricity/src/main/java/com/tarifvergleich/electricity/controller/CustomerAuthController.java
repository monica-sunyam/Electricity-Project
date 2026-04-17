package com.tarifvergleich.electricity.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tarifvergleich.electricity.dto.CustomerDto;
import com.tarifvergleich.electricity.service.customer.CustomerAuthService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@RequestMapping("/auth")
public class CustomerAuthController {

	private final CustomerAuthService customerAuthService;
	
	@PostMapping("/signup")
	public ResponseEntity<?> customerSignUp(@RequestBody CustomerDto customerDto){
		return ResponseEntity.ok(customerAuthService.customerSignUp(customerDto));
	}
	
	@PostMapping("/verify-otp")
	public ResponseEntity<?> verifyOtp(@RequestBody CustomerDto customerDto){
		return ResponseEntity.ok(customerAuthService.verifyOtp(customerDto.getId(), customerDto.getOtp()));
	}
	
	@PostMapping("/resend-otp")
	public ResponseEntity<?> resendOtp(@RequestBody CustomerDto customerDto){
		return ResponseEntity.ok(customerAuthService.resendOtp(customerDto.getId(), false));
	}
	
	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody CustomerDto customerDto, HttpServletRequest request){
		return ResponseEntity.ok(customerAuthService.login(customerDto.getEmail(), customerDto.getPassword(), request));
	}
	
	@PostMapping("/mark-terms")
	public ResponseEntity<?> markAcknowledgment(@RequestBody CustomerDto customerDto, HttpServletRequest request){
		return ResponseEntity.ok(customerAuthService.markAcknowledgement(customerDto.getId(), request));
	}
	
	@PostMapping("/resend-forget-otp")
	public ResponseEntity<?> resendForgetOtp(@RequestBody CustomerDto customerDto){
		return ResponseEntity.ok(customerAuthService.resendOtp(customerDto.getId(), true));
	}
	
	@PostMapping("/forget-password")
	public ResponseEntity<?> forgetPassword(@RequestBody CustomerDto customerDto){
		return ResponseEntity.ok(customerAuthService.forgetPassword(customerDto.getEmail()));
	}
	
	@PostMapping("/reset-password")
	public ResponseEntity<?> resetPassword(@RequestBody CustomerDto customerDto){
		return ResponseEntity.ok(customerAuthService.resetPassword(customerDto.getId(), customerDto.getPassword()));
	}
	
	@PostMapping("/register-login")
	public ResponseEntity<?> loginAfterRegistration(@RequestBody CustomerDto customerDto, HttpServletRequest request){
		return ResponseEntity.ok(customerAuthService.loginAfterRegistration(customerDto.getId(), request));
	}
	
}
