package com.tarifvergleich.electricity.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tarifvergleich.electricity.service.admin.ViewService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ViewController {

	private final ViewService viewService;
	
	@PostMapping("/content")
	public ResponseEntity<?> getAllContent(){
		return ResponseEntity.ok(viewService.getAllView());
	}
}
