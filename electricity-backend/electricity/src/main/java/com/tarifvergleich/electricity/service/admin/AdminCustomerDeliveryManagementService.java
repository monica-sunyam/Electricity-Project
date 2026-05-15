package com.tarifvergleich.electricity.service.admin;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarifvergleich.electricity.dto.AdminCreateOrderEgonDto;
import com.tarifvergleich.electricity.dto.AdminCreateOrderEgonDto.OrderListResponse;
import com.tarifvergleich.electricity.dto.CustomerBillingRequestDto;
import com.tarifvergleich.electricity.dto.CustomerConnectionRequestDto;
import com.tarifvergleich.electricity.dto.CustomerDeliveryDto;
import com.tarifvergleich.electricity.dto.CustomerDeliveryRequestWrapper.AdminEditCustomerDeliveryRelated;
import com.tarifvergleich.electricity.dto.CustomerOrderDto;
import com.tarifvergleich.electricity.dto.CustomerPaymentRequestDto;
import com.tarifvergleich.electricity.dto.CustomerPaymentRequestDto.PaymentDto;
import com.tarifvergleich.electricity.dto.EgonFileSignatureResponse;
import com.tarifvergleich.electricity.dto.EgonFileSignatureResponse.EgonDocumentDto;
import com.tarifvergleich.electricity.dto.EgonFileSignatureResponse.EgonFileSignatureRequest;
import com.tarifvergleich.electricity.dto.EnergyRateDto;
import com.tarifvergleich.electricity.exception.InternalServerException;
import com.tarifvergleich.electricity.model.AdminSignature;
import com.tarifvergleich.electricity.model.CustomerBookingDocument;
import com.tarifvergleich.electricity.model.CustomerConnect;
import com.tarifvergleich.electricity.model.CustomerContractSignature;
import com.tarifvergleich.electricity.model.CustomerDelivery;
import com.tarifvergleich.electricity.model.CustomerOrder;
import com.tarifvergleich.electricity.model.CustomerPayment;
import com.tarifvergleich.electricity.model.CustomerSelectedProvider;
import com.tarifvergleich.electricity.repository.AdminSignatureRepository;
import com.tarifvergleich.electricity.repository.CustomerBookingDocumentRepository;
import com.tarifvergleich.electricity.repository.CustomerDeliveryRepository;
import com.tarifvergleich.electricity.repository.CustomerOrderRepository;
import com.tarifvergleich.electricity.service.ElectricityComparisonService;
import com.tarifvergleich.electricity.service.EnergyService;
import com.tarifvergleich.electricity.service.customer.CustomerBookingService;
import com.tarifvergleich.electricity.util.FileServiceCustomer;
import com.tarifvergleich.electricity.util.FileServiceSuperAdmin;
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
	private final EnergyService energyService;
	private final FileServiceCustomer fileServiceCustomer;
	private final FileServiceSuperAdmin fileServiceSuperAdmin;
	private final CustomerOrderRepository customerOrderRepo;
	private final CustomerBookingDocumentRepository customerBookingDocumentRepo;
	private final AdminSignatureRepository adminSignatureRepo;

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
		CustomerDeliveryDto newDeliveryDetails = deliveryDetails.getDelivery();
		CustomerBillingRequestDto billingAddress = deliveryDetails.getBillingAddress();
		CustomerConnectionRequestDto newCustomerConnection = deliveryDetails.getConnection();
		CustomerPaymentRequestDto newCustomerPayment = deliveryDetails.getPaymentDetails();
		EnergyRateDto newCustomerSelectedProvider = deliveryDetails.getProvider();

		Map<String, Object> deliveryResponse = customerBookingService.saveDelivery(customerId, null, newDeliveryDetails,
				billingAddress, newCustomerSelectedProvider);

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

		CustomerDelivery delivery = customerDeliveryRepo.findById(deliveryId)
				.orElseThrow(() -> new InternalServerException("Failed to create order", HttpStatus.OK));

		delivery.setOrderPlaced(true);

		customerDeliveryRepo.save(delivery);

		if (!(Boolean) paymentResponse.get("res"))
			throw new RuntimeException();

		return Map.of("res", true, "deliveryId", deliveryId);
	}

	@Transactional
	public Map<String, Object> placeNewOrderToEgon(CustomerOrderDto customerOrderDto) {
		if (customerOrderDto.getAdminId() == null || customerOrderDto.getAdminId() <= 0)
			throw new InternalServerException("Admin id missing", HttpStatus.OK);
		if (customerOrderDto.getCustomerOrderId() == null || customerOrderDto.getCustomerOrderId() <= 0)
			throw new InternalServerException("Customer order id missing", HttpStatus.OK);

		CustomerOrder order = customerOrderRepo
				.findByIdAndAdminAdminId(customerOrderDto.getCustomerOrderId(), customerOrderDto.getAdminId())
				.orElseThrow(() -> new InternalServerException("Order record not found with this credential",
						HttpStatus.OK));

		CustomerDelivery delivery = order.getDelivery();

//		CustomerSelectedProvider provider = delivery.getCustomerProvider();
//
//		LocalDate expiry;
//		BigInteger totalTerm;
//
//		try {
//			expiry = helper.flexibleDateParser(provider.getRaw().get("optTerm").asText())
//					.atStartOfDay(ZoneId.of("Europe/Berlin")).minusDays(1).toLocalDate();
//		} catch (DateTimeParseException | IllegalArgumentException e) {
//			Long expireDuration = provider.getRaw().get("optTerm").asLong();
//			expiry = LocalDate.now().atStartOfDay().atZone(ZoneId.of("Europe/Berlin")).plusMonths(expireDuration)
//					.minusDays(1).toLocalDate();
//		}
//
//		totalTerm = BigInteger.valueOf(ChronoUnit.SECONDS.between(expiry.atStartOfDay(ZoneId.of("Europe/Berlin")),
//				ZonedDateTime.now(ZoneId.of("Europe/Berlin"))));
//
//		BigInteger cancelTime = BigInteger.valueOf(0);
//		if (provider.getRaw().path("cancel") != null && provider.getRaw().path("cancelType") != null) {
//			Integer cancel = provider.getRaw().path("cancel").asInt();
//			Integer cancelType = provider.getRaw().path("cancelType").asInt();
//			BigInteger expiryBigInt = helper.toGermamUnixTimestamp(expiry);
//
//			if (cancelType.equals(0))
//				cancelTime = expiryBigInt.subtract(helper.getSecondValueOfDuration(0, 0, 0, 0, 0, 0));
//			else if (cancelType.equals(1))
//				cancelTime = expiryBigInt.subtract(helper.getSecondValueOfDuration(0, 0, cancel, 0, 0, 0));
//			else if (cancelType.equals(2))
//				cancelTime = expiryBigInt.subtract(helper.getSecondValueOfDuration(0, 0, cancel * 7, 0, 0, 0));
//			else if (cancelType.equals(3))
//				cancelTime = expiryBigInt.subtract(helper.getSecondValueOfDuration(0, cancel, 0, 0, 0, 0));
//		}

		/* Map egon place order payload */
		AdminCreateOrderEgonDto placeOrderRequest = AdminCreateOrderEgonDto.mapToEgonRequest(delivery, "new");

		OrderListResponse placeOrderResponse = energyService.placeOrder(placeOrderRequest);

		Long orderNo = Long.parseLong(placeOrderResponse.orders().getFirst().orderNo());

		order.setAdminPlacedOrderOn(Helper.getCurrentTimeBerlin());
		order.setAdminPlacedOrder(true);
		order.setOrderId(orderNo);
//		order.setExpiryOn(helper.toGermamUnixTimestamp(expiry));
//		order.setLastDateOfCancellation(cancelTime);
//		order.setOperationPeriod(totalTerm);

		delivery.setOrderNo(orderNo);
		delivery.setOrderPlacedInEgon(true);
//		delivery.setExpiryOn(helper.toGermamUnixTimestamp(expiry));
//		delivery.setLastDateOfCancellation(cancelTime);

		order.setDelivery(delivery);

		customerOrderRepo.save(order);

		customerOrderDto.setOrderId(orderNo);

//		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
//			@Override
//			public void afterCommit() {
//				asyncServiceAdmin.downloadUnsignedPdf(customerOrderDto);
//			}
//		});

		return Map.of("res", true, "message", "Order placed successfully", "Order no", orderNo);
	}

	@Transactional
	public Map<String, Object> getSignedPdfFromEgon(CustomerOrderDto customerOrderDto) {
		if (customerOrderDto.getAdminId() == null || customerOrderDto.getAdminId() <= 0)
			throw new InternalServerException("Admin id missing", HttpStatus.OK);
		if (customerOrderDto.getCustomerOrderId() == null || customerOrderDto.getCustomerOrderId() <= 0)
			throw new InternalServerException("Customer order id missing", HttpStatus.OK);

		CustomerOrder order = customerOrderRepo
				.findByIdAndAdminAdminId(customerOrderDto.getCustomerOrderId(), customerOrderDto.getAdminId())
				.orElseThrow(() -> new InternalServerException("Order record not found with this credential",
						HttpStatus.OK));

		if (order.getOrderId() == null || order.getOrderId() <= 0)
			throw new InternalServerException("Order is not placed", HttpStatus.OK);

		if (order.getCustomerBookingDocument() != null)
			throw new InternalServerException("Contract already signed", HttpStatus.OK);

		AdminSignature adminSignature = adminSignatureRepo.findByAdminAdminId(customerOrderDto.getAdminId())
				.orElseThrow(() -> new InternalServerException("Admin signature not found with this credential",
						HttpStatus.OK));

		if (adminSignature.getFilePath().isEmpty())
			throw new InternalServerException("Admin signature not found", HttpStatus.OK);

		String fetchAdminSignature = fileServiceSuperAdmin.relativeToBase64(adminSignature.getFilePath());

		CustomerDelivery delivery = order.getDelivery();

		CustomerContractSignature customerSignatures = order.getCustomerContractSignature();

		if (customerSignatures == null)
			throw new InternalServerException("Customer signature is missing", HttpStatus.OK);

		String fetchSignature = "";
		String fetchSignatureBank = "";
		String fetchSignatureCustomer = "";
		String fetchSignatureDataProtection = "";
		if (customerSignatures.getSignature() != null && !customerSignatures.getSignature().isEmpty())
			fetchSignature = fileServiceCustomer.relativeToBase64(customerSignatures.getSignature());
		if (customerSignatures.getSignatureBank() != null && !customerSignatures.getSignatureBank().isEmpty())
			fetchSignatureBank = fileServiceCustomer.relativeToBase64(customerSignatures.getSignatureBank());
		if (customerSignatures.getSignatureCustomer() != null && !customerSignatures.getSignatureCustomer().isEmpty())
			fetchSignatureCustomer = fileServiceCustomer.relativeToBase64(customerSignatures.getSignatureCustomer());
		if (customerSignatures.getSignatureDataProtection() != null
				&& !customerSignatures.getSignatureDataProtection().isEmpty())
			fetchSignatureDataProtection = fileServiceCustomer
					.relativeToBase64(customerSignatures.getSignatureDataProtection());

		CustomerBookingDocument bookingDoc = CustomerBookingDocument.builder().orderNo(delivery.getOrderNo())
				.customer(delivery.getCustomerId()).customerDelivery(delivery).admin(delivery.getAdmin())
				.customerOrder(order).build();

		EgonFileSignatureRequest signature = EgonFileSignatureResponse.mapSignatures(fetchSignature, fetchSignatureBank,
				fetchAdminSignature, fetchSignatureCustomer, fetchSignatureDataProtection);

		EgonDocumentDto egonBookingResponse = energyService.createBookingPdf(delivery.getOrderNo().toString(),
				signature);

		String fileName = delivery.getFirstName() + "_" + delivery.getLastName() + "_" + delivery.getUniqueDeliveryId();

		try {
			String signedContractFilePath = fileServiceCustomer.saveBase64Pdf(egonBookingResponse.file(), fileName,
					"customer-signed-documents");
			bookingDoc.setSignedOriginalFileName(fileName);
			bookingDoc.setSignedFileUrl(signedContractFilePath);
			bookingDoc.setSignedDocumentSubmitted(true);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}

		bookingDoc = customerBookingDocumentRepo.save(bookingDoc);

		delivery.setCustomerBookingDocument(bookingDoc);
		order.setCustomerBookingDocument(bookingDoc);
		order.setDelivery(delivery);

		customerOrderRepo.save(order);

		String signedPdfAbsolutePath = fileServiceCustomer.getAbsolutePath(bookingDoc.getSignedFileUrl());

		if (signedPdfAbsolutePath == null || signedPdfAbsolutePath.isEmpty())
			throw new InternalServerException("Fail to convert url", HttpStatus.OK);

		return Map.of("res", true, "signedPdfUrl", signedPdfAbsolutePath);
	}

	@Transactional
	public Map<String, Object> openOrder(CustomerDeliveryDto deliveryDto) {

		if (deliveryDto.getAdminId() == null || deliveryDto.getAdminId() <= 0)
			throw new InternalServerException("Admin id missing", HttpStatus.OK);

		if (deliveryDto.getDeliveryId() == null || deliveryDto.getDeliveryId() <= 0)
			throw new InternalServerException("Delivery id missing", HttpStatus.OK);

		CustomerDelivery delivery = customerDeliveryRepo
				.findByIdAndAdminAdminId(deliveryDto.getDeliveryId(), deliveryDto.getAdminId()).orElseThrow(
						() -> new InternalServerException("Delivery not found with this credential", HttpStatus.OK));

		if (delivery.getCustomerOrder() != null)
			return Map.of("res", true, "customerOrderId", delivery.getCustomerOrder().getId());

		CustomerOrder newOrder = CustomerOrder.builder().delivery(delivery).customer(delivery.getCustomerId())
				.admin(delivery.getAdmin()).build();

		newOrder = customerOrderRepo.save(newOrder);

		return Map.of("res", true, "customerOrderId", newOrder.getId());
	}

	@Transactional
	public Map<String, Object> uploadSignedPdf(CustomerOrderDto customerOrderDto, MultipartFile file) {

		if (customerOrderDto.getAdminId() == null || customerOrderDto.getAdminId() <= 0)
			throw new InternalServerException("Admin id missing", HttpStatus.OK);
		if (customerOrderDto.getCustomerOrderId() == null || customerOrderDto.getCustomerOrderId() <= 0)
			throw new InternalServerException("Customer order id missing", HttpStatus.OK);

		if (file == null)
			throw new InternalServerException("File missing", HttpStatus.OK);

		CustomerOrder order = customerOrderRepo
				.findByIdAndAdminAdminId(customerOrderDto.getCustomerOrderId(), customerOrderDto.getAdminId())
				.orElseThrow(() -> new InternalServerException("Customer order not found with this credential",
						HttpStatus.OK));
		if (order.getCustomerBookingDocument() == null)
			throw new InternalServerException("Previous record of unsigned document not found", HttpStatus.OK);

		CustomerBookingDocument bookingDocument = order.getCustomerBookingDocument();

		String base64File = helper.convertToBase64(file);

		String filePath = fileServiceCustomer.saveFile(file, "customer-signed-documents");

		if (filePath == null)
			throw new InternalServerException("Error in saving document", HttpStatus.OK);

		bookingDocument.setSignedDocumentSubmitted(true);
		bookingDocument.setSignedFileUrl(filePath);
		bookingDocument.setSignedFileUrl(file.getOriginalFilename());

		customerBookingDocumentRepo.save(bookingDocument);

		return Map.of("res", true, "message", "Customer signed document uploaded successfully");
	}

}
