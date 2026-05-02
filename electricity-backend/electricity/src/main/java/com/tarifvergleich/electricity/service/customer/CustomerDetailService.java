package com.tarifvergleich.electricity.service.customer;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.tarifvergleich.electricity.dto.CustomerAddressDto;
import com.tarifvergleich.electricity.dto.CustomerAttornyDto;
import com.tarifvergleich.electricity.dto.CustomerDeliveryResponseDto;
import com.tarifvergleich.electricity.dto.CustomerDeliveryResponseDto.CustomerDeliveryResponseAll;
import com.tarifvergleich.electricity.dto.CustomerDto;
import com.tarifvergleich.electricity.dto.CustomerDto.CustomerShortDetail;
import com.tarifvergleich.electricity.dto.CustomerServiceRequestDto;
import com.tarifvergleich.electricity.dto.CustomerServiceRequestDto.CustomerServiceRequestResDtoForListing;
import com.tarifvergleich.electricity.dto.CustomerServiceRequestDto.CustomerServiceRequestResDtoForMessages;
import com.tarifvergleich.electricity.dto.CustomerServicesDto;
import com.tarifvergleich.electricity.dto.CustomerServicesDto.CustomerListOfServiceResDto;
import com.tarifvergleich.electricity.dto.ServiceRequestEmailEvent;
import com.tarifvergleich.electricity.exception.InternalServerException;
import com.tarifvergleich.electricity.model.AdminUser;
import com.tarifvergleich.electricity.model.Customer;
import com.tarifvergleich.electricity.model.CustomerAddress;
import com.tarifvergleich.electricity.model.CustomerAttorny;
import com.tarifvergleich.electricity.model.CustomerDelivery;
import com.tarifvergleich.electricity.model.CustomerServiceRequest;
import com.tarifvergleich.electricity.model.CustomerServiceRequestMessages;
import com.tarifvergleich.electricity.model.CustomerServices;
import com.tarifvergleich.electricity.repository.AdminUserRepository;
import com.tarifvergleich.electricity.repository.CustomerAddressRepository;
import com.tarifvergleich.electricity.repository.CustomerAttornyRepository;
import com.tarifvergleich.electricity.repository.CustomerDeliveryRepository;
import com.tarifvergleich.electricity.repository.CustomerRepository;
import com.tarifvergleich.electricity.repository.CustomerServiceRequestRepository;
import com.tarifvergleich.electricity.repository.CustomerServicesRepository;
import com.tarifvergleich.electricity.util.EmailTemplate;
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
	private final CustomerAddressRepository customerAddressRepo;
	private final CustomerServiceRequestRepository customerServiceRequestRepo;
	private final EmailTemplate emailTemplate;
	private final ApplicationEventPublisher eventPublisher;

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
		if (attornyDto.getPlaceAndDate() == null || attornyDto.getPlaceAndDate().isEmpty())
			throw new InternalServerException("Place and date missing", HttpStatus.OK);
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

		attornyEntity.setApprovalStatus(0);
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
		attornyEntity.setPlaceAndDate(attornyDto.getPlaceAndDate());

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

	public Map<String, Object> fetchAllCustomerDeliveriesByGroup(Integer adminId, Integer customerId) {

		if (adminId == null || adminId <= 0)
			throw new InternalServerException("Admin id missing", HttpStatus.OK);
		if (customerId == null || customerId <= 0)
			throw new InternalServerException("Customer id Missing", HttpStatus.OK);

		List<CustomerDelivery> customerDeliveries = customerDeliveryRepo
				.findAllByAdminAdminIdAndCustomerIdCustomerIdAndOrderPlacedOrderByOrderPlacedOnDesc(adminId, customerId,
						true);

		Map<String, List<CustomerDeliveryResponseAll>> deliveryResponse = customerDeliveries.stream()
				.map(CustomerDeliveryResponseDto::getDeliveryResponse).collect(Collectors.groupingBy(deliver -> {
					return deliver.getCustomerAddress().getZip() + " " + deliver.getCustomerAddress().getCity() + " "
							+ deliver.getCustomerAddress().getStreet();
				}));

		return Map.of("res", true, "data", deliveryResponse);
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

			if (serviceRequestDto.getServiceId() == null || serviceRequestDto.getServiceId() <= 0)
				throw new InternalServerException("Service id missing", HttpStatus.OK);

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

				BigInteger oneMonth = BigInteger.valueOf(30).multiply(BigInteger.valueOf(24))
						.multiply(BigInteger.valueOf(60)).multiply(BigInteger.valueOf(60));

				if (Helper.getCurrentTimeBerlin().subtract(customerServiceRequest.getRequestClosedOn())
						.compareTo(oneMonth) > 0)
					throw new InternalServerException("Ticket validity over", HttpStatus.OK);

				isReopened = true;
				customerServiceRequest.setRequestReopenedOn(Helper.getCurrentTimeBerlin());
				customerServiceRequest.setIsClosed(false);
				customerServiceRequest.setIsOpen(true);
			} else {
				isNewMessage = true;
			}
		}

		CustomerServiceRequestMessages message = CustomerServiceRequestMessages.builder()
				.message(serviceRequestDto.getMessage()).chatUser("CUSTOMER").build();

		customerServiceRequest.addCustomerServiceRequestMessage(message);

		customerServiceRequest = customerServiceRequestRepo.save(customerServiceRequest);
		AdminUser admin = customer.getAdmin();

		String customerBody = "";
		String adminBody = "";
		String customerSubject = "";
		String adminSubject = "";
		Map<String, Object> dateTimeMap = Helper.getLocalDateTimeFromBigInteger(customerServiceRequest.getCreatedOn());

		String formattedDateTime = dateTimeMap.get("monthName").toString() + " " + dateTimeMap.get("date").toString()
				+ " " + dateTimeMap.get("year").toString() + ", at " + dateTimeMap.get("hour").toString() + ":"
				+ dateTimeMap.get("minute").toString() + " " + dateTimeMap.get("amPm").toString();

		if (isReopened) {
			customerSubject = "Ticket " + customerServiceRequest.getTicketNumber() + " Reopened";
			customerBody = emailTemplate.createServiceRequestReopenedEmailBody(customer.getSalutation(),
					customer.getLastName(), customer.getFirstName(), customerServiceRequest.getTicketNumber(),
					formattedDateTime, customerServiceRequest.getService().getServiceName(), customer.getEmail(),
					serviceRequestDto.getMessage());

			adminSubject = "REOPENED: Ticket " + customerServiceRequest.getTicketNumber();
			adminBody = emailTemplate.createAdminServiceRequestReopenedEmailBody(admin.getName(),
					customer.getFirstName() + " " + customer.getLastName(), customerServiceRequest.getTicketNumber(),
					serviceRequestDto.getMessage());

		} else if (isNewMessage) {
			customerSubject = "Ticket " + customerServiceRequest.getTicketNumber() + " Added New Message";
			customerBody = emailTemplate.createNewMessageNotificationToCustomerBody(customer.getSalutation(),
					customer.getLastName(), customer.getFirstName(), customerServiceRequest.getTicketNumber(),
					customerServiceRequest.getService().getServiceName(), formattedDateTime,
					serviceRequestDto.getMessage());

			adminSubject = "New Message: Ticket " + customerServiceRequest.getTicketNumber();
			adminBody = emailTemplate.createAdminNewMessageNotificationBody(admin.getName(),
					customer.getFirstName() + " " + customer.getLastName(), customerServiceRequest.getTicketNumber(),
					serviceRequestDto.getMessage());

		} else {
			customerSubject = "New Ticket " + customerServiceRequest.getTicketNumber() + " Opened";
			customerBody = emailTemplate.createServiceRequestOpenedEmailBody(customer.getSalutation(),
					customer.getLastName(), customer.getFirstName(), customerServiceRequest.getTicketNumber(),
					customerServiceRequest.getService().getServiceName(), customerMailId, formattedDateTime,
					serviceRequestDto.getMessage());

			adminSubject = "ACTION REQUIRED: New Ticket " + customerServiceRequest.getTicketNumber();
			adminBody = emailTemplate.createAdminServiceRequestOpenedEmailBody(admin.getName(),
					customer.getFirstName() + " " + customer.getLastName(), customerServiceRequest.getTicketNumber(),
					customerServiceRequest.getService().getServiceName(), serviceRequestDto.getMessage());
		}

		ServiceRequestEmailEvent emailEvent = new ServiceRequestEmailEvent(customerMailId, customerSubject,
				customerBody, adminMailId, adminSubject, adminBody);

		eventPublisher.publishEvent(emailEvent);

		return Map.of("res", true, "message", "Service request message delivered successfully", "TicketNumber",
				customerServiceRequest.getTicketNumber(), "serviceRequestId", customerServiceRequest.getId(),
				"messageBody", serviceRequestDto.getMessage(), "sendOn", Helper.getCurrentTimeBerlin());
	}

	public Map<String, Object> getAllMessages(Integer customerServiceRequestId) {

		if (customerServiceRequestId == null || customerServiceRequestId <= 0)
			throw new InternalServerException("Customer service request id missing", HttpStatus.OK);

		CustomerServiceRequest customerServiceRequest = customerServiceRequestRepo.findById(customerServiceRequestId)
				.orElseThrow(() -> new InternalServerException("CustomerServiceRequest not found with this credential",
						HttpStatus.OK));

		CustomerServiceRequestResDtoForMessages messagesRes = CustomerServiceRequestDto
				.getAllMessagesRes(customerServiceRequest);

		return Map.of("res", true, "data", messagesRes);
	}

	public Map<String, Object> getCountOfRequestInDifferentTabs(Integer customerId) {

		if (customerId == null || customerId <= 0)
			throw new InternalServerException("Customer id missing", HttpStatus.OK);

		Long getTotalOpen = customerServiceRequestRepo.countByIsOpenAndCustomerCustomerId(true, customerId);
		Long getTotalInProgress = customerServiceRequestRepo.countByInProgressAndCustomerCustomerId(true, customerId);
		Long getTotalClosed = customerServiceRequestRepo.countByIsClosedAndCustomerCustomerId(true, customerId);

		return Map.of("res", true, "open", getTotalOpen, "progress", getTotalInProgress, "closed", getTotalClosed);
	}

	public Map<String, Object> fetchAllCustomerServiceRequest(Integer customerId) {

		if (customerId == null || customerId <= 0)
			throw new InternalServerException("Customer id missing", HttpStatus.OK);

		List<CustomerServiceRequest> serviceRequests = customerServiceRequestRepo
				.findAllByCustomerCustomerIdOrderByCreatedOnDesc(customerId);

		Map<String, List<CustomerServiceRequestResDtoForListing>> grouped = serviceRequests.stream()
				.map(CustomerServiceRequestDto::getAllListings).collect(Collectors.groupingBy(dto -> {
					if (dto.getIsOpen())
						return "open";
					if (dto.getInProgress())
						return "progress";
					return "closed";
				}));

		return Map.of("res", true, "openRequests", grouped.getOrDefault("open", List.of()), "inProgressRequets",
				grouped.getOrDefault("progress", List.of()), "closedRequests",
				grouped.getOrDefault("closed", List.of()));
	}

	public Map<String, Object> checkAttornyStatus(Integer customerId) {

		if (customerId == null || customerId <= 0)
			throw new InternalServerException("Customer id missing", HttpStatus.OK);

		String approvalStatus = "";
		boolean recordIsPresent = false;

		List<CustomerAttorny> attornies = customerAttornyRepo
				.findAllByCustomerCustomerIdAndIsRevokedOrderBySubmittedOnDesc(customerId, false);

		if (attornies == null || attornies.isEmpty())
			return Map.of("res", true, "recordIsPresent", recordIsPresent, "approvalStatus", "Not found");

		CustomerAttorny attorny = attornies.getFirst();
		recordIsPresent = true;

		if (attorny.getApprovalStatus().equals(0))
			approvalStatus = "pending".toUpperCase();
		if (attorny.getApprovalStatus().equals(1))
			approvalStatus = "approved".toUpperCase();
		if (attorny.getApprovalStatus().equals(2))
			approvalStatus = "rejected".toUpperCase();

		return Map.of("res", true, "recordIsPresent", recordIsPresent, "approvalStatus", approvalStatus, "createdOn",
				attorny.getSubmittedOn());
	}

	@Transactional
	public Map<String, Object> revokeAttorny(Integer customerId) {

		if (customerId == null || customerId <= 0)
			throw new InternalServerException("Customer id missing", HttpStatus.OK);

		List<CustomerAttorny> attornies = customerAttornyRepo
				.findAllByCustomerCustomerIdAndIsRevokedOrderBySubmittedOnDesc(customerId, false);

		if (attornies == null || attornies.isEmpty())
			throw new InternalServerException("No record for attorny found", HttpStatus.OK);

		CustomerAttorny attorny = attornies.getFirst();

		attorny.setIsRevoked(true);
		attorny.setRevokedOn(Helper.getCurrentTimeBerlin());

		customerAttornyRepo.save(attorny);

		return Map.of("res", true, "message", "Attorny successfully revoked");
	}

	public Map<String, Object> checkForBookings(CustomerAddressDto customerAddressDto) {
		if (customerAddressDto.getCustomerId() == null || customerAddressDto.getCustomerId() <= 0)
			throw new InternalServerException("Customer id missing", HttpStatus.OK);
		if (customerAddressDto.getZip() == null || customerAddressDto.getCity() == null
				|| customerAddressDto.getStreet() == null || customerAddressDto.getZip().isEmpty()
				|| customerAddressDto.getCity().isEmpty() || customerAddressDto.getStreet().isEmpty())
			throw new InternalServerException("Invalid address", HttpStatus.OK);

		if (customerAddressDto.getDeliveryType() == null || customerAddressDto.getDeliveryType().isEmpty()
				|| (!customerAddressDto.getDeliveryType().equalsIgnoreCase("Electricity")
						&& !customerAddressDto.getDeliveryType().equalsIgnoreCase("GS")))
			throw new InternalServerException("Delivery type missing", HttpStatus.OK);

		List<CustomerAddress> customerAddresses = customerAddressRepo
				.findAllByCustomerIdCustomerIdAndZipAndCityAndStreetAndHouseNumber(customerAddressDto.getCustomerId(),
						customerAddressDto.getZip(), customerAddressDto.getCity(), customerAddressDto.getStreet(),
						customerAddressDto.getHouseNumber());

		if (customerAddresses == null || customerAddresses.isEmpty())
			return Map.of("res", true, "message", "No booking found with this address");

		List<Integer> addressIds = customerAddresses.stream().filter(addr -> addr != null).map(CustomerAddress::getId)
				.toList();

		List<CustomerDelivery> deliveries = customerDeliveryRepo
				.findAllByCustomerIdCustomerIdAndIsExpiredAndIsCancelledAndDeliveryTypeAndOrderPlacedAndAddressIdIn(
						customerAddressDto.getCustomerId(), false, false,
						customerAddressDto.getDeliveryType().toUpperCase(), true, addressIds);

		if (deliveries == null || deliveries.isEmpty())
			return Map.of("res", true, "message", "No booking found with this address");

		return Map.of("res", false, "message", "Active booking exits with this address");
	}

}
