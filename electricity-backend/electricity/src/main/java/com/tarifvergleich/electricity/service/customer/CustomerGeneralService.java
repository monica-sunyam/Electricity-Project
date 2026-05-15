package com.tarifvergleich.electricity.service.customer;

import java.math.BigInteger;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.tarifvergleich.electricity.dto.CustomerRequestCounsellingDto;
import com.tarifvergleich.electricity.exception.InternalServerException;
import com.tarifvergleich.electricity.model.AdminUser;
import com.tarifvergleich.electricity.model.Customer;
import com.tarifvergleich.electricity.model.CustomerRequestCounselling;
import com.tarifvergleich.electricity.model.ListOfHolidays;
import com.tarifvergleich.electricity.repository.AdminUserRepository;
import com.tarifvergleich.electricity.repository.CustomerRepository;
import com.tarifvergleich.electricity.repository.CustomerRequestCounsellingRepository;
import com.tarifvergleich.electricity.repository.ListOfHolidaysRepository;
import com.tarifvergleich.electricity.service.MailService;
import com.tarifvergleich.electricity.util.EmailTemplate;
import com.tarifvergleich.electricity.util.Helper;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomerGeneralService {

	private final CustomerRequestCounsellingRepository customerRequestCounsellingRepo;
	private final ListOfHolidaysRepository listOfHolidaysRepo;
	private final CustomerRepository customerRepo;
	private final AdminUserRepository adminUserRepo;
	private final Helper helper;
	private final MailService mailService;
	private final EmailTemplate emailTemplate;

	public Map<String, Object> checkDateIsHoliday(CustomerRequestCounsellingDto scheduleDto) {

		if (scheduleDto == null)
			throw new InternalServerException("schedule missing", HttpStatus.OK);

		if (scheduleDto.getAdminId() == null || scheduleDto.getAdminId() <= 0)
			throw new InternalServerException("Admin missing", HttpStatus.OK);

		if (scheduleDto.getScheduleDate() == null)
			throw new InternalServerException("Schedule date not found", HttpStatus.OK);

		if (scheduleDto.getScheduleDate().isBefore(LocalDate.now(ZoneId.of("Europe/Berlin"))))
			throw new InternalServerException("Past Date not allowed", HttpStatus.OK);

		if (!adminUserRepo.existsById(scheduleDto.getAdminId()))
			throw new InternalServerException("Admin not found", HttpStatus.OK);

		BigInteger startTime = helper.toGermanTimestampWithDynamicTime(scheduleDto.getScheduleDate(), 0, 0);

		DayOfWeek dayName = scheduleDto.getScheduleDate().atStartOfDay().atZone(ZoneId.of("Europe/Berlin"))
				.getDayOfWeek();

		if (listOfHolidaysRepo.existsByAdminAdminIdAndStartDate(scheduleDto.getAdminId(), startTime)
				|| dayName.equals(DayOfWeek.SUNDAY)) {

			List<LocalDate> holidayDates = new LinkedList<LocalDate>();
			LocalDate start = scheduleDto.getScheduleDate().atStartOfDay(ZoneId.of("Europe/Berlin")).toLocalDate();
			if (dayName.equals(DayOfWeek.SUNDAY)) {
				holidayDates.add(start);
				startTime = helper.toGermanTimestampWithDynamicTime((start = start.plusDays(1)), 0, 0);
			}

			while (listOfHolidaysRepo.existsByAdminAdminIdAndStartDate(scheduleDto.getAdminId(), startTime) || start
					.atStartOfDay().atZone(ZoneId.of("Europe/Berlin")).getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
				if (!start.atStartOfDay().atZone(ZoneId.of("Europe/Berlin")).getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
					ListOfHolidays holiday = listOfHolidaysRepo
							.findByAdminAdminIdAndStartDate(scheduleDto.getAdminId(), startTime).orElse(null);

					if (holiday != null) {
						String rangeId = holiday.getRangeId();

						List<ListOfHolidays> holidays = listOfHolidaysRepo
								.findAllByAdminAdminIdAndRangeIdOrderByIdAsc(scheduleDto.getAdminId(), rangeId);

						startTime = holidays.getLast().getStartDate();
						LocalDate temp = helper.toGermalDateStamp(startTime);

						while (!start.isAfter(temp)) {
							holidayDates.add(start);
							start = start.plusDays(1);
						}
						startTime = helper.toGermanTimestampWithDynamicTime(start, 0, 0);
					}
				} else {
					holidayDates.add(start);
					start = start.plusDays(1);
					startTime = helper.toGermanTimestampWithDynamicTime(start, 0, 0);
				}
			}

			startTime = helper.toGermanTimestampWithDynamicTime(start, 0, 0);
			Map<String, Object> dateFormatter = Helper.getLocalDateTimeFromBigInteger(startTime);

			List<LocalDate> nextThreeWorkingDays = new LinkedList<LocalDate>();
			while (nextThreeWorkingDays.size() < 3) {
				if (!listOfHolidaysRepo.existsByAdminAdminIdAndStartDate(scheduleDto.getAdminId(), startTime) && !start
						.atStartOfDay().atZone(ZoneId.of("Europe/Berlin")).getDayOfWeek().equals(DayOfWeek.SUNDAY))
					nextThreeWorkingDays.add(start);
				else if (!start.atStartOfDay().atZone(ZoneId.of("Europe/Berlin")).getDayOfWeek()
						.equals(DayOfWeek.SUNDAY)) {
					ListOfHolidays holiday = listOfHolidaysRepo
							.findByAdminAdminIdAndStartDate(scheduleDto.getAdminId(), startTime).orElse(null);

					if (holiday != null) {
						String rangeId = holiday.getRangeId();

						List<ListOfHolidays> holidays = listOfHolidaysRepo
								.findAllByAdminAdminIdAndRangeIdOrderByIdAsc(scheduleDto.getAdminId(), rangeId);

						startTime = holidays.getLast().getStartDate();
						LocalDate temp = helper.toGermalDateStamp(startTime);

						while (!start.isAfter(temp)) {
							start = start.plusDays(1);
						}
						startTime = helper.toGermanTimestampWithDynamicTime(start, 0, 0);
					}
				}
				start = start.plusDays(1);
				startTime = helper.toGermanTimestampWithDynamicTime(start, 0, 0);

			}

			return Map.of("res", false, "nextDate", nextThreeWorkingDays, "nextDay", dateFormatter.get("dayOfWeek"),
					"errorMessage", "Selected day is a holiday", "holidayDated", holidayDates);
		}

		return Map.of("res", true, "message", "valid date");
	}

	@Transactional
	public Map<String, Object> addCounsellingSchedule(CustomerRequestCounsellingDto scheduleDto) {

		if (scheduleDto == null)
			throw new InternalServerException("Schedule not found", HttpStatus.OK);
		if (scheduleDto.getAdminId() == null || scheduleDto.getAdminId() <= 0)
			throw new InternalServerException("Admin id missing", HttpStatus.OK);
		if (scheduleDto.getDescription() == null || scheduleDto.getDescription().isEmpty())
			throw new InternalServerException("Descripton missing", HttpStatus.OK);
		if (scheduleDto.getTimeSlot() == null || scheduleDto.getTimeSlot().isEmpty())
			throw new InternalServerException("Time slot not found", HttpStatus.OK);
		if (scheduleDto.getMobileNumber() == null || scheduleDto.getMobileNumber().isEmpty())
			throw new InternalServerException("Contact number missing", HttpStatus.OK);
		if (scheduleDto.getScheduleDate() == null)
			throw new InternalServerException("Schedule date not found", HttpStatus.OK);
		if (scheduleDto.getScheduleDate().isBefore(LocalDate.now(ZoneId.of("Europe/Berlin"))))
			throw new InternalServerException("Past Date not allowed", HttpStatus.OK);

		String[] timeSlotRange = scheduleDto.getTimeSlot().trim().split("-");

		if (Integer.parseInt(timeSlotRange[0].trim()) < 8 && Integer.parseInt(timeSlotRange[1].trim()) > 20)
			throw new InternalServerException("Wrong time slot provided", HttpStatus.OK);

		AdminUser admin;
		Customer customer = null;
		if (scheduleDto.getCustomerId() != null && scheduleDto.getCustomerId() > 0) {
			customer = customerRepo
					.findByCustomerIdAndAdminAdminId(scheduleDto.getCustomerId(), scheduleDto.getAdminId())
					.orElseThrow(() -> new InternalServerException("Customer and admin not found with this credential",
							HttpStatus.OK));
			admin = customer.getAdmin();
		} else {
			admin = adminUserRepo.findById(scheduleDto.getAdminId()).orElseThrow(
					() -> new InternalServerException("Admin not found with this credential", HttpStatus.OK));
		}

		if (listOfHolidaysRepo.existsByAdminAdminIdAndStartDate(admin.getAdminId(),
				helper.toGermanTimestampWithDynamicTime(scheduleDto.getScheduleDate(), 0, 0))
				|| scheduleDto.getScheduleDate().atStartOfDay().atZone(ZoneId.of("Europe/Berlin")).getDayOfWeek()
						.equals(DayOfWeek.SUNDAY))
			throw new InternalServerException("Selected date is a holiday", HttpStatus.OK);

		CustomerRequestCounselling counsellingReq = CustomerRequestCounselling.builder()
				.mobileNumber(scheduleDto.getMobileNumber())
				.weekDay(scheduleDto.getScheduleDate().atStartOfDay().atZone(ZoneId.of("Europe/Berlin")).getDayOfWeek()
						.name())
				.timeSlot(scheduleDto.getTimeSlot()).description(scheduleDto.getDescription())
				.scheduleDate(helper.toGermanTimestampWithDynamicTime(scheduleDto.getScheduleDate(), 0, 0))
				.customer(customer).admin(admin).build();

		customerRequestCounsellingRepo.save(counsellingReq);

		Integer startTime = Integer.parseInt(timeSlotRange[0].trim());
		Integer endTime = Integer.parseInt(timeSlotRange[1].trim());
		String timeSlot = startTime < 12 ? startTime + " A.M"
				: startTime == 12 ? startTime + " P.M" : (startTime - 12) + " P.M";
		timeSlot += endTime < 12 ? endTime + " A.M" : endTime == 12 ? endTime + " P.M" : (endTime - 12) + " P.M";

		String emailBody = emailTemplate.createAdminNewCounsellingRequestEmailBody(admin.getName(),
				scheduleDto.getScheduleDate().toString(), timeSlot, scheduleDto.getMobileNumber(),
				scheduleDto.getDescription());
		String to = admin.getEmail();

		String subject = "New Request for Counselling";

		mailService.sendMail(to, subject, emailBody);

		return Map.of("res", true, "message", "Request added successfully");
	}

	public Map<String, Object> fetchWorkingDays(Integer adminId) {
		if (adminId == null || adminId <= 0)
			throw new InternalServerException("Admin id missing", HttpStatus.OK);

		Map<LocalDate, String> listOfWorkingDays = new LinkedHashMap<LocalDate, String>();

		BigInteger startDate = helper.toGermamUnixTimestamp(LocalDate.now(ZoneId.of("Europe/Berlin")));
		
		if(LocalDateTime.now(ZoneId.of("Europe/Berlin")).getHour() > 15)
			startDate = helper.toGermamUnixTimestamp(helper.toGermalDateStamp(startDate).plusDays(1));

		while (listOfWorkingDays.size() < 3) {

			if (helper.toGermalDateStamp(startDate).atStartOfDay().getDayOfWeek().equals(DayOfWeek.SUNDAY))
				startDate = helper.toGermamUnixTimestamp(helper.toGermalDateStamp(startDate).plusDays(1));

			ListOfHolidays holiday = listOfHolidaysRepo.findByAdminAdminIdAndStartDate(adminId, startDate).orElse(null);

			if (holiday == null) {
				listOfWorkingDays.put(helper.toGermalDateStamp(startDate), Instant.ofEpochSecond(startDate.longValue())
						.atZone(ZoneId.of("Europe/Berlin")).getDayOfWeek().toString());
				
			} else {
				String rangeId = holiday.getRangeId();
				ListOfHolidays tempHoliday = listOfHolidaysRepo.findFirstByRangeIdOrderByIdDesc(rangeId)
						.orElseThrow(() -> new InternalServerException("Error fetching dates", HttpStatus.OK));
				startDate = tempHoliday.getStartDate();
			}
			startDate = helper.toGermamUnixTimestamp(helper.toGermalDateStamp(startDate).plusDays(1));
		}

		return Map.of("res", true, "data", listOfWorkingDays);
	}

}
