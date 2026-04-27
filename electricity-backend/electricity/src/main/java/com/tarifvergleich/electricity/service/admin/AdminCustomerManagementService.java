package com.tarifvergleich.electricity.service.admin;

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

import com.tarifvergleich.electricity.dto.CustomerComparingEnergyDto;
import com.tarifvergleich.electricity.dto.CustomerDeliveryDto;
import com.tarifvergleich.electricity.dto.CustomerDeliveryResponseDto;
import com.tarifvergleich.electricity.dto.CustomerDeliveryResponseDto.CustomerDeliveryResponseAll;
import com.tarifvergleich.electricity.dto.CustomerDto;
import com.tarifvergleich.electricity.dto.CustomerDto.AdminCustomerResponse;
import com.tarifvergleich.electricity.dto.CustomerDto.SingleCustomerResponseDelivery;
import com.tarifvergleich.electricity.dto.CustomerServiceRequestDto;
import com.tarifvergleich.electricity.dto.CustomerServiceRequestDto.CustomerServiceRequestResDtoForAdmin;
import com.tarifvergleich.electricity.dto.CustomerServiceRequestDto.CustomerServiceRequestResDtoForListing;
import com.tarifvergleich.electricity.dto.ServiceRequestEmailEvent.ServiceResponseEmailEvent;
import com.tarifvergleich.electricity.exception.InternalServerException;
import com.tarifvergleich.electricity.model.AdminUser;
import com.tarifvergleich.electricity.model.Customer;
import com.tarifvergleich.electricity.model.CustomerComparingEnergy;
import com.tarifvergleich.electricity.model.CustomerDelivery;
import com.tarifvergleich.electricity.model.CustomerServiceRequest;
import com.tarifvergleich.electricity.model.CustomerServiceRequestMessages;
import com.tarifvergleich.electricity.repository.CustomerComparingEnergyRepository;
import com.tarifvergleich.electricity.repository.CustomerDeliveryRepository;
import com.tarifvergleich.electricity.repository.CustomerRepository;
import com.tarifvergleich.electricity.repository.CustomerServiceRequestRepository;
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
	private final ApplicationEventPublisher eventPublisher;
	private final EmailTemplate emailTemplate;

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

		if (deliveryReq.getPage() != null) {

			if (deliveryReq.getSize() == null)
				deliveryReq.setSize(20);

			Pageable pageable = PageRequest.of(deliveryReq.getPage() - 1, deliveryReq.getSize(),
					Sort.by("orderPlacedOn").descending());

			Page<CustomerDelivery> customerDeliveries = customerDeliveryRepo.findAll(pageable);

			Page<CustomerDeliveryResponseAll> customerDeliveryResponse = customerDeliveries
					.map(CustomerDeliveryResponseDto::getDeliveryResponse);

			return Map.of("res", true, "data", customerDeliveryResponse.getContent(), "page",
					customerDeliveryResponse.getPageable().getPageNumber() + 1, "totalPage",
					customerDeliveryResponse.getTotalPages());

		}

		List<CustomerDelivery> customerDeliveries = customerDeliveryRepo
				.findAllByAdminAdminIdOrderByOrderPlacedOnDesc(deliveryReq.getAdminId());
		List<CustomerDeliveryResponseAll> customerDeliveryResponse = customerDeliveries.stream()
				.map(CustomerDeliveryResponseDto::getDeliveryResponse).toList();

		return Map.of("res", true, "data", customerDeliveryResponse);
	}

	public Map<String, Object> getAllComparison(Integer adminId, Integer page, Integer size) {

		if (adminId == null || adminId <= 0)
			throw new InternalServerException("Admin id missing", HttpStatus.OK);

		if (page != null && page > 0) {

			if (size == null)
				size = 20;
			;

			Pageable pageable = PageRequest.of(page - 1, size, Sort.by("comparedOn").descending());

			Page<CustomerComparingEnergy> energyComparisons = customerComparingEnergyRepo.findAll(pageable);

			Page<CustomerComparingEnergyDto> energyComparisonResp = energyComparisons
					.map(CustomerComparingEnergyDto::customerComparisonResponse);

			return Map.of("res", true, "data", energyComparisonResp.getContent(), "page",
					energyComparisonResp.getPageable().getPageNumber() + 1, "totalPage",
					energyComparisonResp.getTotalPages());
		}

		List<CustomerComparingEnergy> energyComparisons = customerComparingEnergyRepo
				.findAllByAdminAdminIdOrderByIdDesc(adminId);
		List<CustomerComparingEnergyDto> energyComparisonResp = energyComparisons.stream()
				.map(CustomerComparingEnergyDto::customerComparisonResponse).toList();

		return Map.of("res", true, "data", energyComparisonResp);
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

}
