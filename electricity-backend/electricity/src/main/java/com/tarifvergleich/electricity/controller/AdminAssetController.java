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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.tarifvergleich.electricity.dto.AdminAssetDto;
import com.tarifvergleich.electricity.dto.AdminServiceMenuDto;
import com.tarifvergleich.electricity.service.AdminAssetService;
import com.tarifvergleich.electricity.service.ViewService;
import com.tarifvergleich.electricity.util.FileServiceSuperAdmin;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@RequestMapping("/admin")
public class AdminAssetController {

	private final AdminAssetService adminAssetService;
	private final ViewService viewService;
	private final FileServiceSuperAdmin fileUtil;

	@PostMapping(value = "/add-menu")
	public ResponseEntity<?> addAsset(@RequestParam("data") String assetDtoJson,
			@RequestPart(value = "file", required = false) MultipartFile file) {
		AdminAssetDto assetDto = new ObjectMapper().readValue(assetDtoJson, AdminAssetDto.class);
		Map<String, Object> response = adminAssetService.addAsset(assetDto, file);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/get-all-menu")
	public ResponseEntity<Map<String, Object>> getAll(@RequestBody Map<String, Integer> payload) {
		Integer adminId = (int) payload.get("adminId");
		Integer type = 0;
		if(payload.get("type") != null)
			type = (int) payload.get("type");
		Map<String, Object> response = adminAssetService.getAllAssets(adminId, type);
		return ResponseEntity.ok(response);
	}
	
	@PostMapping("/add-service-menu")
	public ResponseEntity<?> addServiceMenu(@RequestParam("data") String serviceMenuDto, @RequestPart(name = "file", required = false) MultipartFile file){
		AdminServiceMenuDto menuDto = new ObjectMapper().readValue(serviceMenuDto, AdminServiceMenuDto.class);
		return ResponseEntity.ok(adminAssetService.addServiceMenu(menuDto, file));
	}
	
	@PostMapping("/get-all-service-menu")
	public ResponseEntity<?> getAllService(@RequestBody AdminServiceMenuDto menuDto){
		return ResponseEntity.ok(adminAssetService.getAllAdminServiceMenu(menuDto.getAdminId(), menuDto.getType(), menuDto.getHighlight()));
	}
	
	@PostMapping("/delete-menu")
	public ResponseEntity<?> deleteAsset(@RequestBody AdminAssetDto assetDto){
		return ResponseEntity.ok(adminAssetService.deleteAsset(assetDto.getAdminId(), assetDto.getId()));
	}
	
	@PostMapping("/delete-service-menu")
	public ResponseEntity<?> deleteServiceMenu(@RequestBody AdminServiceMenuDto menuDto){
		return ResponseEntity.ok(adminAssetService.deleteService(menuDto.getAdminId(), menuDto.getId()));
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
	
	@PostMapping("/content")
	public ResponseEntity<?> getAllContents(){
		return ResponseEntity.ok(viewService.getAllView());
	}
}
