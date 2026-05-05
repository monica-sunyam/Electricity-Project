package com.tarifvergleich.electricity.service.admin;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarifvergleich.electricity.dto.CustomerBillingRequestDto;
import com.tarifvergleich.electricity.dto.CustomerConnectionRequestDto;
import com.tarifvergleich.electricity.dto.CustomerDeliveryDto;
import com.tarifvergleich.electricity.dto.CustomerDeliveryRequestWrapper.AdminEditCustomerDeliveryRelated;
import com.tarifvergleich.electricity.dto.CustomerPaymentRequestDto;
import com.tarifvergleich.electricity.dto.CustomerPaymentRequestDto.PaymentDto;
import com.tarifvergleich.electricity.dto.EnergyRateDto;
import com.tarifvergleich.electricity.exception.InternalServerException;
import com.tarifvergleich.electricity.model.CustomerConnect;
import com.tarifvergleich.electricity.model.CustomerDelivery;
import com.tarifvergleich.electricity.model.CustomerPayment;
import com.tarifvergleich.electricity.model.CustomerSelectedProvider;
import com.tarifvergleich.electricity.repository.CustomerDeliveryRepository;
import com.tarifvergleich.electricity.service.ElectricityComparisonService;
import com.tarifvergleich.electricity.service.customer.CustomerBookingService;
import com.tarifvergleich.electricity.util.Helper;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminCustomerDeliveryManagementService {

	private final CustomerDeliveryRepository customerDeliveryRepo;
	private final Helper helper;
	private final ElectricityComparisonService electricityComparisonService;
	private final ObjectMapper objectMapper;
	private final CustomerBookingService customerBookingService;

	@Transactional
	public Map<String, Object> editDeliveryDetailsByAdmin(AdminEditCustomerDeliveryRelated deliveryDetails) {
		if (deliveryDetails == null)
			throw new InternalServerException("No details found for edit", HttpStatus.OK);
		if (deliveryDetails.getAdminId() == null || deliveryDetails.getAdminId() <= 0)
			throw new InternalServerException("Admin id missing", HttpStatus.OK);

		if (deliveryDetails.getDeliveryId() == null || deliveryDetails.getDeliveryId() <= 0)
			throw new InternalServerException("Delivery id missing", HttpStatus.OK);

		CustomerDeliveryDto editDeliveryDetails = deliveryDetails.getDelivery();
		CustomerConnectionRequestDto editCustomerConnection = deliveryDetails.getConnection();
		CustomerPaymentRequestDto editCustomerPayment = deliveryDetails.getPaymentDetails();
		EnergyRateDto editCustomerSelectedProvider = deliveryDetails.getProvider();

		CustomerDelivery customerDelivery = customerDeliveryRepo
				.findByIdAndAdminAdminId(deliveryDetails.getDeliveryId(), deliveryDetails.getAdminId())
				.orElseThrow(() -> new InternalServerException("Customer Delivery not found with this credential",
						HttpStatus.OK));

		CustomerConnect connection = customerDelivery.getCustomerConnection();
		CustomerPayment payment = customerDelivery.getCustomerPayment();
		CustomerSelectedProvider provider = customerDelivery.getCustomerProvider();

		/* Edit Customer Delivery */

		if (editDeliveryDetails != null) {

			if (editDeliveryDetails.getTitle() != null && !editDeliveryDetails.getTitle().isEmpty())
				customerDelivery.setTitle(editDeliveryDetails.getTitle());
			if (editDeliveryDetails.getFirstName() != null && !editDeliveryDetails.getFirstName().isEmpty())
				customerDelivery.setFirstName(editDeliveryDetails.getFirstName());
			if (editDeliveryDetails.getLastName() != null && !editDeliveryDetails.getLastName().isEmpty())
				customerDelivery.setLastName(editDeliveryDetails.getLastName());
			if (editDeliveryDetails.getSalutation() != null && !editDeliveryDetails.getSalutation().isEmpty())
				customerDelivery.setSalutation(editDeliveryDetails.getSalutation());
			if (editDeliveryDetails.getMobile() != null && !editDeliveryDetails.getMobile().isEmpty())
				customerDelivery.setMobile(editDeliveryDetails.getMobile());
			if (editDeliveryDetails.getDob() != null) {

				LocalDate todayInBerlin = LocalDate.now(ZoneId.of("Europe/Berlin"));
				LocalDate eighteenYearsAgo = todayInBerlin.minusYears(18);

				if (editDeliveryDetails.getDob().isBefore(eighteenYearsAgo))
					customerDelivery.setDob(helper.toGermamUnixTimestamp(editDeliveryDetails.getDob()));
			}

		}

		/* Edit Connection Details */

		if (editCustomerConnection != null && connection != null) {

			if (editCustomerConnection.getIsMovingIn() != null && editCustomerConnection.getIsMovingIn()) {

				if (editCustomerConnection.getMoveInDate() != null
						&& editCustomerConnection.getMoveInDate().isBefore(LocalDate.now(ZoneId.of("Europe/Berlin"))))
					connection.setMoveInDate(helper.toGermamUnixTimestamp(editCustomerConnection.getMoveInDate()));

			} else {
				if (editCustomerConnection.getAutoCancellation() != null)
					connection.setAutoCancellation(editCustomerConnection.getAutoCancellation());

				if (editCustomerConnection.getAlreadyCancelled() != null)
					connection.setAlreadyCancelled(editCustomerConnection.getAlreadyCancelled());

				if (editCustomerConnection.getSelfCancellation() != null)
					connection.setSelfCancellation(editCustomerConnection.getSelfCancellation());

				if (editCustomerConnection.getDelivery() != null)
					connection.setDelivery(editCustomerConnection.getDelivery());

				if (editCustomerConnection.getDelivery() != null && editCustomerConnection.getDelivery()) {
					if (editCustomerConnection.getDesiredDelivery() == null || editCustomerConnection
							.getDesiredDelivery().isBefore(LocalDate.now(ZoneId.of("Europe/Berlin"))))

						connection.setDesiredDelivery(
								helper.toGermamUnixTimestamp(editCustomerConnection.getDesiredDelivery()));

				}
			}

			if (editCustomerConnection.getMarketLocationId() != null
					&& !editCustomerConnection.getMarketLocationId().isEmpty())
				connection.setMarketLocationId(editCustomerConnection.getMarketLocationId());

			customerDelivery.setCustomerConnection(connection);
		}

		/* Edit Payment Details */

		if (editCustomerPayment != null && payment != null) {

			PaymentDto paymentDetails = editCustomerPayment.getPaymentData();

			if (paymentDetails.getPaymentMethod() != null && !paymentDetails.getPaymentMethod().isEmpty())
				payment.setPaymentMethod(paymentDetails.getPaymentMethod());

			if (!paymentDetails.getPaymentMethod().equals("ueberweisung")) {

				if (paymentDetails.getIban() != null && !paymentDetails.getIban().isEmpty()) {

					electricityComparisonService.checkIban(paymentDetails.getIban());
					payment.setIban(paymentDetails.getIban());
				}

				if (paymentDetails.getAccountHolder() != null) {

					if (paymentDetails.getAccountHolder().getFirstName() != null
							&& !paymentDetails.getAccountHolder().getFirstName().isEmpty())
						payment.setAccountHolderFirstName(paymentDetails.getAccountHolder().getFirstName());

					if (paymentDetails.getAccountHolder().getLastName() != null
							&& !paymentDetails.getAccountHolder().getLastName().isEmpty())
						payment.setAccountHolderLastName(paymentDetails.getAccountHolder().getLastName());
				}

				if (paymentDetails.getSepaConsent() != null)
					payment.setSepaConsent(paymentDetails.getSepaConsent());

			}

			customerDelivery.setCustomerPayment(payment);

		}

		/* Edit Provider */
		if (editCustomerSelectedProvider != null && provider != null) {

			if (editCustomerSelectedProvider.getBranch() != null && !editCustomerSelectedProvider.getBranch().isEmpty())
				provider.setBranch(editCustomerSelectedProvider.getBranch());

			if (editCustomerSelectedProvider.getProviderName() != null
					&& !editCustomerSelectedProvider.getProviderName().isEmpty())
				provider.setProviderName(editCustomerSelectedProvider.getProviderName());

			if (editCustomerSelectedProvider.getProviderSVGPath() != null
					&& !editCustomerSelectedProvider.getProviderSVGPath().isEmpty())
				provider.setProviderSVGPath(editCustomerSelectedProvider.getProviderSVGPath());

			if (editCustomerSelectedProvider.getRateName() != null
					&& !editCustomerSelectedProvider.getRateName().isEmpty())
				provider.setRateName(editCustomerSelectedProvider.getRateName());

			if (editCustomerSelectedProvider.getType() != null && !editCustomerSelectedProvider.getType().isEmpty())
				provider.setType(editCustomerSelectedProvider.getType());

			if (editCustomerSelectedProvider.getNetzProviderId() != null
					&& editCustomerSelectedProvider.getNetzProviderId() > 0)
				provider.setNetzProviderId(editCustomerSelectedProvider.getNetzProviderId());

			if (editCustomerSelectedProvider.getProviderId() != null
					&& editCustomerSelectedProvider.getProviderId() > 0)
				provider.setProviderId(editCustomerSelectedProvider.getProviderId());

			if (editCustomerSelectedProvider.getRateId() != null && editCustomerSelectedProvider.getRateId() > 0)
				provider.setRateId(editCustomerSelectedProvider.getRateId());

			if (editCustomerSelectedProvider.getConsumption() != null
					&& editCustomerSelectedProvider.getConsumption() > 0)
				provider.setConsumption(editCustomerSelectedProvider.getConsumption());

			if (editCustomerSelectedProvider.getWorkPrice() > 0)
				provider.setWorkPrice(editCustomerSelectedProvider.getWorkPrice());

			if (editCustomerSelectedProvider.getBasePriceYear() > 0)
				provider.setBasePriceYear(editCustomerSelectedProvider.getBasePriceYear());

			if (editCustomerSelectedProvider.getTotalPrice() > 0)
				provider.setTotalPrice(editCustomerSelectedProvider.getTotalPrice());

			if (editCustomerSelectedProvider.getTotalPriceMonth() > 0)
				provider.setTotalPriceMonth(editCustomerSelectedProvider.getTotalPriceMonth());

			if (editCustomerSelectedProvider.getTermBeforeNewMaxDate() != null && editCustomerSelectedProvider
					.getTermBeforeNewMaxDate().isBefore(helper.toGermalDateStamp(Helper.getCurrentTimeBerlin())))
				customerDelivery.setExpiryOn(
						helper.toGermamUnixTimestamp(editCustomerSelectedProvider.getTermBeforeNewMaxDate()));

			provider.setRaw(objectMapper.valueToTree(editCustomerSelectedProvider));

			customerDelivery.setCustomerProvider(provider);
		}

		customerDeliveryRepo.save(customerDelivery);

		return Map.of("res", true, "message", "Customer booking updated successfully");
	}

	@Transactional
	public Map<String, Object> addNewDeliveryByAdmin(AdminEditCustomerDeliveryRelated deliveryDetails) {

		if (deliveryDetails == null)
			throw new InternalServerException("No details found for edit", HttpStatus.OK);
		if (deliveryDetails.getAdminId() == null || deliveryDetails.getAdminId() <= 0)
			throw new InternalServerException("Admin id missing", HttpStatus.OK);
		if (deliveryDetails.getCustomerId() == null || deliveryDetails.getCustomerId() <= 0)
			throw new InternalServerException("Customer id missing", HttpStatus.OK);

		Integer customerId = deliveryDetails.getCustomerId();
		Integer adminId = deliveryDetails.getAdminId();
		CustomerDeliveryDto newDeliveryDetails = deliveryDetails.getDelivery();
		CustomerBillingRequestDto billingAddress = deliveryDetails.getBillingAddress();
		CustomerConnectionRequestDto newCustomerConnection = deliveryDetails.getConnection();
		CustomerPaymentRequestDto newCustomerPayment = deliveryDetails.getPaymentDetails();
		EnergyRateDto newCustomerSelectedProvider = deliveryDetails.getProvider();

		Map<String, Object> deliveryResponse = customerBookingService.saveDelivery(customerId, adminId,
				newDeliveryDetails, billingAddress, newCustomerSelectedProvider);

		if (!deliveryResponse.containsKey("deliveryId") || !(Boolean) deliveryResponse.get("res"))
			throw new RuntimeException();

		Integer deliveryId = (Integer) deliveryResponse.get("deliveryId");

		Map<String, Object> connectionResponse = customerBookingService.saveConnection(customerId, deliveryId,
				newCustomerConnection);

		if (!(Boolean) connectionResponse.get("res"))
			throw new RuntimeException();

		newCustomerPayment.setDeliveryId(deliveryId);
		newCustomerPayment.setCustomerId(customerId);

		Map<String, Object> paymentResponse = customerBookingService.savePayment(newCustomerPayment);

		if (!(Boolean) paymentResponse.get("res"))
			throw new RuntimeException();

		return Map.of("res", true, "deliveryId", deliveryId);
	}

}
