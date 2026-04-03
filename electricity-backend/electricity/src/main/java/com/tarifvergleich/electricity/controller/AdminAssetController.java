package com.tarifvergleich.electricity.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.tarifvergleich.electricity.dto.AdminAssetDto;
import com.tarifvergleich.electricity.service.AdminAssetService;
import com.tarifvergleich.electricity.util.FileServiceSuperAdmin;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@RequestMapping("/asset")
public class AdminAssetController {

	private final AdminAssetService adminAssetService;
	private final FileServiceSuperAdmin fileUtil;

	@PostMapping(value = "/add-asset", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> addAsset(@RequestPart("data") AdminAssetDto assetDto,
			@RequestPart("file") MultipartFile file) {
		Map<String, Object> response = adminAssetService.addAsset(assetDto, file);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/get-all")
	public ResponseEntity<Map<String, Object>> getAll(@RequestBody Map<String, Integer> payload) {
		Integer adminId = (int) payload.get("adminId");
		Map<String, Object> response = adminAssetService.getAllAssets(adminId);
		return ResponseEntity.ok(response);
	}
	
	@GetMapping("/view/{contentType}/{fileName}")
	public ResponseEntity<Resource> getFile(
	        @PathVariable String contentType, 
	        @PathVariable String fileName) {
	    
	    String relativePath = contentType + "/" + fileName;
	    
	    Resource resource = fileUtil.loadFile(relativePath);

	    String detectedContentType = "application/octet-stream";
	    try {
	        detectedContentType = Files.probeContentType(Paths.get(resource.getURI()));
	    } catch (IOException e) {
	        
	    }

	    return ResponseEntity.ok()
	            .contentType(MediaType.parseMediaType(detectedContentType))

	            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
	            .body(resource);
	}
}
