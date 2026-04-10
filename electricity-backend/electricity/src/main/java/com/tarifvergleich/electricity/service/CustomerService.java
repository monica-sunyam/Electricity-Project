package com.tarifvergleich.electricity.service;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.tarifvergleich.electricity.dto.CustomerDto;
import com.tarifvergleich.electricity.exception.InternalServerException;
import com.tarifvergleich.electricity.model.Customer;
import com.tarifvergleich.electricity.model.CustomerLoginHistory;
import com.tarifvergleich.electricity.repository.CustomerRepository;
import com.tarifvergleich.electricity.util.EmailTemplate;
import com.tarifvergleich.electricity.util.Helper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomerService {

	private final CustomerRepository customerRepo;
	private final Helper helper;
	private final MailService mailService;
	private final EmailTemplate emailTemplate;

	@Transactional
	public Map<String, Object> customerSignUp(CustomerDto customerDto) {

		if (customerDto.getEmail() == null || customerDto.getEmail().isEmpty())
			throw new InternalServerException("Email not found", HttpStatus.BAD_REQUEST);
		if (customerDto.getPassword() == null || customerDto.getPassword().isEmpty())
			throw new InternalServerException("Password not found", HttpStatus.BAD_REQUEST);
		if (customerDto.getUserType() == null || customerDto.getUserType().isEmpty())
			throw new InternalServerException("User type missing", HttpStatus.BAD_REQUEST);
		if (customerDto.getFirstName() == null || customerDto.getFirstName().isEmpty()
				|| customerDto.getLastName() == null || customerDto.getLastName().isEmpty())
			throw new InternalServerException("First name or last name missing", HttpStatus.BAD_REQUEST);
		if (customerDto.getTitle() == null || customerDto.getTitle().isEmpty())
			throw new InternalServerException("Title not found", HttpStatus.BAD_REQUEST);
		if (customerDto.getSalutation() == null || customerDto.getSalutation().isEmpty())
			throw new InternalServerException("Salutation missing", HttpStatus.BAD_REQUEST);
		if (customerDto.getMobileNumber() == null || customerDto.getMobileNumber().isEmpty())
			throw new InternalServerException("Mobile number missing", HttpStatus.BAD_REQUEST);
		if (customerDto.getUserType().toLowerCase().equals("business")) {
			if (customerDto.getCompanyName() == null || customerDto.getCompanyName().isEmpty())
				throw new InternalServerException("Company name missing", HttpStatus.BAD_REQUEST);
		}
		
		if(!(helper.isPasswordSecure(customerDto.getPassword(), customerDto.getEmail()))) {
			throw new InternalServerException("Password not safe", HttpStatus.BAD_REQUEST);
		}

		if (customerRepo.existsByEmail(customerDto.getEmail())) {

			Customer customer = customerRepo.findByEmail(customerDto.getEmail())
					.orElseThrow(() -> new InternalServerException("Customer not found", HttpStatus.BAD_REQUEST));

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
				.otp(otp).userType(customerDto.getUserType().toUpperCase()).firstName(customerDto.getFirstName())
				.lastName(customerDto.getLastName()).title(customerDto.getTitle())
				.salutation(customerDto.getSalutation()).mobileNumber(customerDto.getMobileNumber())
				.companyName(customerDto.getUserType().toUpperCase().equals("BUSINESS") ? customerDto.getCompanyName() : null).build();

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
			throw new InternalServerException("OTP missing", HttpStatus.BAD_REQUEST);

		if (id == null || id <= 0)
			throw new InternalServerException("Customer id missing", HttpStatus.BAD_REQUEST);

		Customer customer = customerRepo.findById(id)
				.orElseThrow(() -> new InternalServerException("Customer not found", HttpStatus.BAD_REQUEST));

		if (customer.getOtp().equals(otp)) {
			customer.setIsVerified(true);
			customerRepo.save(customer);
			return Map.of("res", true, "message", "Valid otp");
		}

		return Map.of("res", false, "message", "Invalid otp");
	}

	@Transactional
	public Map<String, Object> markAcknowledgement(Integer customerId, HttpServletRequest request) {
		if (customerId == null || customerId <= 0)
			throw new InternalServerException("Customer id missing", HttpStatus.BAD_REQUEST);

		Customer customer = customerRepo.findById(customerId)
				.orElseThrow(() -> new InternalServerException("Customer not found", HttpStatus.BAD_REQUEST));

		customer.setIsAcknowledged(true);
		String loginIp = helper.getIp(request);

		customer.addLoginHistory(CustomerLoginHistory.builder().customerId(customer).loginIp(loginIp).build());
		customerRepo.save(customer);

		return Map.of("res", true, "message", "Signup completed");
	}

	@Transactional
	public Map<String, Object> resendOtp(Integer id, boolean isForget) {
		if (id == null || id <= 0)
			throw new InternalServerException("Customer id missing", HttpStatus.BAD_REQUEST);

		Customer customer = customerRepo.findById(id)
				.orElseThrow(() -> new InternalServerException("Customer not found", HttpStatus.BAD_REQUEST));

		String otp = helper.generateOtp();

		customer.setOtp(otp);

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
			throw new InternalServerException("Email not found", HttpStatus.BAD_REQUEST);

		Customer customer = customerRepo.findByEmail(email).orElseThrow(
				() -> new InternalServerException("Customer not found with this credential", HttpStatus.BAD_REQUEST));

		if (customer.getPassword() != null && customer.getPassword().equals(password) && customer.getIsVerified() && customer.getIsAcknowledged()
				&& customer.getStatus()) {

			String loginIp = helper.getIp(request);

			customer.addLoginHistory(CustomerLoginHistory.builder().customerId(customer).loginIp(loginIp).build());
			customerRepo.save(customer);

			return Map.of("res", true, "message", "Login successful");
		}
		else if(customer.getPassword() == null)
			return Map.of("res", false, "message", "New password is not set");
		else if (customer.getPassword().equals(password))
			return Map.of("res", false, "message", "Incomplete profile");
		else
			return Map.of("res", false, "message", "Incorrect password");
	}

	public Map<String, Object> fetchCustomer(Integer id) {
		if (id == null || id <= 0)
			throw new InternalServerException("Customer id missing", HttpStatus.BAD_REQUEST);

		Customer customer = customerRepo.findById(id)
				.orElseThrow(() -> new InternalServerException("Customer not found", HttpStatus.BAD_REQUEST));

		return Map.of("res", true, "data", customer);
	}

	@Transactional
	public Map<String, Object> forgetPassword(String email) {

		if (email == null || email.isEmpty())
			throw new InternalServerException("Email not found", HttpStatus.BAD_REQUEST);

		Customer customer = customerRepo.findByEmail(email)
				.orElseThrow(() -> new InternalServerException("Customer not found", HttpStatus.BAD_REQUEST));

		String otp = helper.generateOtp();
		customer.setOtp(otp);
		customer.setPassword(null);

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
			throw new InternalServerException("Customer id missing", HttpStatus.BAD_REQUEST);

		Customer customer = customerRepo.findById(id)
				.orElseThrow(() -> new InternalServerException("Customer not found", HttpStatus.BAD_REQUEST));
		
		if(!(helper.isPasswordSecure(newPassword, customer.getEmail()))) {
			throw new InternalServerException("Password not safe", HttpStatus.BAD_REQUEST);
		}
		
		if(customer.getPassword() != null)
			return Map.of("res", false, "message", "Forget password to set new password");

		customer.setPassword(newPassword);

		customerRepo.save(customer);

		return Map.of("res", true, "message", "Password changed successfully");
	}
}
