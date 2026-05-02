package com.tarifvergleich.electricity.service.customer;

import java.math.BigInteger;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarifvergleich.electricity.dto.CustomerBillingRequestDto;
import com.tarifvergleich.electricity.dto.CustomerConnectionRequestDto;
import com.tarifvergleich.electricity.dto.CustomerContactScheduleRequestDto;
import com.tarifvergleich.electricity.dto.CustomerDeliveryDto;
import com.tarifvergleich.electricity.dto.CustomerDeliveryResponseDto;
import com.tarifvergleich.electricity.dto.CustomerPaymentRequestDto;
import com.tarifvergleich.electricity.dto.CustomerPaymentRequestDto.AccountHolderDto;
import com.tarifvergleich.electricity.dto.CustomerPaymentRequestDto.PaymentDto;
import com.tarifvergleich.electricity.dto.EnergyRateDto;
import com.tarifvergleich.electricity.exception.InternalServerException;
import com.tarifvergleich.electricity.mapper.CustomerResponseMapper;
import com.tarifvergleich.electricity.model.Customer;
import com.tarifvergleich.electricity.model.CustomerAddress;
import com.tarifvergleich.electricity.model.CustomerBillingAddress;
import com.tarifvergleich.electricity.model.CustomerConnect;
import com.tarifvergleich.electricity.model.CustomerContactSchedule;
import com.tarifvergleich.electricity.model.CustomerDelivery;
import com.tarifvergleich.electricity.model.CustomerPayment;
import com.tarifvergleich.electricity.model.CustomerSelectedProvider;
import com.tarifvergleich.electricity.repository.CustomerAddressRepository;
import com.tarifvergleich.electricity.repository.CustomerDeliveryRepository;
import com.tarifvergleich.electricity.repository.CustomerRepository;
import com.tarifvergleich.electricity.repository.ListOfHolidaysRepository;
import com.tarifvergleich.electricity.service.ElectricityComparisonService;
import com.tarifvergleich.electricity.service.MailService;
import com.tarifvergleich.electricity.util.FileServiceCustomer;
import com.tarifvergleich.electricity.util.Helper;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomerBookingService {

	private final CustomerRepository customerRepo;
	private final CustomerAddressRepository customerAddressRepo;
	private final CustomerResponseMapper customerResponseMapper;
	private final CustomerDeliveryRepository customerDeliveryRepo;
	private final ListOfHolidaysRepository listOfHolidaysRepo;
	private final Helper helper;
	private final ObjectMapper objectMapper;
	private final ElectricityComparisonService electricityComparisonService;
	private final FileServiceCustomer fileServiceCustomer;
	private final MailService mailService;

	@Transactional
	public Map<String, Object> saveDelivery(Integer customerId, Integer deliveryId, CustomerDeliveryDto deliveryDto,
			CustomerBillingRequestDto billingRequestDto, EnergyRateDto providerInfo) {

		if (customerId == null || customerId <= 0)
			throw new InternalServerException("Customer id missing", HttpStatus.OK);

		if (providerInfo == null)
			throw new InternalServerException("Provider data missing", HttpStatus.OK);

		Customer customer = customerRepo.findById(customerId)
				.orElseThrow(() -> new InternalServerException("Customer not found", HttpStatus.OK));

		CustomerDelivery editDelivery = null;
		if (deliveryId != null)
			editDelivery = customerDeliveryRepo.findById(deliveryId)
					.orElseThrow(() -> new InternalServerException("Delivery record not found", HttpStatus.OK));

		if (deliveryDto == null)
			throw new InternalServerException("Delivery details not found", HttpStatus.OK);
		if (billingRequestDto == null)
			throw new InternalServerException("Billing details not found", HttpStatus.OK);

		if (deliveryDto.getFirstName() == null || deliveryDto.getFirstName().isEmpty()
				|| deliveryDto.getLastName() == null || deliveryDto.getLastName().isEmpty())
			throw new InternalServerException("Name missing", HttpStatus.OK);

		if (deliveryDto.getMobile() == null || deliveryDto.getMobile().isEmpty())
			throw new InternalServerException("Mobile number missing", HttpStatus.OK);

		if (deliveryDto.getSalutation() == null || deliveryDto.getSalutation().isEmpty())
			throw new InternalServerException("Salutation missing", HttpStatus.OK);

//		if(deliveryDto.getTelephone() == null || deliveryDto.getTelephone().isEmpty())
//			throw new InternalServerException("Telephone number missing", HttpStatus.OK);

		if (deliveryDto.getZip() == null || deliveryDto.getZip().isEmpty())
			throw new InternalServerException("Zip code missing", HttpStatus.OK);

		if (deliveryDto.getCity() == null || deliveryDto.getCity().isEmpty())
			throw new InternalServerException("City missing", HttpStatus.OK);

		if (deliveryDto.getStreet() == null || deliveryDto.getStreet().isEmpty())
			throw new InternalServerException("Street missing", HttpStatus.OK);

		if (deliveryDto.getDob() == null)
			throw new InternalServerException("DOB missing", HttpStatus.OK);

		if (deliveryDto.getDeliveryType() == null || deliveryDto.getDeliveryType().isEmpty()
				|| (!deliveryDto.getDeliveryType().equalsIgnoreCase("Electricity")
						&& !deliveryDto.getDeliveryType().equalsIgnoreCase("GS")))
			throw new InternalServerException("Delivery type missing", HttpStatus.OK);

		LocalDate todayInBerlin = LocalDate.now(ZoneId.of("Europe/Berlin"));

		// 1. Calculate the "cutoff" date (18 years ago)
		LocalDate eighteenYearsAgo = todayInBerlin.minusYears(18);

		// 2. Compare the provided date (e.g., dateOfBirth)
		if (deliveryDto.getDob().isAfter(eighteenYearsAgo)) {
			throw new InternalServerException("User must be at least 18 years old", HttpStatus.OK);
		}

		CustomerBillingAddress billingAddress = null;

		if (billingRequestDto.getDifferent()) {

			if (billingRequestDto.getZip() == null || billingRequestDto.getZip().isEmpty())
				throw new InternalServerException("Billing zip code missing", HttpStatus.OK);
			if (billingRequestDto.getCity() == null || billingRequestDto.getCity().isEmpty())
				throw new InternalServerException("Billing city missing", HttpStatus.OK);
			if (billingRequestDto.getStreet() == null || billingRequestDto.getStreet().isEmpty())
				throw new InternalServerException("Billing street missing", HttpStatus.OK);

			if (editDelivery == null) {

				billingAddress = CustomerBillingAddress.builder().zip(billingRequestDto.getZip())
						.city(billingRequestDto.getCity()).street(billingRequestDto.getStreet())
						.houseNumber(billingRequestDto.getHouseNumber()).isDifferent(true).build();
			} else {
				billingAddress = editDelivery.getBillingAddress();

				billingAddress.setZip(billingRequestDto.getZip());
				billingAddress.setCity(billingRequestDto.getCity());
				billingAddress.setStreet(billingRequestDto.getStreet());
				billingAddress.setHouseNumber(billingRequestDto.getHouseNumber());
				billingAddress.setIsDifferent(true);
			}
		} else {
			if (editDelivery == null) {
				billingAddress = CustomerBillingAddress.builder().zip(deliveryDto.getZip()).city(deliveryDto.getCity())
						.street(deliveryDto.getStreet()).houseNumber(deliveryDto.getHouseNumber()).isDifferent(false)
						.build();
			} else {
				billingAddress = editDelivery.getBillingAddress();

				billingAddress.setZip(deliveryDto.getZip());
				billingAddress.setCity(deliveryDto.getCity());
				billingAddress.setStreet(deliveryDto.getStreet());
				billingAddress.setHouseNumber(deliveryDto.getHouseNumber());
				billingAddress.setIsDifferent(false);
			}
		}

		CustomerAddress address = customerAddressRepo.findAddress(customerId, deliveryDto.getZip(),
				deliveryDto.getCity(), deliveryDto.getStreet(), deliveryDto.getHouseNumber()).orElse(null);

		if (address == null) {
			address = CustomerAddress.builder().zip(deliveryDto.getZip()).city(deliveryDto.getCity())
					.street(deliveryDto.getStreet()).houseNumber(deliveryDto.getHouseNumber()).customerId(customer)
					.build();

			customer.addCustomerAddress(address);
		}

		if (editDelivery == null) {

			CustomerSelectedProvider selectedProvider = CustomerSelectedProvider.builder()
					.branch(providerInfo.getBranch()).netzProviderId(providerInfo.getNetzProviderId())
					.providerId(providerInfo.getProviderId()).providerSVGPath(providerInfo.getProviderSVGPath())
					.providerName(providerInfo.getProviderName()).rateId(providerInfo.getRateId())
					.rateName(providerInfo.getRateName()).totalPrice(providerInfo.getTotalPrice())
					.consumption(providerInfo.getConsumption()).basePriceYear(providerInfo.getBasePriceYear())
					.workPrice(providerInfo.getWorkPrice()).totalPriceMonth(providerInfo.getTotalPriceMonth())
					.type(providerInfo.getType()).raw(objectMapper.valueToTree(providerInfo)).build();

			CustomerDelivery delivery = CustomerDelivery.builder().title(deliveryDto.getTitle())
					.firstName(deliveryDto.getFirstName()).lastName(deliveryDto.getLastName()).address(address)
					.billingAddress(billingAddress).salutation(deliveryDto.getSalutation())
					.mobile(deliveryDto.getMobile()).telephone(deliveryDto.getTelephone())
					.deliveryType(deliveryDto.getDeliveryType().toUpperCase()).customerProvider(selectedProvider)
					.dob(helper.toGermamUnixTimestamp(deliveryDto.getDob())).build();

			delivery.setUserAdmin(customer.getAdmin());

			customer.addCustomerDelivery(delivery);
			customerRepo.save(customer);
			deliveryId = customer.getCustomerDelivery().getLast().getId();
		} else {
			editDelivery.setTitle(deliveryDto.getTitle());
			editDelivery.setFirstName(deliveryDto.getFirstName());
			editDelivery.setLastName(deliveryDto.getLastName());
			editDelivery.setSalutation(deliveryDto.getSalutation());
			editDelivery.setMobile(deliveryDto.getMobile());
			editDelivery.setTelephone(deliveryDto.getTelephone());
			editDelivery.setDob(helper.toGermamUnixTimestamp(deliveryDto.getDob()));
			editDelivery.setBillingAddress(billingAddress);

			editDelivery.setAddress(address);
			customerDeliveryRepo.save(editDelivery);
			customerRepo.save(customer);
		}

		return Map.of("res", true, "customerId", customerId, "deliveryId", deliveryId);
	}

	@Transactional
	public Map<String, Object> saveConnection(Integer customerId, Integer deliveryId,
			CustomerConnectionRequestDto customerConnectDto) {

		if (customerId == null || customerId <= 0)
			throw new InternalServerException("Customer id missing", HttpStatus.OK);

		if (deliveryId == null || deliveryId <= 0)
			throw new InternalServerException("Devilery id missing", HttpStatus.OK);

		if (customerConnectDto.getIsMovingIn() == null)
			throw new InternalServerException("Moving in missing", HttpStatus.OK);

		if (customerConnectDto.getIsMovingIn()) {
			if (customerConnectDto.getMoveInDate() == null)
				throw new InternalServerException("Moving in date missing", HttpStatus.OK);

			if (customerConnectDto.getMoveInDate().isBefore(LocalDate.now(ZoneId.of("Europe/Berlin"))))
				throw new InternalServerException("Moving in date is past date", HttpStatus.OK);
		} else {
			if (customerConnectDto.getAutoCancellation() == null)
				throw new InternalServerException("Auto Cancellation missing", HttpStatus.OK);

			if (customerConnectDto.getAlreadyCancelled() == null)
				throw new InternalServerException("Already cancelled missing", HttpStatus.OK);

			if (customerConnectDto.getSelfCancellation() == null)
				throw new InternalServerException("Self cancellation missing", HttpStatus.OK);

			if (customerConnectDto.getDelivery() == null)
				throw new InternalServerException("Delivery option missing", HttpStatus.OK);

			if (customerConnectDto.getDelivery()) {
				if (customerConnectDto.getDesiredDelivery() == null
						|| customerConnectDto.getDesiredDelivery().isBefore(LocalDate.now(ZoneId.of("Europe/Berlin"))))

					throw new InternalServerException("Desired Delivery not found or ill formed", HttpStatus.OK);
			}
		}

		if (customerConnectDto.getSubmitLater() == null)
			throw new InternalServerException("Submit later not found", HttpStatus.OK);

		if (customerConnectDto.getMeterNumber() == null || customerConnectDto.getMeterNumber().isEmpty())
			throw new InternalServerException("Meter number missing", HttpStatus.OK);

		CustomerDelivery delivery = customerDeliveryRepo.findById(deliveryId)
				.orElseThrow(() -> new InternalServerException("Delivery record not found", HttpStatus.OK));

		if (delivery.getCustomerId().getCustomerId() != customerId)
			throw new InternalServerException("Customer not found", HttpStatus.OK);

		if (delivery.getCustomerConnection() == null) {

			CustomerConnect customerConnect = CustomerConnect.builder().isMovingIn(customerConnectDto.getIsMovingIn())
					.moveInDate(customerConnectDto.getMoveInDate() != null
							? helper.toGermamUnixTimestamp(customerConnectDto.getMoveInDate())
							: null)
					.submitLater(customerConnectDto.getSubmitLater()).meterNumber(customerConnectDto.getMeterNumber())
					.currentProvider(customerConnectDto.getCurrentProvider())
					.autoCancellation(customerConnectDto.getAutoCancellation())
					.alreadyCancelled(customerConnectDto.getAlreadyCancelled())
					.selfCancellation(customerConnectDto.getSelfCancellation())
					.delivery(customerConnectDto.getDelivery())
					.desiredDelivery(customerConnectDto.getDesiredDelivery() != null
							? helper.toGermamUnixTimestamp(customerConnectDto.getDesiredDelivery())
							: null)
					.marketLocationId(customerConnectDto.getMarketLocationId()).customerDelivery(delivery).build();

			delivery.setCustomerConnection(customerConnect);
		}

		else {
			CustomerConnect customerConnect = delivery.getCustomerConnection();
			customerConnect.setIsMovingIn(customerConnectDto.getIsMovingIn());
			customerConnect.setMoveInDate(customerConnectDto.getMoveInDate() != null
					? helper.toGermamUnixTimestamp(customerConnectDto.getMoveInDate())
					: null);
			customerConnect.setSubmitLater(customerConnectDto.getSubmitLater());
			customerConnect.setMeterNumber(customerConnectDto.getMeterNumber());
			customerConnect.setCurrentProvider(customerConnectDto.getCurrentProvider());
			customerConnect.setAutoCancellation(customerConnectDto.getAutoCancellation());
			customerConnect.setAlreadyCancelled(customerConnectDto.getAlreadyCancelled());
			customerConnect.setSelfCancellation(customerConnectDto.getSelfCancellation());
			customerConnect.setDelivery(customerConnectDto.getDelivery());
			customerConnect.setDesiredDelivery(customerConnectDto.getDesiredDelivery() != null
					? helper.toGermamUnixTimestamp(customerConnectDto.getDesiredDelivery())
					: null);

			customerConnect.setMarketLocationId(customerConnectDto.getMarketLocationId());
		}

		customerDeliveryRepo.save(delivery);
		return Map.of("res", true, "customerId", customerId, "deliveryId", deliveryId);
	}

	@Transactional
	public Map<String, Object> savePayment(CustomerPaymentRequestDto paymentDto) {

		if (paymentDto.getCustomerId() == null || paymentDto.getCustomerId() <= 0)
			throw new InternalServerException("Customer id missing", HttpStatus.OK);

		if (paymentDto.getDeliveryId() == null || paymentDto.getDeliveryId() <= 0)
			throw new InternalServerException("Delivery id missing", HttpStatus.OK);

		CustomerDelivery delivery = customerDeliveryRepo.findById(paymentDto.getDeliveryId())
				.orElseThrow(() -> new InternalServerException("Customer delivery not found", HttpStatus.OK));

		if (paymentDto.getPaymentData() == null)
			throw new InternalServerException("Payment details missing", HttpStatus.OK);

		PaymentDto paymentDetails = paymentDto.getPaymentData();

		if (paymentDetails.getPaymentMethod() == null || paymentDetails.getPaymentMethod().isEmpty())
			throw new InternalServerException("Payment method missing", HttpStatus.OK);

		AccountHolderDto account = new AccountHolderDto();

		if (!paymentDetails.getPaymentMethod().equals("ueberweisung")) {

			if (paymentDetails.getIban() == null || paymentDetails.getIban().isEmpty())
				throw new InternalServerException("Iban missing", HttpStatus.OK);

			electricityComparisonService.checkIban(paymentDetails.getIban());

			if (paymentDetails.getAccountHolder() == null)
				throw new InternalServerException("Account holder data missing", HttpStatus.OK);

			if (paymentDetails.getSepaConsent() == null)
				throw new InternalServerException("Sepa Consent missing", HttpStatus.OK);

			account = paymentDetails.getAccountHolder();

			if (account == null || account.getFirstName() == null || account.getLastName() == null
					|| account.getFirstName().isEmpty() || account.getLastName().isEmpty())
				throw new InternalServerException("Account holder details missing", HttpStatus.OK);
		}

		if (delivery.getCustomerPayment() == null) {

			CustomerPayment payment = CustomerPayment.builder().paymentMethod(paymentDetails.getPaymentMethod())
					.iban(paymentDetails.getIban()).accountHolderFirstName(account.getFirstName())
					.accountHolderLastName(account.getLastName()).customerDeliveryId(delivery)
					.sepaConsent(paymentDetails.getSepaConsent()).build();

			delivery.setCustomerPayment(payment);

		} else {
			CustomerPayment payment = delivery.getCustomerPayment();

			payment.setPaymentMethod(paymentDetails.getPaymentMethod());
			payment.setIban(paymentDetails.getIban());
			payment.setAccountHolderFirstName(account.getFirstName());
			payment.setAccountHolderLastName(account.getLastName());
			payment.setSepaConsent(paymentDetails.getSepaConsent());
		}

		customerDeliveryRepo.save(delivery);

		return Map.of("res", true, "customerId", paymentDto.getCustomerId(), "deliveryId", delivery.getId());
	}

	public Map<String, Object> fetchByStep(Integer customerId, Integer deliveryId, Integer step) {

		if (customerId == null || customerId <= 0)
			throw new InternalServerException("Customer id missing", HttpStatus.OK);

		if (deliveryId == null || deliveryId <= 0)
			throw new InternalServerException("Delivery id missing", HttpStatus.OK);

		if (step < 0 || step > 4)
			throw new InternalServerException("Invalid step", HttpStatus.OK);

		Customer customer = customerRepo.findById(customerId)
				.orElseThrow(() -> new InternalServerException("Customer not found", HttpStatus.OK));
		CustomerDelivery delivery = customerDeliveryRepo.findById(deliveryId)
				.orElseThrow(() -> new InternalServerException("Delivery details not found", HttpStatus.OK));

		if (customer.getCustomerId() != delivery.getCustomerId().getCustomerId())
			throw new InternalServerException("Customer id wirh delivery mismatch", HttpStatus.OK);

		if (step == 0)
			return Map.of("res", true, "data", delivery, "message", "All details for step 0");

		else if (step == 2)
			return Map.of("res", true, "data", CustomerDeliveryResponseDto.mapResponse(delivery), "message",
					"All details for step 2");
		else if (step == 3)
			return Map.of("res", true, "data", delivery.getCustomerConnection(), "message", "All details for step 3");
		else if (step == 4)
			return Map.of("res", true, "data", delivery.getCustomerPayment(), "message", "All details for step 4");
		else
			return Map.of("res", false, "message", "Enter a valid form step");
	}

	@Transactional
	public Map<String, Object> submit(Integer customerId, Integer deliveryId) {
		if (deliveryId == null || deliveryId <= 0)
			throw new InternalServerException("Delivery id missing", HttpStatus.OK);

		CustomerDelivery delivery = customerDeliveryRepo.findById(deliveryId)
				.orElseThrow(() -> new InternalServerException("Customer Delivery details not found", HttpStatus.OK));
		if (delivery.getCustomerId().getCustomerId() != customerId)
			throw new InternalServerException("Customer and delivery mis-match", HttpStatus.OK);

		delivery.setOrderPlaced(true);
		delivery.setOrderPlacedOn(Helper.getCurrentTimeBerlin());

		customerDeliveryRepo.save(delivery);

		return Map.of("res", true, "message", "Order placed");
	}

	@Transactional
	public Map<String, Object> submitCustomerSchedule(CustomerContactScheduleRequestDto schedule) {
		if (schedule.getDeliveryId() == null || schedule.getDeliveryId() <= 0)
			throw new InternalServerException("Delivery id missing", HttpStatus.OK);

		if (schedule.getDayOfWeek() == null || schedule.getDayOfWeek().isEmpty())
			throw new InternalServerException("Day of the week missing", HttpStatus.OK);

		if (schedule.getTimeSlot() == null || schedule.getTimeSlot().isEmpty())
			throw new InternalServerException("Time slot missing", HttpStatus.OK);

		BigInteger startTime = helper.toGermanTimestampWithDynamicTime(schedule.getScheduleDate(), 0, 0);
		DayOfWeek dayName = schedule.getScheduleDate().atStartOfDay(ZoneId.of("Europe/Berlin")).getDayOfWeek();

		if (listOfHolidaysRepo.existsByAdminAdminIdAndStartDate(schedule.getAdminId(), startTime)
				|| dayName.equals(DayOfWeek.SUNDAY)) {
			LocalDate start = schedule.getScheduleDate();

			List<LocalDate> holidayDates = new LinkedList<LocalDate>();

			while (listOfHolidaysRepo.existsByAdminAdminIdAndStartDate(schedule.getAdminId(), startTime)
					|| Instant.ofEpochSecond(startTime.longValue()).atZone(ZoneId.of("Europe/Berlin")).getDayOfWeek()
							.equals(DayOfWeek.SUNDAY)) {
				holidayDates.add(start);
				start = start.plusDays(1);
				startTime = helper.toGermanTimestampWithDynamicTime(start, 0, 0);
			}

			startTime = helper.toGermanTimestampWithDynamicTime(start, 0, 0);

			Map<String, Object> dateFormatter = Helper.getLocalDateTimeFromBigInteger(startTime);

			return Map.of("res", false, "nextDate", start, "nextDay", dateFormatter.get("dayOfWeek"), "errorMessage",
					"Selected day is a holiday", "holidayDated", holidayDates);

		}

		CustomerDelivery delivery = customerDeliveryRepo.findById(schedule.getDeliveryId())
				.orElseThrow(() -> new InternalServerException("Customer delivery details missing", HttpStatus.OK));

		CustomerContactSchedule customerSchedule = CustomerContactSchedule.builder().dayOfWeek(schedule.getDayOfWeek())
				.timeSlot(schedule.getTimeSlot()).description(schedule.getDescription()).customerDelivery(delivery)
				.scheduleDate(startTime).build();
		delivery.setCustomerSchedule(customerSchedule);

		customerDeliveryRepo.save(delivery);

		return Map.of("res", true, "message", "Schedule submitted");
	}

	public Map<String, Object> fetchCustomer(Integer id) {
		if (id == null || id <= 0)
			throw new InternalServerException("Customer id missing", HttpStatus.OK);

		Customer customer = customerRepo.findById(id)
				.orElseThrow(() -> new InternalServerException("Customer not found", HttpStatus.OK));

		return Map.of("res", true, "data", customerResponseMapper.toResponseDto(customer));
	}

	public Map<String, Object> sendUnsignedDocumentByEmail(Integer adminId, Integer customerId) {

		if (adminId == null || customerId <= 0)
			throw new InternalServerException("Admin id missing", HttpStatus.OK);
		if (customerId == null || customerId <= 0)
			throw new InternalServerException("Customer id missing", HttpStatus.OK);

		Customer customer = customerRepo.findByCustomerIdAndAdminAdminId(customerId, adminId).orElseThrow(
				() -> new InternalServerException("Customer not found with this credentials", HttpStatus.OK));

		String to = customer.getEmail();
		String body = "Please find your signed contract attached.";
		String subject = "Signed pdf";

		String relativePath = "customer-unsigned-documents/file-sample_150kB.pdf";

		String absolutePath = fileServiceCustomer.getAbsolutePath(relativePath);
		
		mailService.sendMailWithAttachment(to, subject, body, absolutePath);

		return Map.of("res", true, "message", "Mail sended successfully");
	}

}
