package com.tarifvergleich.electricity.service.admin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarifvergleich.electricity.dto.CustomerAttornyDto;
import com.tarifvergleich.electricity.dto.CustomerComparingEnergyDto;
import com.tarifvergleich.electricity.dto.CustomerDeliveryDto;
import com.tarifvergleich.electricity.dto.CustomerDeliveryResponseDto;
import com.tarifvergleich.electricity.dto.CustomerDeliveryResponseDto.CustomerDeliveryResponseAll;
import com.tarifvergleich.electricity.dto.CustomerDto;
import com.tarifvergleich.electricity.dto.CustomerDto.AdminCustomerResponse;
import com.tarifvergleich.electricity.dto.CustomerDto.SingleCustomerResponseDelivery;
import com.tarifvergleich.electricity.dto.CustomerNoteDto;
import com.tarifvergleich.electricity.dto.CustomerServiceRequestDto;
import com.tarifvergleich.electricity.dto.CustomerServiceRequestDto.CustomerServiceRequestResDtoForAdmin;
import com.tarifvergleich.electricity.dto.CustomerServiceRequestDto.CustomerServiceRequestResDtoForListing;
import com.tarifvergleich.electricity.dto.ServiceRequestEmailEvent.ServiceResponseEmailEvent;
import com.tarifvergleich.electricity.exception.InternalServerException;
import com.tarifvergleich.electricity.model.AdminUser;
import com.tarifvergleich.electricity.model.Customer;
import com.tarifvergleich.electricity.model.CustomerAttorny;
import com.tarifvergleich.electricity.model.CustomerComparingEnergy;
import com.tarifvergleich.electricity.model.CustomerDelivery;
import com.tarifvergleich.electricity.model.CustomerNote;
import com.tarifvergleich.electricity.model.CustomerServiceRequest;
import com.tarifvergleich.electricity.model.CustomerServiceRequestMessages;
import com.tarifvergleich.electricity.repository.CustomerAttornyRepository;
import com.tarifvergleich.electricity.repository.CustomerComparingEnergyRepository;
import com.tarifvergleich.electricity.repository.CustomerDeliveryRepository;
import com.tarifvergleich.electricity.repository.CustomerRepository;
import com.tarifvergleich.electricity.repository.CustomerServiceRequestRepository;
import com.tarifvergleich.electricity.service.customer.CustomerAuthService;
import com.tarifvergleich.electricity.util.EmailTemplate;
import com.tarifvergleich.electricity.util.Helper;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminCustomerManagementService {

	private final CustomerRepository customerRepo;
	private final CustomerDeliveryRepository customerDeliveryRepo;
	private final CustomerComparingEnergyRepository customerComparingEnergyRepo;
	private final CustomerServiceRequestRepository customerServiceRequestRepo;
	private final CustomerAttornyRepository customerAttornyRepo;
	private final ApplicationEventPublisher eventPublisher;
	private final EmailTemplate emailTemplate;
	private final CustomerAuthService customerAuthService;
	private final ObjectMapper objectMapper;

	public Map<String, Object> getCustomers(CustomerDto customerReq) {

		if (customerReq.getAdminId() == null || customerReq.getAdminId() <= 0)
			throw new InternalServerException("Admin id missing", HttpStatus.OK);

		if (customerReq.getId() != null && customerReq.getId() > 0) {
			Customer customer = customerRepo.findById(customerReq.getId()).orElseThrow(
					() -> new InternalServerException("Customer not found with this credential", HttpStatus.OK));

			if (customer.getAdmin().getAdminId() != customerReq.getAdminId())
				throw new InternalServerException("Not authorised to access customer details", HttpStatus.OK);

			SingleCustomerResponseDelivery customerRes = CustomerDto.getCustomerResponseDto(customer);

			return Map.of("res", true, "data", customerRes);

		} else if (customerReq.getPage() != null) {

			if (customerReq.getSize() == null)
				customerReq.setSize(20);

			Pageable pageable = PageRequest.of(customerReq.getPage() - 1, customerReq.getSize(),
					Sort.by("joinedOn").descending());

			Page<Customer> customers = null;

			String search = customerReq.getSearch();
			String userType = customerReq.getUserType();
			Boolean isVerified = customerReq.getIsVerified();
			Integer adminId = customerReq.getAdminId();

			if (search != null && userType != null && !userType.isEmpty() && isVerified != null)
				customers = customerRepo.searchByUserTypeAndVerifiedAndTerms(adminId, search.trim(),
						userType.trim().toUpperCase(), isVerified, pageable);
			else if (search != null && userType != null && !userType.isEmpty())
				customers = customerRepo.searchByUserTypeAndTerms(adminId, search.trim(), userType.trim().toUpperCase(),
						pageable);
			else if (search != null && isVerified != null)
				customers = customerRepo.searchVerifiedCustomers(adminId, search.trim(), isVerified, pageable);
			else if (search != null)
				customers = customerRepo.searchByAdminAndTerm(adminId, search.trim(), pageable);
			else
				customers = customerRepo.findAllByAdminAdminId(customerReq.getAdminId(), pageable);

			Page<AdminCustomerResponse> customerRes = customers.map(CustomerDto::getCustomerDtoResponseForAdmin);

			return Map.of("res", true, "data", customerRes.getContent(), "page",
					customerRes.getPageable().getPageNumber() + 1, "totalPage", customerRes.getTotalPages());

		}

		List<Customer> customers = customerRepo.findAllByAdminAdminIdOrderByJoinedOnDesc(customerReq.getAdminId());

		List<AdminCustomerResponse> customerRes = customers.stream().map(CustomerDto::getCustomerDtoResponseForAdmin)
				.toList();

		return Map.of("res", true, "data", customerRes);

	}

	public Map<String, Object> getAllDeliveries(CustomerDeliveryDto deliveryReq) {

		if (deliveryReq.getAdminId() == null || deliveryReq.getAdminId() <= 0)
			throw new InternalServerException("Admin id missing", HttpStatus.OK);

		if (deliveryReq.getDeliveryId() != null && deliveryReq.getDeliveryId() > 0) {

			CustomerDelivery delivery = customerDeliveryRepo.findById(deliveryReq.getDeliveryId()).orElseThrow(
					() -> new InternalServerException("Resource not found with this credential", HttpStatus.OK));

			CustomerDeliveryResponseAll customerDeliveryResponse = CustomerDeliveryResponseDto
					.getDeliveryResponse(delivery);

			return Map.of("res", true, "data", customerDeliveryResponse);

		}

		if (deliveryReq.getPage() != null) {

			if (deliveryReq.getSize() == null)
				deliveryReq.setSize(20);

			Pageable pageable = PageRequest.of(deliveryReq.getPage() - 1, deliveryReq.getSize(),
					Sort.by("orderPlacedOn").descending());

			Page<CustomerDelivery> customerDeliveries;
			Integer filter = deliveryReq.getFilter();
			
			if(deliveryReq.getSearch() == null)
				deliveryReq.setSearch("");
			if(filter == null)
				filter = 0;

			if (filter.equals(0))
				customerDeliveries = customerDeliveryRepo.findByConditions(deliveryReq.getAdminId(),
						deliveryReq.getSearch(), null, null, null, null, null, null, pageable);
			else if (filter.equals(1))
				customerDeliveries = customerDeliveryRepo.findByConditions(deliveryReq.getAdminId(),
						deliveryReq.getSearch(), false, null, null, null, null, null, pageable);
			else if (filter.equals(2))
				customerDeliveries = customerDeliveryRepo.findByConditions(deliveryReq.getAdminId(),
						deliveryReq.getSearch(), null, true, null, null, null, null, pageable);
			else if (filter.equals(3))
				customerDeliveries = customerDeliveryRepo.findByConditions(deliveryReq.getAdminId(),
						deliveryReq.getSearch(), null, null, true, null, null, null, pageable);
			else if (filter.equals(4))
				customerDeliveries = customerDeliveryRepo.findByConditions(deliveryReq.getAdminId(),
						deliveryReq.getSearch(), null, null, null, true, null, null, pageable);
			else if (filter.equals(5))
				customerDeliveries = customerDeliveryRepo.findByConditions(deliveryReq.getAdminId(),
						deliveryReq.getSearch(), null, null, null, null, true, null, pageable);
			else if (filter.equals(6))
				customerDeliveries = customerDeliveryRepo.findByConditions(deliveryReq.getAdminId(),
						deliveryReq.getSearch(), null, null, null, null, null, true, pageable);
			else
				throw new InternalServerException("Wrong usage of filter", HttpStatus.OK);

			Page<CustomerDeliveryResponseAll> customerDeliveryResponse = customerDeliveries
					.map(CustomerDeliveryResponseDto::getDeliveryResponse);

			return Map.of("res", true, "data", customerDeliveryResponse.getContent(), "page",
					customerDeliveryResponse.getPageable().getPageNumber() + 1, "totalPage",
					customerDeliveryResponse.getTotalPages(), "totalRecord", customerDeliveryResponse.getTotalElements());

		}

		List<CustomerDelivery> customerDeliveries = customerDeliveryRepo
				.findAllByAdminAdminIdOrderByOrderPlacedOnDesc(deliveryReq.getAdminId());
		List<CustomerDeliveryResponseAll> customerDeliveryResponse = customerDeliveries.stream()
				.map(CustomerDeliveryResponseDto::getDeliveryResponse).toList();

		return Map.of("res", true, "data", customerDeliveryResponse);
	}

	public Map<String, Object> fetchCustomerByNameEmailAndId(Integer adminId) {

		if (adminId == null || adminId <= 0)
			throw new InternalServerException("Admin id missing", HttpStatus.OK);

		List<Map<String, Object>> selectiveResults = customerRepo.findSelectiveCustomerDetailsAsMap(adminId);

		return Map.of("res", true, "data", selectiveResults);
	}

	public Map<String, Object> getAllComparison(Integer adminId, Integer page, Integer size, String search) {

		if (adminId == null || adminId <= 0)
			throw new InternalServerException("Admin id missing", HttpStatus.OK);

		if (page != null && page > 0) {

			if (size == null)
				size = 20;
			;

			Pageable pageable = PageRequest.of(page - 1, size, Sort.by("compared_on").descending());

			Page<CustomerComparingEnergy> energyComparisons = customerComparingEnergyRepo
					.findAllByDifferentFilter(search, adminId, pageable);

			Page<CustomerComparingEnergyDto> energyComparisonResp = energyComparisons
					.map(CustomerComparingEnergyDto::customerComparisonResponse);

			return Map.of("res", true, "data", energyComparisonResp.getContent(), "page",
					energyComparisonResp.getPageable().getPageNumber() + 1, "totalPage",
					energyComparisonResp.getTotalPages(), "totalRecords", energyComparisonResp.getTotalElements());
		}

		List<CustomerComparingEnergy> energyComparisons = customerComparingEnergyRepo
				.findAllByAdminAdminIdOrderByIdDesc(adminId);
		List<CustomerComparingEnergyDto> energyComparisonResp = energyComparisons.stream()
				.map(CustomerComparingEnergyDto::customerComparisonResponse).toList();

		return Map.of("res", true, "data", energyComparisonResp, "totalRecords", energyComparisonResp.size());
	}

	@Transactional
	public Map<String, Object> addResponseToCustomerServiceRequest(CustomerServiceRequestDto serviceRequestDto) {

		if (serviceRequestDto.getMessage() == null || serviceRequestDto.getMessage().isEmpty())
			throw new InternalServerException("Service message missing", HttpStatus.OK);

		if (serviceRequestDto.getAdminId() == null || serviceRequestDto.getAdminId() <= 0)
			throw new InternalServerException("Admin id missing", HttpStatus.OK);

		if (serviceRequestDto.getServiceRequestId() == null || serviceRequestDto.getServiceRequestId() <= 0)
			throw new InternalServerException("Service request id missing", HttpStatus.OK);

		CustomerServiceRequest serviceRequest = customerServiceRequestRepo
				.findByIdAndAdminAdminId(serviceRequestDto.getServiceRequestId(), serviceRequestDto.getAdminId())
				.orElseThrow(() -> new InternalServerException(
						"Customer service request not found with this creddential", HttpStatus.OK));

		Customer customer = serviceRequest.getCustomer();

		AdminUser admin = customer.getAdmin();

		CustomerServiceRequestMessages message = CustomerServiceRequestMessages.builder().chatUser("ADMIN")
				.message(serviceRequestDto.getMessage()).build();

		serviceRequest.addCustomerServiceRequestMessage(message);
		serviceRequest.setIsOpen(false);
		serviceRequest.setInProgress(true);

		customerServiceRequestRepo.save(serviceRequest);

		Map<String, Object> dateTimeMap = Helper.getLocalDateTimeFromBigInteger(serviceRequest.getCreatedOn());

		String formattedDateTime = dateTimeMap.get("monthName").toString() + " " + dateTimeMap.get("date").toString()
				+ " " + dateTimeMap.get("year").toString() + ", at " + dateTimeMap.get("hour").toString() + ":"
				+ dateTimeMap.get("minute").toString() + " " + dateTimeMap.get("amPm").toString();

		String subject = "Received  a  response  from  the  consultant on ticket-No. "
				+ serviceRequest.getTicketNumber();
		String body = emailTemplate.createServiceRequestResponseEmailBody(customer.getSalutation(),
				customer.getLastName(), customer.getFirstName(), serviceRequest.getTicketNumber(), formattedDateTime,
				customer.getEmail(), serviceRequestDto.getMessage(), admin.getName());

		ServiceResponseEmailEvent serviceEventData = new ServiceResponseEmailEvent(customer.getEmail(), subject, body);
		eventPublisher.publishEvent(serviceEventData);

		return Map.of("res", true, "ticketNo", serviceRequest.getTicketNumber(), "message",
				"Message sended successfully");
	}

	public Map<String, Object> fetchCustomerServiceRequests(CustomerServiceRequestDto serviceRequestDto) {

		if (serviceRequestDto.getAdminId() == null || serviceRequestDto.getAdminId() <= 0)
			throw new InternalServerException("Admin id missing", HttpStatus.OK);

		if (serviceRequestDto.getPage() != null && serviceRequestDto.getPage() <= 0)
			throw new InternalServerException("Page is not set correctly", HttpStatus.OK);

		if (serviceRequestDto.getPage() != null && serviceRequestDto.getPage() > 0) {

			if (serviceRequestDto.getSize() == null || serviceRequestDto.getSize() <= 0)
				serviceRequestDto.setSize(10);

			Pageable pageable = PageRequest.of(serviceRequestDto.getPage() - 1, serviceRequestDto.getSize(),
					Sort.by("createdOn").descending());

			Page<CustomerServiceRequest> customerServiceRequestsPage = customerServiceRequestRepo
					.findAllByAdminAdminId(serviceRequestDto.getAdminId(), pageable);

			List<CustomerServiceRequest> customerServiceRequests = customerServiceRequestsPage.getContent();

			Map<String, List<CustomerServiceRequestResDtoForListing>> customerServiceDtoRes = customerServiceRequests
					.stream().map(CustomerServiceRequestDto::getAllListings).collect(Collectors.groupingBy(req -> {
						if (req.getIsOpen())
							return "open";
						if (req.getInProgress())
							return "progress";
						return "closed";
					}));

			List<CustomerServiceRequestResDtoForAdmin> allCustomerServiceRes = customerServiceRequests.stream()
					.map(CustomerServiceRequestDto::getAllListingsForAdmin).toList();

			return Map.of("res", true, "data",
					Map.of("open", customerServiceDtoRes.getOrDefault("open", List.of()), "inProgress",
							customerServiceDtoRes.getOrDefault("progress", List.of()), "closed",
							customerServiceDtoRes.getOrDefault("closed", List.of()), "all", allCustomerServiceRes,
							"page", customerServiceRequestsPage.getPageable().getPageNumber() + 1, "totalPage",
							customerServiceRequestsPage.getTotalPages()));

		}

		List<CustomerServiceRequest> customerServiceRequests = customerServiceRequestRepo
				.findAllByAdminAdminIdOrderByCreatedOnDesc(serviceRequestDto.getAdminId());

		if (customerServiceRequests == null || customerServiceRequests.isEmpty())
			return Map.of("res", true, "open", List.of(), "inProgress", List.of(), "closed", List.of());

		Map<String, List<CustomerServiceRequestResDtoForListing>> customerServiceDtoRes = customerServiceRequests
				.stream().map(CustomerServiceRequestDto::getAllListings).collect(Collectors.groupingBy(req -> {
					if (req.getIsOpen())
						return "open";
					if (req.getInProgress())
						return "progress";
					return "closed";
				}));

		List<CustomerServiceRequestResDtoForAdmin> allCustomerServiceRes = customerServiceRequests.stream()
				.map(CustomerServiceRequestDto::getAllListingsForAdmin).toList();

		return Map.of("res", true, "open", customerServiceDtoRes.getOrDefault("open", List.of()), "inProgress",
				customerServiceDtoRes.getOrDefault("progress", List.of()), "closed",
				customerServiceDtoRes.getOrDefault("closed", List.of()), "all", allCustomerServiceRes);
	}

	@Transactional
	public Map<String, Object> closeCustomerServiceRequest(Integer adminId, Integer serviceRequestId) {

		if (adminId == null || adminId <= 0)
			throw new InternalServerException("Admin id missing", HttpStatus.OK);
		if (serviceRequestId == null || serviceRequestId <= 0)
			throw new InternalServerException("Customer service request id missing", HttpStatus.OK);

		CustomerServiceRequest serviceRequest = customerServiceRequestRepo
				.findByIdAndAdminAdminId(serviceRequestId, adminId).orElseThrow(() -> new InternalServerException(
						"Customer Service request not found with this credential", HttpStatus.OK));

		if (serviceRequest.getInProgress()) {
			serviceRequest.setIsOpen(false);
			serviceRequest.setInProgress(false);
			serviceRequest.setIsClosed(true);
			serviceRequest.setRequestClosedOn(Helper.getCurrentTimeBerlin());
		} else if (serviceRequest.getIsClosed()) {
			serviceRequest.setIsOpen(false);
			serviceRequest.setInProgress(true);
			serviceRequest.setIsClosed(false);
			serviceRequest.setRequestClosedOn(null);
		}

		customerServiceRequestRepo.save(serviceRequest);

		return Map.of("res", true, "message", "Customer service request closed successfully");
	}

	@Transactional
	public Map<String, Object> updateAttornyStatus(CustomerAttornyDto attornyDto) {

		if (attornyDto.getAdminId() == null || attornyDto.getAdminId() <= 0)
			throw new InternalServerException("Admin id missing", HttpStatus.OK);
		if (attornyDto.getAttornyId() == null || attornyDto.getAttornyId() <= 0)
			throw new InternalServerException("Attorny id missing", HttpStatus.OK);
		if (attornyDto.getApprovalStatus() == null || attornyDto.getApprovalStatus() < 1
				|| attornyDto.getApprovalStatus() > 2)
			throw new InternalServerException("Approval status missing", HttpStatus.OK);

		CustomerAttorny attorny = customerAttornyRepo
				.findByIdAndAdminAdminId(attornyDto.getAttornyId(), attornyDto.getAdminId()).orElseThrow(
						() -> new InternalServerException("Attorny not found with this credential", HttpStatus.OK));

		if (attorny.getIsRevoked())
			throw new InternalServerException("Attorny is already revoked by customer", HttpStatus.OK);

		attorny.setApprovalStatus(attorny.getApprovalStatus());

		if (attornyDto.getApprovalStatus().equals(1))
			attorny.setApprovedOn(Helper.getCurrentTimeBerlin());
		else
			attorny.setRejectedOn(Helper.getCurrentTimeBerlin());

		customerAttornyRepo.save(attorny);

		return Map.of("res", true, "message", "Status updated successfully");
	}

	@Transactional
	public Map<String, Object> toggleNotificationOfCustomer(Integer adminId, Integer customerId) {

		if (adminId == null || adminId <= 0)
			throw new InternalServerException("Admin id missing", HttpStatus.OK);
		if (customerId == null || customerId <= 0)
			throw new InternalServerException("Customer id missing", HttpStatus.OK);

		Customer customer = customerRepo.findByCustomerIdAndAdminAdminId(customerId, adminId).orElseThrow(
				() -> new InternalServerException("Customer not found wiyth this credential", HttpStatus.OK));

		customer.setIsNotificationEnabled(!customer.getIsNotificationEnabled());

		customerRepo.save(customer);

		return Map.of("res", true, "message", "Customer notification updated");
	}

	@Transactional
	public Map<String, Object> createNewCustomer(CustomerDto customerDto) {
		customerDto.setIsVerified(true);
		Map<String, Object> createResponse = new HashMap<>(customerAuthService.customerSignUp(customerDto));
		if (createResponse.containsKey("page"))
			createResponse.remove("page");

		if (createResponse.containsKey("isAcknowledge"))
			createResponse.remove("isAcknowledge");

		if (createResponse.containsKey("data")) {

			Map<String, Object> responseData;
			try {
				String dataJson = objectMapper.writeValueAsString(createResponse.get("data"));
				responseData = objectMapper.readValue(dataJson, new TypeReference<Map<String, Object>>() {
				});
			} catch (JsonProcessingException e) {
				e.printStackTrace();
				throw new InternalServerException("Internal Server Error", HttpStatus.OK);
			}

			if (responseData != null && responseData.containsKey("id")) {
				Integer customerId = (Integer) responseData.get("id");
				Customer customer = customerRepo.findById(customerId).orElseThrow(() -> new RuntimeException());

				customer.setIsVerified(true);
				customer.setVerifiedOn(Helper.getCurrentTimeBerlin());
				customer.setIsAcknowledged(true);
				customerRepo.save(customer);
			}

			return createResponse;

		}

		throw new InternalServerException("Internal Server Error", HttpStatus.OK);
	}

	@Transactional
	public Map<String, Object> addCustomerNoteByAdmin(CustomerNoteDto noteDto) {
		if (noteDto == null)
			throw new InternalServerException("All required data missing", HttpStatus.OK);
		if (noteDto.getAdminId() == null || noteDto.getAdminId() <= 0)
			throw new InternalServerException("Admin id missing", HttpStatus.OK);
		if (noteDto.getCustomerId() == null || noteDto.getCustomerId() <= 0)
			throw new InternalServerException("Customer id missing", HttpStatus.OK);

		Customer customer = customerRepo.findByCustomerIdAndAdminAdminId(noteDto.getCustomerId(), noteDto.getAdminId())
				.orElseThrow(
						() -> new InternalServerException("Customer not found with this credential", HttpStatus.OK));

		CustomerNote note = CustomerNote.builder().note(noteDto.getNote()).build();

		customer.addCustomerNote(note);

		customerRepo.save(customer);

		return Map.of("res", true, "message", "Note added successfully");
	}

	@Transactional
	public Map<String, Object> addLexofficeNumberForCustomer(CustomerDto customerDto) {

		if (customerDto.getAdminId() == null || customerDto.getAdminId() <= 0)
			throw new InternalServerException("Admin id missing", HttpStatus.OK);
		if (customerDto.getId() == null || customerDto.getId() <= 0)
			throw new InternalServerException("Customer id missing", HttpStatus.OK);
		if (customerDto.getLexofficeNumber() == null || customerDto.getLexofficeNumber().isEmpty())
			throw new InternalServerException("Lexoffice number missing", HttpStatus.OK);

		Customer customer = customerRepo.findByCustomerIdAndAdminAdminId(customerDto.getId(), customerDto.getAdminId())
				.orElseThrow(
						() -> new InternalServerException("Customer not found with this credential", HttpStatus.OK));

		customer.setLexofficeNumber(customerDto.getLexofficeNumber());

		customerRepo.save(customer);

		return Map.of("res", true, "mesage", "lexoffice_Number added successfully");
	}

}
