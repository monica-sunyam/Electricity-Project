package com.tarifvergleich.electricity.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tarifvergleich.electricity.dto.CustomerRequestCounsellingDto;
import com.tarifvergleich.electricity.service.GeneralService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@RequestMapping("/api")
public class PublicController {

	private final GeneralService generalService;
	
	@PostMapping("/check-holiday")
	public ResponseEntity<?> checkDate(@RequestBody CustomerRequestCounsellingDto counsellingReqDto){
		return ResponseEntity.ok(generalService.checkDateIsHoliday(counsellingReqDto));
	}
	
	@PostMapping("/add-counselling-request")
	public ResponseEntity<?> addCounsellingRequest(@RequestBody CustomerRequestCounsellingDto counsellingDto){
		return ResponseEntity.ok(generalService.addCounsellingSchedule(counsellingDto));
	}
}
