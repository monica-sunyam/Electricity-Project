package com.tarifvergleich.electricity.service.admin;

import java.math.BigInteger;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.tarifvergleich.electricity.exception.InternalServerException;
import com.tarifvergleich.electricity.model.AdminLoginHistory;
import com.tarifvergleich.electricity.model.AdminUser;
import com.tarifvergleich.electricity.repository.AdminLoginHistoryRepository;
import com.tarifvergleich.electricity.repository.AdminUserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminAuthService {

	private final AdminUserRepository adminUserRepo;
	private final AdminLoginHistoryRepository adminLoginRepo;

	@Transactional
	public Map<String, Object> adminRegister(AdminUser adminUser) {

		if (adminUser.getName() == null || adminUser.getName().isEmpty())
			throw new InternalServerException("Invalid Name field", HttpStatus.BAD_REQUEST);

		if (adminUser.getEmail() == null || adminUser.getEmail().isEmpty())
			throw new InternalServerException("Invalid Email", HttpStatus.BAD_REQUEST);

		if (adminUser.getPassword() == null || adminUser.getPassword().isEmpty())
			throw new InternalServerException("Invalid Password", HttpStatus.BAD_REQUEST);

		if (adminUser.getAdminRole() == null)
			throw new InternalServerException("Admin Role Missing", HttpStatus.BAD_REQUEST);

		// Validation for refreshtoken

		AdminLoginHistory adminLoginHistory = AdminLoginHistory.builder().loginIp(adminUser.getIpAddress())
				.adminUser(adminUser).build();

		adminUser.addLoginHistory(adminLoginHistory);
		adminUser.setLastLogin(BigInteger.valueOf(Instant.now().getEpochSecond()));

		return Map.of("res", true, "data", adminUserRepo.save(adminUser));
	}

	@Transactional
	public Map<String, Object> adminLogin(String email, String password, String ip) {
		if (email == null || password == null || email.isEmpty() || password.isEmpty())
			throw new InternalServerException("Invalid email or password", HttpStatus.UNAUTHORIZED);

		AdminUser adminUser = adminUserRepo.findByEmail(email).orElseThrow(
				() -> new InternalServerException("Admin not found with this credentials", HttpStatus.NOT_FOUND));
		
		if(!(adminUser.getPassword().equals(password)))
			throw new InternalServerException("Incorrect Credentials", HttpStatus.UNAUTHORIZED);

		AdminLoginHistory adminLoginHistory = AdminLoginHistory.builder().loginIp(ip).adminUser(adminUser).build();

		adminUser.addLoginHistory(adminLoginHistory);
		adminUser.setLastLogin(BigInteger.valueOf(Instant.now().getEpochSecond()));
		return Map.of("res", true, "data", adminUserRepo.save(adminUser));
	}

	@Transactional
	public Map<String, Object> adminLogout(Integer admin_id) {
		if (admin_id == null || admin_id <= 0)
			throw new InternalServerException("Invalid admin id", HttpStatus.BAD_REQUEST);

		AdminUser adminUser = adminUserRepo.findById(admin_id)
				.orElseThrow(() -> new InternalServerException("Admin not found", HttpStatus.NOT_FOUND));

		AdminLoginHistory adminLoginHistory = adminUser.getLoginHistory().getLast();

		adminLoginHistory.setLogoutTime(BigInteger.valueOf(Instant.now().getEpochSecond()));

		adminLoginRepo.save(adminLoginHistory);

		Map<String, Object> response = new HashMap<String, Object>();

		response.put("res", true);

		return response;

	}

}
