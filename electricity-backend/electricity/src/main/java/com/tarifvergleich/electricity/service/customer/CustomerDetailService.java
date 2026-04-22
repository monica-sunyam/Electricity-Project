package com.tarifvergleich.electricity.service.customer;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.tarifvergleich.electricity.dto.CustomerAttornyDto;
import com.tarifvergleich.electricity.dto.CustomerDto;
import com.tarifvergleich.electricity.exception.InternalServerException;
import com.tarifvergleich.electricity.model.AdminUser;
import com.tarifvergleich.electricity.model.Customer;
import com.tarifvergleich.electricity.model.CustomerAttorny;
import com.tarifvergleich.electricity.repository.AdminUserRepository;
import com.tarifvergleich.electricity.repository.CustomerAttornyRepository;
import com.tarifvergleich.electricity.repository.CustomerRepository;
import com.tarifvergleich.electricity.util.FileServiceCustomer;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomerDetailService {

	private final CustomerRepository customerRepo;
	private final FileServiceCustomer fileServiceCustomer;
	private final AdminUserRepository adminUserRepo;
	private final CustomerAttornyRepository customerAttornyRepo;
	
	public Map<String, Object> getCustomerDetails(Integer customerId) {

		if (customerId == null || customerId <= 0)
			throw new InternalServerException("Customer id missing", HttpStatus.OK);

		Customer customer = customerRepo.findById(customerId).orElseThrow(
				() -> new InternalServerException("Customer missing with this credentials", HttpStatus.OK));

		return Map.of("res", true, "data", CustomerDto.getCustomerResponseDto(customer));
	}

	@Transactional
	public Map<String, Object> submitCustomerAttorny(CustomerAttornyDto attornyDto, MultipartFile file) {

		if (attornyDto.getAdminId() == null || attornyDto.getAdminId() <= 0)
			throw new InternalServerException("Admin missing", HttpStatus.OK);
		if (attornyDto.getCustomerId() == null || attornyDto.getCustomerId() <= 0)
			throw new InternalServerException("Customer missing", HttpStatus.OK);
		if (attornyDto.getSalutation() == null)
			throw new InternalServerException("Salutation missing", HttpStatus.OK);
		if (attornyDto.getTitle() == null)
			throw new InternalServerException("Title missing", HttpStatus.OK);
		if (attornyDto.getUserType() == null || (!attornyDto.getUserType().toLowerCase().equals("business")
				&& !attornyDto.getUserType().toLowerCase().equals("private")))
			throw new InternalServerException("User type missing", HttpStatus.OK);
		if (attornyDto.getFirstName() == null || attornyDto.getFirstName().trim().isEmpty())
			throw new InternalServerException("First name missing", HttpStatus.OK);
		if (attornyDto.getLastName() == null || attornyDto.getLastName().trim().isEmpty())
			throw new InternalServerException("Last name missing", HttpStatus.OK);
		if (attornyDto.getZip() == null || attornyDto.getZip().trim().isEmpty())
			throw new InternalServerException("Post code missing", HttpStatus.OK);
		if (attornyDto.getCity() == null || attornyDto.getCity().trim().isEmpty())
			throw new InternalServerException("City missing", HttpStatus.OK);
		if (attornyDto.getStreet() == null || attornyDto.getStreet().trim().isEmpty())
			throw new InternalServerException("Street missing", HttpStatus.OK);
		if (file == null)
			throw new InternalServerException("Signature missing", HttpStatus.OK);

		if (attornyDto.getUserType().toUpperCase().equals("BUSINESS")) {
			if (attornyDto.getCompanyName() == null || attornyDto.getCompanyName().trim().isEmpty())
				throw new InternalServerException("Business name missing", HttpStatus.OK);

			if (attornyDto.getLegalRepresentativeFirstName() == null
					|| attornyDto.getLegalRepresentativeFirstName().trim().isEmpty())
				throw new InternalServerException("Legal representative first name missing", HttpStatus.OK);

			if (attornyDto.getLegalRepresentativeLastName() == null
					|| attornyDto.getLegalRepresentativeLastName().trim().isEmpty())
				throw new InternalServerException("Legal representative last name missing", HttpStatus.OK);

		} else {
			if (attornyDto.getHouseNumber() == null || attornyDto.getHouseNumber().trim().isEmpty())
				throw new InternalServerException("House number missing", HttpStatus.OK);
		}

		AdminUser admin = adminUserRepo.findById(attornyDto.getAdminId())
				.orElseThrow(() -> new InternalServerException("Admin not found with this credential", HttpStatus.OK));
		Customer customer = customerRepo.findById(attornyDto.getCustomerId()).orElseThrow(
				() -> new InternalServerException("Customer not found with this credential", HttpStatus.OK));

		String filePath = fileServiceCustomer.saveFile(file, "customer-signature");

		CustomerAttorny attornyEntity;

		CustomerAttorny customerAttorny = Optional.ofNullable(customer.getCustomerAttorny())
				.orElse(Collections.emptyList()).stream()
				.filter(attorny -> !Boolean.TRUE.equals(attorny.getIsRevoked()) && !attorny.getApprovalStatus().equals(2)).reduce((first, second) -> second)
				.orElse(null);

		if (customerAttorny != null) {
			attornyEntity = customerAttorny;
		} else {
			attornyEntity = new CustomerAttorny();
			attornyEntity.setCustomerRef(customer);
			attornyEntity.setAdminRef(admin);
			attornyEntity.setIsRevoked(false);
		}

		attornyDto.setApprovalStatus(1);
		attornyEntity.setSalutation(attornyDto.getSalutation());
		attornyEntity.setTitle(attornyDto.getTitle());
		attornyEntity.setFirstName(attornyDto.getFirstName());
		attornyEntity.setLastName(attornyDto.getLastName());
		attornyEntity.setZip(attornyDto.getZip());
		attornyEntity.setCity(attornyDto.getCity());
		attornyEntity.setStreet(attornyDto.getStreet());
		attornyEntity.setCustomerSignaturePath(filePath);
		attornyEntity.setCustomerUniqueId(customer.getCustomerUniqueId());
		attornyEntity.setHouseNumber(attornyDto.getHouseNumber());
		attornyEntity.setUserType(attornyDto.getUserType().toUpperCase());

		if ("BUSINESS".equalsIgnoreCase(attornyDto.getUserType())) {
			attornyEntity.setCompanyName(attornyDto.getCompanyName());
			attornyEntity.setLegalRepresentativeFirstName(attornyDto.getLegalRepresentativeFirstName());
			attornyEntity.setLegalRepresentativeLastName(attornyDto.getLegalRepresentativeLastName());
		}
		
		customerAttornyRepo.save(attornyEntity);

		return Map.of("res", true, "createdOn", attornyEntity.getSubmittedOn());
	}
}
