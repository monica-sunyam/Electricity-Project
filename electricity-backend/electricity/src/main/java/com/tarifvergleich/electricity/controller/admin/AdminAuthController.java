package com.tarifvergleich.electricity.controller.admin;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tarifvergleich.electricity.model.AdminUser;
import com.tarifvergleich.electricity.service.MailService;
import com.tarifvergleich.electricity.service.admin.AdminAuthService;
import com.tarifvergleich.electricity.util.EmailTemplate;
import com.tarifvergleich.electricity.util.Helper;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@RequestMapping("/admin")
public class AdminAuthController {
	
	private final AdminAuthService adminService;
	private final Helper util;
	private final MailService mailService;
	private final EmailTemplate template;

	@PostMapping("/admin-register")
	public ResponseEntity<?> adminRegistration(@RequestBody AdminUser adminUser, HttpServletRequest request){
		adminUser.setIpAddress(util.getIp(request));
		return ResponseEntity.ok(adminService.adminRegister(adminUser));
	}
	
	@PostMapping("/admin-login")
	public ResponseEntity<?> adminLogin(@RequestBody Map<String, Object> credentials, HttpServletRequest request){
		String email = (String)credentials.get("email");
		String password = (String)credentials.get("password");
		String ipAddress = util.getIp(request);
		return ResponseEntity.ok(adminService.adminLogin(email, password, ipAddress));
	}
	
	@PostMapping("/admin-logout")
	public ResponseEntity<?> adminLogout(@RequestBody Map<String, Object> credentials){
		Integer id = (int)credentials.get("id");
		return ResponseEntity.ok(adminService.adminLogout(id));
	}
	
}
