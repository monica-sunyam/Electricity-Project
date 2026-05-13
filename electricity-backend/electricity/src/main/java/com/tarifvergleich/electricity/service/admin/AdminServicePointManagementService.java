package com.tarifvergleich.electricity.service.admin;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.tarifvergleich.electricity.dto.CustomerRequestCounsellingDto;
import com.tarifvergleich.electricity.dto.CustomerRequestCounsellingDto.CustomerRequestCousellingResponseForAdmin;
import com.tarifvergleich.electricity.dto.CustomerServicesDto;
import com.tarifvergleich.electricity.dto.CustomerServicesDto.CustomerListOfServiceForAdminResDto;
import com.tarifvergleich.electricity.dto.ListOfHolidaysDto;
import com.tarifvergleich.electricity.dto.ListOfHolidaysDto.ListOfHolidaysResponseDto;
import com.tarifvergleich.electricity.exception.InternalServerException;
import com.tarifvergleich.electricity.model.AdminUser;
import com.tarifvergleich.electricity.model.CustomerRequestCounselling;
import com.tarifvergleich.electricity.model.CustomerServices;
import com.tarifvergleich.electricity.model.ListOfHolidays;
import com.tarifvergleich.electricity.repository.AdminUserRepository;
import com.tarifvergleich.electricity.repository.CustomerRequestCounsellingRepository;
import com.tarifvergleich.electricity.repository.CustomerServicesRepository;
import com.tarifvergleich.electricity.repository.ListOfHolidaysRepository;
import com.tarifvergleich.electricity.util.Helper;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminServicePointManagementService {

	private final AdminUserRepository adminUserRepo;
	private final CustomerServicesRepository customerServicesRepo;
	private final Helper helper;
	private final ListOfHolidaysRepository listOfHolidaysRepo;
	private final CustomerRequestCounsellingRepository customerRequestCounsellingRepo;

	@Transactional
	public Map<String, Object> addCustomerServices(CustomerServicesDto servicesDto) {

		if (servicesDto.getAdminId() == null || servicesDto.getAdminId() <= 0)
			throw new InternalServerException("Admin id missing", HttpStatus.OK);

		if (servicesDto.getServiceName() == null || servicesDto.getServiceName().isEmpty())
			throw new InternalServerException("Service name missing", HttpStatus.OK);
		if (servicesDto.getServiceType() == null || (!servicesDto.getServiceType().equalsIgnoreCase("general")
				&& !servicesDto.getServiceType().equalsIgnoreCase("delivery")
				&& !servicesDto.getServiceType().equalsIgnoreCase("all")))
			throw new InternalServerException("Service type missing", HttpStatus.OK);

		AdminUser admin = adminUserRepo.findById(servicesDto.getAdminId())
				.orElseThrow(() -> new InternalServerException("Admin not found with this credential", HttpStatus.OK));

		CustomerServices service = null;
		if (servicesDto.getServiceId() != null && servicesDto.getServiceId() > 0) {
			service = customerServicesRepo.findById(servicesDto.getServiceId())
					.orElseThrow(() -> new InternalServerException("Invalid service id", HttpStatus.OK));
			if (!service.getAdmin().getAdminId().equals(admin.getAdminId()))
				throw new InternalServerException("Admin and service's admin mis-match", HttpStatus.OK);
			service.setUpdatedOn(Helper.getCurrentTimeBerlin());
		} else {
			service = new CustomerServices();
			service.setAdmin(admin);
		}

		service.setServiceName(servicesDto.getServiceName());
		service.setServiceType(servicesDto.getServiceType().toUpperCase());

		service = customerServicesRepo.save(service);

		return Map.of("res", true, "message", "Service added successfully", "serviceId", service.getId());
	}

	@Transactional
	public Map<String, Object> removeCustomerService(CustomerServicesDto servicesDto) {

		if (servicesDto.getAdminId() == null || servicesDto.getAdminId() <= 0)
			throw new InternalServerException("Admin id missing", HttpStatus.OK);
		if (servicesDto.getServiceId() == null || servicesDto.getServiceId() <= 0)
			throw new InternalServerException("Service is missing", HttpStatus.OK);

		CustomerServices service = customerServicesRepo.findById(servicesDto.getServiceId())
				.orElseThrow(() -> new InternalServerException("Customer service not found", HttpStatus.OK));

		if (!service.getAdmin().getAdminId().equals(servicesDto.getAdminId()))
			throw new InternalServerException("Admin does not contain this customer service", HttpStatus.OK);

		customerServicesRepo.deleteById(servicesDto.getServiceId());

		return Map.of("res", true, "message", "Customer service removed successfully");
	}

	public Map<String, Object> fetchServices(CustomerServicesDto servicesDto) {

		if (servicesDto.getAdminId() == null || servicesDto.getAdminId() <= 0)
			throw new InternalServerException("Admin id missing", HttpStatus.OK);

		if (servicesDto.getServiceId() != null && servicesDto.getServiceId() > 0) {

			CustomerServices service = customerServicesRepo
					.findByIdAndAdminAdminId(servicesDto.getServiceId(), servicesDto.getAdminId()).orElseThrow(
							() -> new InternalServerException("Service not found with this credential", HttpStatus.OK));

			return Map.of("res", true, "data", CustomerServicesDto.mapCustomerServiceForAdmin(service));
		}

		if (servicesDto.getPage() != null) {

			if (servicesDto.getSize() == null || servicesDto.getSize() <= 0)
				servicesDto.setSize(10);

			Pageable pageable = PageRequest.of(servicesDto.getPage() - 1, servicesDto.getSize(),
					Sort.by("addedOn").descending());

			Page<CustomerServices> servicesPage = customerServicesRepo.findAllByAdminAdminId(servicesDto.getAdminId(),
					pageable);

			List<CustomerServices> services = servicesPage.getContent();

			List<CustomerListOfServiceForAdminResDto> servicesResponse = services.stream()
					.map(CustomerServicesDto::mapCustomerServiceForAdmin).toList();

			return Map.of("res", true, "data", servicesResponse, "page", servicesPage.getPageable().getPageNumber() + 1,
					"totalPage", servicesPage.getTotalPages());

		}

		List<CustomerServices> services = customerServicesRepo
				.findAllByAdminAdminIdOrderByAddedOnDesc(servicesDto.getAdminId());

		List<CustomerListOfServiceForAdminResDto> servicesResponse = services.stream()
				.map(CustomerServicesDto::mapCustomerServiceForAdmin).toList();

		return Map.of("res", true, "data", servicesResponse);
	}

	@Transactional
	public Map<String, Object> adminAddHolidays(ListOfHolidaysDto holidaysDto) {
		if (holidaysDto.getAdminId() == null || holidaysDto.getAdminId() <= 0)
			throw new InternalServerException("Admin id missing", HttpStatus.OK);

		Map<String, Object> dateDetail = Helper.getLocalDateTimeFromBigInteger(Helper.getCurrentTimeBerlin());

		Integer currentYear = (Integer) dateDetail.get("year");
		Integer year = holidaysDto.getStartDate().getYear();

		if (year < currentYear)
			throw new InternalServerException("Provide present or future dates", HttpStatus.OK);

		if (holidaysDto.getStartDate() == null || holidaysDto.getEndDate() == null)
			throw new InternalServerException("Provide valid starting and ending date", HttpStatus.OK);

		if (holidaysDto.getHolidayType() == null || holidaysDto.getHolidayType().isEmpty()
				|| (!holidaysDto.getHolidayType().equalsIgnoreCase("PUBLIC")
						&& !holidaysDto.getHolidayType().equalsIgnoreCase("COMPANY")
						&& !holidaysDto.getHolidayType().equalsIgnoreCase("OPTION")))
			throw new InternalServerException("Holiday type missing", HttpStatus.OK);

		LocalDate start = holidaysDto.getStartDate();
		LocalDate end = holidaysDto.getEndDate();

		if (start.isAfter(end)) {
			throw new InternalServerException("Start date cannot be after end date", HttpStatus.OK);
		}

		if (holidaysDto.getName() == null || holidaysDto.getName().isEmpty())
			throw new InternalServerException("Holiday name missing", HttpStatus.OK);

		AdminUser admin = adminUserRepo.findById(holidaysDto.getAdminId())
				.orElseThrow(() -> new InternalServerException("Admin not found with this credentials", HttpStatus.OK));

		String rangeId = null;

		if (holidaysDto.getRangeId() != null && !holidaysDto.getRangeId().isEmpty()) {

			List<ListOfHolidays> existingHolidays = listOfHolidaysRepo
					.findAllByAdminAdminIdAndRangeIdOrderByIdAsc(holidaysDto.getAdminId(), holidaysDto.getRangeId());

			if (existingHolidays == null || existingHolidays.isEmpty())
				throw new InternalServerException("Existing data not found with this credentials", HttpStatus.OK);

			rangeId = holidaysDto.getRangeId();

			Map<BigInteger, ListOfHolidays> holidayMap = existingHolidays.stream()
					.collect(Collectors.toMap(ListOfHolidays::getStartDate, h -> h));

			List<ListOfHolidays> toSave = new ArrayList<>();

			BigInteger targetStart = helper.toGermanTimestampWithDynamicTime(start, 0, 0);
			BigInteger targetEnd = helper.toGermanTimestampWithDynamicTime(end, 23, 59);

			List<ListOfHolidays> toDelete = existingHolidays.stream().filter(
					h -> h.getStartDate().compareTo(targetStart) < 0 || h.getStartDate().compareTo(targetEnd) > 0)
					.collect(Collectors.toList());

			if (!toDelete.isEmpty()) {
				listOfHolidaysRepo.deleteAll(toDelete);
			}

			for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {

				BigInteger currentDayStart = helper.toGermanTimestampWithDynamicTime(date, 0, 0);
				BigInteger currentDayEnd = helper.toGermanTimestampWithDynamicTime(date, 23, 59);

				if (holidayMap.containsKey(currentDayStart)) {
					ListOfHolidays existing = holidayMap.get(currentDayStart);
					existing.setName(holidaysDto.getName());
					existing.setHolidayType(holidaysDto.getHolidayType().toUpperCase());
					existing.setYear(date.getYear());
					existing.setEndDate(currentDayEnd);
					toSave.add(existing);
				} else {
					ListOfHolidays newHoliday = ListOfHolidays.builder().name(holidaysDto.getName())
							.startDate(currentDayStart).endDate(currentDayEnd).year(date.getYear())
							.holidayType(holidaysDto.getHolidayType().toUpperCase()).rangeId(holidaysDto.getRangeId())
							.admin(admin).build();
					toSave.add(newHoliday);
				}
			}

			listOfHolidaysRepo.saveAll(toSave);
		}

		else {
			List<ListOfHolidays> toSave = new ArrayList<>();

			rangeId = Helper.getUniqueId();

			for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {

				BigInteger currentDayStart = helper.toGermanTimestampWithDynamicTime(date, 0, 0);
				BigInteger currentDayEnd = helper.toGermanTimestampWithDynamicTime(date, 23, 59);

				ListOfHolidays holiday = listOfHolidaysRepo
						.findByAdminAdminIdAndStartDate(admin.getAdminId(), currentDayStart).orElse(null);

				if (holiday == null) {
					ListOfHolidays newHoliday = ListOfHolidays.builder().name(holidaysDto.getName())
							.startDate(currentDayStart).endDate(currentDayEnd).year(date.getYear())
							.holidayType(holidaysDto.getHolidayType().toUpperCase()).rangeId(rangeId).admin(admin)
							.build();
					toSave.add(newHoliday);
				} else {
					holiday.setName(holidaysDto.getName());
					holiday.setHolidayType(holidaysDto.getHolidayType().toUpperCase());
					holiday.setYear(date.getYear());
					holiday.setEndDate(currentDayEnd);
					holiday.setRangeId(rangeId);
					toSave.add(holiday);
				}
			}

			listOfHolidaysRepo.saveAll(toSave);
		}

		return Map.of("res", true, "message", "Holidays added successfully", "rangeId", rangeId);
	}

	public Map<String, Object> adminGetHolidayList(Integer adminId, Integer year) {

		if (adminId == null || adminId <= 0)
			throw new InternalServerException("Admin id missing", HttpStatus.OK);

		List<ListOfHolidays> holidays = listOfHolidaysRepo.findAllByAdminAdminIdAndYearOrderByStartDateAsc(adminId,
				year);

		if (holidays == null || holidays.isEmpty())
			return Map.of("res", true, "data", List.of());

		Map<String, List<ListOfHolidaysResponseDto>> holidayResponse = holidays.stream()
				.map(ListOfHolidaysDto::mapAdminListOfHolidays).collect(Collectors
						.groupingBy(ListOfHolidaysDto::getMonthName, LinkedHashMap::new, Collectors.toList()));

		return Map.of("res", true, "data", holidayResponse);
	}

	@Transactional
	public Map<String, Object> adminDeleteHolidays(ListOfHolidaysDto holidaysDto) {

		if (holidaysDto.getAdminId() == null || holidaysDto.getAdminId() <= 0)
			throw new InternalServerException("Admin id missing", HttpStatus.OK);

		if (holidaysDto.getHolidayId() == null || holidaysDto.getHolidayId() <= 0)
			throw new InternalServerException("Holiday id missing", HttpStatus.OK);

		if (!listOfHolidaysRepo.existsByIdAndAdminAdminId(holidaysDto.getHolidayId(), holidaysDto.getAdminId()))
			throw new InternalServerException("Holiday record not found with this credential", HttpStatus.OK);

		listOfHolidaysRepo.deleteByIdAndAdminAdminId(holidaysDto.getHolidayId(), holidaysDto.getAdminId());

		return Map.of("res", true, "message", "Holiday removed successfully");
	}

	public Map<String, Object> fetchCounsellingrequets(CustomerRequestCounsellingDto requestDto) {

		if (requestDto.getAdminId() == null || requestDto.getAdminId() <= 0)
			throw new InternalServerException("Admin id missing", HttpStatus.OK);

		Long totalConcluded = customerRequestCounsellingRepo.countAllByAdminAdminIdAndConcluded(requestDto.getAdminId(),
				true);
		Long totalUnconcluded = customerRequestCounsellingRepo
				.countAllByAdminAdminIdAndConcluded(requestDto.getAdminId(), false);
		Long totalRequests = customerRequestCounsellingRepo.count();

		if (requestDto.getPage() != null && requestDto.getPage() > 0) {
			if (requestDto.getSize() == null || requestDto.getSize() <= 0)
				requestDto.setSize(10);

			Pageable pageable = PageRequest.of(requestDto.getPage() - 1, requestDto.getSize(),
					Sort.by("createdOn").descending());
			Page<CustomerRequestCounselling> counsellingRequests = customerRequestCounsellingRepo
					.findAllByAdminAdminIdAndOptionalConclude(requestDto.getAdminId(), requestDto.getConcluded(), pageable);

			Page<CustomerRequestCousellingResponseForAdmin> mappedRequest = counsellingRequests
					.map(CustomerRequestCounsellingDto::mapCustomerRequestCounsellingResponseForAdmin);

			return Map.of("res", true, "data", mappedRequest.getContent(), "page",
					mappedRequest.getPageable().getPageNumber() + 1, "totalPage", mappedRequest.getTotalPages(),
					"totalRecords", totalRequests, "totalConsluded", totalConcluded, "totalUnconcluded",
					totalUnconcluded);

		}

		List<CustomerRequestCounselling> counsellingRequests = customerRequestCounsellingRepo
				.findAllByAdminAdminIdAndOptionalConcludedOrderByCreatedOnDesc(requestDto.getAdminId(), requestDto.getConcluded());

		List<CustomerRequestCousellingResponseForAdmin> mappedRequest = counsellingRequests.stream()
				.map(CustomerRequestCounsellingDto::mapCustomerRequestCounsellingResponseForAdmin).toList();

		return Map.of("res", true, "data", mappedRequest, "totalRecords", totalRequests, "totalConsluded",
				totalConcluded, "totalUnconcluded", totalUnconcluded);
	}

	@Transactional
	public Map<String, Object> toggleCustomerRequestCounsellingConcluded(Integer adminId, Integer counsellingId,
			Boolean setConclusion) {

		if (adminId == null || adminId <= 0)
			throw new InternalServerException("Admin id missing", HttpStatus.OK);

		if (counsellingId == null || counsellingId <= 0)
			throw new InternalServerException("Counselling id missing", HttpStatus.OK);

		CustomerRequestCounselling counsellingRequest = customerRequestCounsellingRepo
				.findByIdAndAdminAdminId(counsellingId, adminId).orElseThrow(
						() -> new InternalServerException("Request not found with this credential", HttpStatus.OK));

		if (setConclusion == null)
			setConclusion = !counsellingRequest.getConcluded();
		else if (setConclusion.equals(counsellingRequest.getConcluded()))
			return Map.of("res", true, "message", "Request status updated successfully");

		Integer result = customerRequestCounsellingRepo.updateConcludedByAdminAdminIdAndId(counsellingId, adminId,
				setConclusion);

		if (result == null || result <= 0)
			throw new InternalServerException("Internal Server issue", HttpStatus.OK);

		return Map.of("res", true, "message", "Request status updated successfully");
	}
	
	public Map<String, Object> fetchAllAdminDocuments(Integer adminId){
		return Map.of();
	}

}
