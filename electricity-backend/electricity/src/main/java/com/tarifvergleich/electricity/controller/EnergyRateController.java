package com.tarifvergleich.electricity.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.tarifvergleich.electricity.service.EnergyService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class EnergyRateController {

	private final EnergyService energyService;
	
	@PostMapping("/get-rates")
	public ResponseEntity<?> getRates(@RequestBody Map<String, Object> payload){
		return ResponseEntity.ok(energyService.getRates(payload));
	}
}
