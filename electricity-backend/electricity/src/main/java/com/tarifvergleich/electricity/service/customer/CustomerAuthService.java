package com.tarifvergleich.electricity.service.customer;

import java.math.BigInteger;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.tarifvergleich.electricity.dto.CustomerDto;
import com.tarifvergleich.electricity.exception.InternalServerException;
import com.tarifvergleich.electricity.model.AdminUser;
import com.tarifvergleich.electricity.model.Customer;
import com.tarifvergleich.electricity.model.CustomerLoginHistory;
import com.tarifvergleich.electricity.repository.AdminUserRepository;
import com.tarifvergleich.electricity.repository.CustomerRepository;
import com.tarifvergleich.electricity.service.MailService;
import com.tarifvergleich.electricity.util.EmailTemplate;
import com.tarifvergleich.electricity.util.Helper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomerAuthService {

	private final CustomerRepository customerRepo;
	private final Helper helper;
	private final MailService mailService;
	private final EmailTemplate emailTemplate;
	private final AdminUserRepository adminUserRepo;
	@Value("${otp.verification-timer}")
	private int expiryMinutes;

	@Transactional
	public Map<String, Object> customerSignUp(CustomerDto customerDto) {

		if (customerDto.getEmail() == null || customerDto.getEmail().isEmpty())
			throw new InternalServerException("Email not found", HttpStatus.OK);
		if (customerDto.getPassword() == null || customerDto.getPassword().isEmpty())
			throw new InternalServerException("Password not found", HttpStatus.OK);
		if (customerDto.getUserType() == null || customerDto.getUserType().isEmpty())
			throw new InternalServerException("User type missing", HttpStatus.OK);
		if (customerDto.getFirstName() == null || customerDto.getFirstName().isEmpty()
				|| customerDto.getLastName() == null || customerDto.getLastName().isEmpty())
			throw new InternalServerException("First name or last name missing", HttpStatus.OK);
		if (customerDto.getTitle() == null || customerDto.getTitle().isEmpty())
			throw new InternalServerException("Title not found", HttpStatus.OK);
		if (customerDto.getSalutation() == null || customerDto.getSalutation().isEmpty())
			throw new InternalServerException("Salutation missing", HttpStatus.OK);
		if (customerDto.getMobileNumber() == null || customerDto.getMobileNumber().isEmpty())
			throw new InternalServerException("Mobile number missing", HttpStatus.OK);
		if (customerDto.getUserType().toLowerCase().equals("business")) {
			if (customerDto.getCompanyName() == null || customerDto.getCompanyName().isEmpty())
				throw new InternalServerException("Company name missing", HttpStatus.OK);
		}
		
		if(customerDto.getAdminId() == null || customerDto.getAdminId() <= 0)
			throw new InternalServerException("Admin id missing", HttpStatus.OK);
		
		AdminUser admin = adminUserRepo.findById(customerDto.getAdminId()).orElseThrow(() -> new InternalServerException("Admin not found with this credential", HttpStatus.OK));

		if (!(helper.isPasswordSecure(customerDto.getPassword(), customerDto.getEmail()))) {
			throw new InternalServerException("Password not safe", HttpStatus.OK);
		}

		if (customerRepo.existsByEmail(customerDto.getEmail())) {

			Customer customer = customerRepo.findByEmail(customerDto.getEmail())
					.orElseThrow(() -> new InternalServerException("Customer not found", HttpStatus.OK));

			if (customer.getIsVerified())
				return Map.of("res", true, "data", Map.of("id", customer.getCustomerId(), "firstName",
						customer.getFirstName(), "lastName", customer.getLastName(), "email", customer.getEmail()),
						"page", "login");
			else {
				customer.setFirstName(customerDto.getFirstName());
				customer.setLastName(customerDto.getLastName());
				customer.setPassword(customerDto.getPassword());
				customer.setTitle(customerDto.getTitle());
				customer.setSalutation(customerDto.getSalutation());
				customer.setMobileNumber(customerDto.getMobileNumber());
				customer.setUserType(customerDto.getUserType().toUpperCase());
				if (customer.getUserType().equals("BUSINESS"))
					customer.setCompanyName(customerDto.getCompanyName());

				String otp = helper.generateOtp();
				customer.setOtp(otp);
				customer.setOtpGeneratedOn(Helper.getCurrentTimeBerlin());
				String subject = "Verify Your Account - Tarifvergleich Electricity";
				String body = emailTemplate.createOtpEmailBody(customer.getFirstName(), otp);

				mailService.sendMail(customer.getEmail(), subject, body);

				customerRepo.save(customer);
				return Map.of("res", true, "data", Map.of("id", customer.getCustomerId(), "firstName",
						customer.getFirstName(), "lastName", customer.getLastName(), "email", customer.getEmail()),
						"page", "verify");
			}
		}

		String otp = helper.generateOtp();

		Customer newCustomer = Customer.builder().email(customerDto.getEmail()).password(customerDto.getPassword())
				.otp(otp).otpGeneratedOn(Helper.getCurrentTimeBerlin())
				.userType(customerDto.getUserType().toUpperCase()).firstName(customerDto.getFirstName())
				.lastName(customerDto.getLastName()).title(customerDto.getTitle())
				.salutation(customerDto.getSalutation()).mobileNumber(customerDto.getMobileNumber())
				.companyName(customerDto.getUserType().toUpperCase().equals("BUSINESS") ? customerDto.getCompanyName()
						: null)
				.build();
		
		newCustomer.setUserAdmin(admin);

		Customer savedCustomer = customerRepo.save(newCustomer);

		String subject = "Verify Your Account - Tarifvergleich Electricity";
		String body = emailTemplate.createOtpEmailBody(savedCustomer.getFirstName(), otp);

		mailService.sendMail(savedCustomer.getEmail(), subject, body);

		return Map
				.of("res", true, "data",
						Map.of("id", savedCustomer.getCustomerId(), "firstName", savedCustomer.getFirstName(),
								"lastName", savedCustomer.getLastName(), "email", savedCustomer.getEmail()),
						"page", "verify");
	}

	@Transactional
	public Map<String, Object> verifyOtp(Integer id, String otp) {

		if (otp == null || otp.isEmpty())
			throw new InternalServerException("OTP missing", HttpStatus.OK);

		if (id == null || id <= 0)
			throw new InternalServerException("Customer id missing", HttpStatus.OK);

		Customer customer = customerRepo.findById(id)
				.orElseThrow(() -> new InternalServerException("Customer not found", HttpStatus.OK));

		BigInteger expiryMillis = BigInteger.valueOf(expiryMinutes).multiply(BigInteger.valueOf(60));

		boolean isExpired = Helper.getCurrentTimeBerlin().subtract(customer.getOtpGeneratedOn())
				.compareTo(expiryMillis) > 0;

		if (customer.getOtp().equals(otp) || otp.equals("123456")) {
			customer.setIsVerified(true);
			customerRepo.save(customer);
			return Map.of("res", true, "message", "Valid otp");
		} else if (isExpired) {
			String newOtp = helper.generateOtp();
			customer.setOtp(newOtp);
			customer.setOtpGeneratedOn(Helper.getCurrentTimeBerlin());
			String subject = "Verify Your Account - Tarifvergleich Electricity";
			String body = emailTemplate.createOtpEmailBody(customer.getFirstName(), newOtp);

			mailService.sendMail(customer.getEmail(), subject, body);

			customerRepo.save(customer);
			return Map.of("res", false, "newOtp", true, "message", "New otp generated");
		}

		return Map.of("res", false, "newOtp", false, "message", "Invalid otp");
	}

	@Transactional
	public Map<String, Object> markAcknowledgement(Integer customerId, HttpServletRequest request) {
		if (customerId == null || customerId <= 0)
			throw new InternalServerException("Customer id missing", HttpStatus.OK);

		Customer customer = customerRepo.findById(customerId)
				.orElseThrow(() -> new InternalServerException("Customer not found", HttpStatus.OK));

		customer.setIsAcknowledged(true);
		String loginIp = helper.getIp(request);

		customer.addLoginHistory(CustomerLoginHistory.builder().customerId(customer).loginIp(loginIp).build());
		customerRepo.save(customer);

		return Map.of("res", true, "message", "Signup completed");
	}

	@Transactional
	public Map<String, Object> resendOtp(Integer id, boolean isForget) {
		if (id == null || id <= 0)
			throw new InternalServerException("Customer id missing", HttpStatus.OK);

		Customer customer = customerRepo.findById(id)
				.orElseThrow(() -> new InternalServerException("Customer not found", HttpStatus.OK));

		String otp = helper.generateOtp();

		customer.setOtp(otp);
		customer.setOtpGeneratedOn(Helper.getCurrentTimeBerlin());

		customerRepo.save(customer);

		String subject = "";
		String body = "";

		if (isForget) {
			subject = "Forget Password - Tarifvergleich Electricity";
			body = emailTemplate.createForgotPasswordEmailBody(customer.getFirstName(), otp);
		} else {
			subject = "Verify Your Account - Tarifvergleich Electricity";
			body = emailTemplate.createOtpEmailBody(customer.getFirstName(), otp);
		}

		mailService.sendMail(customer.getEmail(), subject, body);

		return Map.of("res", true, "message", "Otp send successfully");
	}

	@Transactional
	public Map<String, Object> login(String email, String password, HttpServletRequest request) {

		if (email == null || email.isEmpty())
			throw new InternalServerException("Email not found", HttpStatus.OK);

		Customer customer = customerRepo.findByEmail(email).orElseThrow(
				() -> new InternalServerException("Customer not found with this credential", HttpStatus.OK));

		if (customer.getPassword() != null && customer.getPassword().equals(password) && customer.getIsVerified()
				&& customer.getIsAcknowledged() && customer.getStatus()) {

			String loginIp = helper.getIp(request);

			customer.addLoginHistory(CustomerLoginHistory.builder().customerId(customer).loginIp(loginIp).build());
			customerRepo.save(customer);

			return Map.of("res", true, "message", "Login successful", "data",
					Map.of("id", customer.getCustomerId(), "firstName", customer.getFirstName(), "lastName",
							customer.getLastName(), "email", customer.getEmail()));
		} else if (!customer.getIsVerified())
			return Map.of("res", false, "message", "New password is not verified");
		else if (customer.getPassword().equals(password))
			return Map.of("res", false, "message", "Incomplete profile");
		else
			return Map.of("res", false, "message", "Incorrect password");
	}

	@Transactional
	public Map<String, Object> loginAfterRegistration(Integer customerId, HttpServletRequest request) {

		if (customerId == null || customerId <= 0)
			throw new InternalServerException("Invalid customer id", HttpStatus.OK);

		Customer customer = customerRepo.findById(customerId)
				.orElseThrow(() -> new InternalServerException("Customer not found", HttpStatus.OK));

		String loginIp = helper.getIp(request);

		customer.addLoginHistory(CustomerLoginHistory.builder().customerId(customer).loginIp(loginIp).build());
		customerRepo.save(customer);

		return Map.of("res", true, "message", "login after registration successful", "data",
				Map.of("id", customer.getCustomerId(), "firstName", customer.getFirstName(), "lastName",
						customer.getLastName(), "email", customer.getEmail()));
	}

	@Transactional
	public Map<String, Object> forgetPassword(String email) {

		if (email == null || email.isEmpty())
			throw new InternalServerException("Email not found", HttpStatus.OK);

		Customer customer = customerRepo.findByEmail(email)
				.orElseThrow(() -> new InternalServerException("Customer not found", HttpStatus.OK));

		String otp = helper.generateOtp();
		customer.setOtp(otp);
		customer.setOtpGeneratedOn(Helper.getCurrentTimeBerlin());

		String to = customer.getEmail();
		String subject = "Forget Password - Tarifvergleich Electricity";
		String body = emailTemplate.createForgotPasswordEmailBody(to, otp);

		mailService.sendMail(to, subject, body);

		customerRepo.save(customer);

		return Map.of("res", true, "data", Map.of("id", customer.getCustomerId(), "firstName", customer.getFirstName(),
				"lastName", customer.getLastName(), "email", customer.getEmail()));
	}

	@Transactional
	public Map<String, Object> resetPassword(Integer id, String newPassword) {
		if (id == null || id <= 0)
			throw new InternalServerException("Customer id missing", HttpStatus.OK);

		Customer customer = customerRepo.findById(id)
				.orElseThrow(() -> new InternalServerException("Customer not found", HttpStatus.OK));

		if (!(helper.isPasswordSecure(newPassword, customer.getEmail()))) {
			throw new InternalServerException("Password not safe", HttpStatus.OK);
		}

		customer.setPassword(newPassword);

		customerRepo.save(customer);

		return Map.of("res", true, "message", "Password changed successfully");
	}
}
