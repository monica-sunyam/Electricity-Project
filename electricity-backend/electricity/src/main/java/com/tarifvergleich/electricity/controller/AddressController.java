package com.tarifvergleich.electricity.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tarifvergleich.electricity.service.AddressService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class AddressController {
	
	private final AddressService addressService;
	
	@PostMapping("/cities")
	public ResponseEntity<?> fetchCities(@RequestBody Map<String, String> request){
		String zip = request.get("zip");
	    return ResponseEntity.ok(addressService.getCitiesByZip(zip));
	}
	
	@PostMapping("/cities-egon")
	public ResponseEntity<?> fetchCitiesFromEgon(@RequestBody Map<String, String> request){
		String zip = request.get("zip");
		return ResponseEntity.ok(addressService.fetchCities(zip));
	}
	
	@PostMapping("/streets-by-zip")
	public ResponseEntity<?> fetchStreets(@RequestBody Map<String, Object> payload){
		String placeId = payload.get("placeId").toString();
		return ResponseEntity.ok(addressService.getStreetsByCity(placeId));		
	}
	
	@PostMapping("/streets-by-city-zip")
	public ResponseEntity<?> fetchStreetsByEgon(@RequestBody Map<String, String> payload){
		String zip = payload.get("zip");
		String city= payload.get("city");
		return ResponseEntity.ok(addressService.fetchStreet(zip, city));		
	}
}