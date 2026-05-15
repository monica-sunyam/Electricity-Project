package com.tarifvergleich.electricity.controller.customer;

import java.util.HashMap;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarifvergleich.electricity.dto.ListOfHolidaysDto;
import com.tarifvergleich.electricity.service.customer.CustomerDetailService;
import com.tarifvergleich.electricity.service.customer.CustomerGeneralService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@RequestMapping("/customer")
public class CustomerPublicController {

	private final CustomerDetailService customerDetailService;
	private final CustomerGeneralService customerGeneralService;
	private final ObjectMapper objectMapper;

	@PostMapping("add-contract-signature")
	public ResponseEntity<?> addCustomerContractSignatures(@RequestPart("data") String token,
			@RequestPart(value = "signature", required = true) MultipartFile signature,
			@RequestPart("signatureBank") MultipartFile signatureBank,
			@RequestPart("signatureCustomer") MultipartFile signatureCustomer,
			@RequestPart("signatureDataProtection") MultipartFile signatureDataProtection) {

		try {
			token = objectMapper.readValue(token, String.class);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}

		Map<String, MultipartFile> files = new HashMap<String, MultipartFile>();
		if (signature != null)
			files.put("signature", signature);
		if (signatureBank != null)
			files.put("signatureBank", signatureBank);
		if (signatureCustomer != null)
			files.put("signatureCustomer", signatureCustomer);
		if (signatureDataProtection != null)
			files.put("signatureDataProtection", signatureDataProtection);

		return ResponseEntity.ok(customerDetailService.submitCustomerContractSignatures(token, files));
	}

	@PostMapping("/list-of-working-days")
	public ResponseEntity<?> getListOfWorkingDays(@RequestBody ListOfHolidaysDto listOfHolidaysDto) {
		return ResponseEntity.ok(customerGeneralService.fetchWorkingDays(listOfHolidaysDto.getAdminId()));
	}

}
