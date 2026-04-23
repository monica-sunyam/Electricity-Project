package com.tarifvergleich.electricity.service.customer;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.tarifvergleich.electricity.dto.CustomerAttornyDto;
import com.tarifvergleich.electricity.dto.CustomerDeliveryResponseDto;
import com.tarifvergleich.electricity.dto.CustomerDeliveryResponseDto.CustomerDeliveryResponseAll;
import com.tarifvergleich.electricity.dto.CustomerDto;
import com.tarifvergleich.electricity.dto.CustomerDto.CustomerShortDetail;
import com.tarifvergleich.electricity.dto.CustomerServiceRequestDto;
import com.tarifvergleich.electricity.dto.CustomerServicesDto;
import com.tarifvergleich.electricity.dto.CustomerServicesDto.CustomerListOfServiceResDto;
import com.tarifvergleich.electricity.exception.InternalServerException;
import com.tarifvergleich.electricity.model.AdminUser;
import com.tarifvergleich.electricity.model.Customer;
import com.tarifvergleich.electricity.model.CustomerAttorny;
import com.tarifvergleich.electricity.model.CustomerDelivery;
import com.tarifvergleich.electricity.model.CustomerServiceRequest;
import com.tarifvergleich.electricity.model.CustomerServiceRequestMessages;
import com.tarifvergleich.electricity.model.CustomerServices;
import com.tarifvergleich.electricity.repository.AdminUserRepository;
import com.tarifvergleich.electricity.repository.CustomerAttornyRepository;
import com.tarifvergleich.electricity.repository.CustomerDeliveryRepository;
import com.tarifvergleich.electricity.repository.CustomerRepository;
import com.tarifvergleich.electricity.repository.CustomerServiceRequestRepository;
import com.tarifvergleich.electricity.repository.CustomerServicesRepository;
import com.tarifvergleich.electricity.service.MailService;
import com.tarifvergleich.electricity.util.FileServiceCustomer;
import com.tarifvergleich.electricity.util.Helper;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomerDetailService {

	private final CustomerRepository customerRepo;
	private final FileServiceCustomer fileServiceCustomer;
	private final AdminUserRepository adminUserRepo;
	private final CustomerAttornyRepository customerAttornyRepo;
	private final CustomerServicesRepository customerServicesRepo;
	private final CustomerDeliveryRepository customerDeliveryRepo;
	private final CustomerServiceRequestRepository customerServiceRequestRepo;
	private final MailService mailService;

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
				.orElse(Collections.emptyList()).stream().filter(attorny -> !Boolean.TRUE.equals(attorny.getIsRevoked())
						&& !attorny.getApprovalStatus().equals(2))
				.reduce((first, second) -> second).orElse(null);

		if (customerAttorny != null) {
			attornyEntity = customerAttorny;
		} else {
			attornyEntity = new CustomerAttorny();
			attornyEntity.setCustomerRef(customer);
			attornyEntity.setAdminRef(admin);
			attornyEntity.setIsRevoked(false);
		}

		attornyEntity.setApprovalStatus(1);
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

	public Map<String, Object> fetchAllCustomerDeliveries(Integer customerId) {

		if (customerId == null || customerId <= 0)
			throw new InternalServerException("Customer id missing", HttpStatus.OK);

		Customer customer = customerRepo.findById(customerId).orElseThrow(
				() -> new InternalServerException("Customer not found with this credentian", HttpStatus.OK));

		List<CustomerDelivery> customerDeliveries = customer.getCustomerDelivery();

		if (customerDeliveries == null || customerDeliveries.isEmpty())
			return Map.of("res", true, "customerDeliveries", List.of());

		List<CustomerDeliveryResponseAll> deliveryResponse = customerDeliveries.stream()
				.filter(delivery -> delivery.getOrderPlaced()).map(CustomerDeliveryResponseDto::getDeliveryResponse)
				.sorted((a, b) -> b.getOrderPlacedOn().compareTo(a.getOrderPlacedOn())).toList();
		CustomerShortDetail customerResponse = CustomerDto.customerShortResponse(customer);

		return Map.of("res", true, "customer", customerResponse, "delivery", deliveryResponse);
	}

	public Map<String, Object> fetchCustomerServices(Integer adminId, String serviceType) {

		if (adminId == null || adminId <= 0)
			throw new InternalServerException("Admin id missing", HttpStatus.OK);
		if (serviceType != null && !serviceType.equalsIgnoreCase("Delivery")
				&& !serviceType.equalsIgnoreCase("General"))
			throw new InternalServerException("Invalid service type", HttpStatus.OK);

		List<CustomerServices> services = customerServicesRepo.findAllByAdminAdminIdAndStatus(adminId, true);
		List<CustomerListOfServiceResDto> servicesResponse = null;

		if (services == null || services.isEmpty())
			return Map.of("res", true, "data", List.of());

		if (serviceType == null || serviceType.equalsIgnoreCase("general")) {
			servicesResponse = services.stream().filter(
					service -> service.getServiceType().equals("ALL") || service.getServiceType().equals("GENERAL"))
					.map(CustomerServicesDto::mapCustomerService).toList();
		} else {
			servicesResponse = services.stream().filter(
					service -> service.getServiceType().equals("ALL") || service.getServiceType().equals("DELIVERY"))
					.map(CustomerServicesDto::mapCustomerService).toList();
		}

		return Map.of("res", true, "data", servicesResponse);
	}

	@Transactional
	public Map<String, Object> addRequestAndMessage(CustomerServiceRequestDto serviceRequestDto) {

		if (serviceRequestDto.getMessage() == null || serviceRequestDto.getMessage().isEmpty())
			throw new InternalServerException("Service message missing", HttpStatus.OK);
		if (serviceRequestDto.getCustomerId() == null || serviceRequestDto.getCustomerId() <= 0)
			throw new InternalServerException("Customer id missing", HttpStatus.OK);
		if (serviceRequestDto.getServiceId() == null || serviceRequestDto.getServiceId() <= 0)
			throw new InternalServerException("Service id missing", HttpStatus.OK);
		
		boolean isReopened = false;
		boolean isNewMessage = false;
		
		Customer customer = customerRepo.findById(serviceRequestDto.getCustomerId())
				.orElseThrow(() -> new InternalServerException("Customer not found", HttpStatus.OK));
		
		String customerMailId = customer.getEmail();
		String adminMailId = customer.getAdmin().getEmail();

		CustomerServiceRequest customerServiceRequest = null;

		if (serviceRequestDto.getServiceRequestId() == null || serviceRequestDto.getServiceRequestId() <= 0) {

			if (serviceRequestDto.getTitle() == null || serviceRequestDto.getTitle().isEmpty())
				throw new InternalServerException("Title missing", HttpStatus.OK);

			if (serviceRequestDto.getServiceRequestType() == null
					|| (!serviceRequestDto.getServiceRequestType().equalsIgnoreCase("DELIVERY")
							&& !serviceRequestDto.getServiceRequestType().equalsIgnoreCase("GENERAL")))
				throw new InternalServerException("Customer service request type undefined", HttpStatus.OK);

			CustomerServices service = customerServicesRepo.findById(serviceRequestDto.getServiceId())
					.orElseThrow(() -> new InternalServerException("Invalid customer service id", HttpStatus.OK));

			CustomerDelivery delivery = null;

			if (serviceRequestDto.getServiceRequestType().equalsIgnoreCase("DELIVERY")) {

				if (serviceRequestDto.getDeliveryId() == null || serviceRequestDto.getDeliveryId() <= 0)
					throw new InternalServerException("Delivery id missing", HttpStatus.OK);

				delivery = customerDeliveryRepo.findById(serviceRequestDto.getDeliveryId())
						.orElseThrow(() -> new InternalServerException("Customer delivery not found", HttpStatus.OK));
			}

			customerServiceRequest = CustomerServiceRequest.builder().service(service).admin(service.getAdmin())
					.customerDelivery(delivery).customer(customer).title(serviceRequestDto.getTitle())
					.description(serviceRequestDto.getMessage())
					.serviceRequestType(serviceRequestDto.getServiceRequestType().toUpperCase()).build();
		}

		else {

			customerServiceRequest = customerServiceRequestRepo.findById(serviceRequestDto.getServiceRequestId())
					.orElseThrow(
							() -> new InternalServerException("Customer service request not found", HttpStatus.OK));

			if (customerServiceRequest.getIsClosed()) {
				isReopened = true;
				customerServiceRequest.setRequestReopenedOn(Helper.getCurrentTimeBerlin());
				customerServiceRequest.setIsClosed(false);
				customerServiceRequest.setIsOpen(true);
			}
			else {
				isNewMessage = true;
			}
		}

		CustomerServiceRequestMessages message = CustomerServiceRequestMessages.builder()
				.message(serviceRequestDto.getMessage()).chatUser("CUSTOMER").build();

		customerServiceRequest.addCustomerServiceRequestMessage(message);

		customerServiceRequest = customerServiceRequestRepo.save(customerServiceRequest);
		
		if(isReopened) {
			// Add the mail logic based on reopened both for customer and admin.
			
		}
		else if(isNewMessage) {
			
			
			
		} else {
			
		}

		return Map.of("res", true, "message", "Service request message delivered successfully");
	}

}
