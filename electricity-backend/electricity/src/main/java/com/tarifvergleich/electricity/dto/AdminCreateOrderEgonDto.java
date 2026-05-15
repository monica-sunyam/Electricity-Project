package com.tarifvergleich.electricity.dto;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonValue;
import com.tarifvergleich.electricity.model.CustomerAddress;
import com.tarifvergleich.electricity.model.CustomerBillingAddress;
import com.tarifvergleich.electricity.model.CustomerConnect;
import com.tarifvergleich.electricity.model.CustomerDelivery;
import com.tarifvergleich.electricity.model.CustomerPayment;
import com.tarifvergleich.electricity.model.CustomerSelectedProvider;
import com.tarifvergleich.electricity.util.Helper;

public record AdminCreateOrderEgonDto(EgonAddressDto delivery, EgonAddressDto billing,
		EgonContactPersonDto contactPerson, EgonContactDetailsDto contact, Object payment,
		List<EgonProductDto> products) {

	public static AdminCreateOrderEgonDto mapToEgonRequest(CustomerDelivery delivery, String deliveryType) {

		CustomerAddress customerAddress = delivery.getAddress();
		CustomerBillingAddress customerBillingAddress = delivery.getBillingAddress();
		CustomerConnect customerConnection = delivery.getCustomerConnection();
		CustomerPayment customerPayment = delivery.getCustomerPayment();
		CustomerSelectedProvider provider = delivery.getCustomerProvider();
		Helper helper = new Helper();

		EgonAddressDto address = new EgonAddressDto("81", customerAddress.getZip(), customerAddress.getCity(),
				customerAddress.getStreet(), customerAddress.getHouseNumber());

		EgonAddressDto billingAddress = new EgonAddressDto("81", customerBillingAddress.getZip(),
				customerBillingAddress.getCity(), customerBillingAddress.getStreet(),
				customerBillingAddress.getHouseNumber());

		EgonContactPersonDto contactPerson = new EgonContactPersonDto(delivery.getSalutation(), delivery.getFirstName(),
				delivery.getLastName(), helper.toGermalDateStamp(delivery.getDob()));

		EgonContactDetailsDto contact = new EgonContactDetailsDto("49", delivery.getMobile().replace("+", ""),
				delivery.getCustomerId().getEmail());

		Object payment;

		if (customerPayment.getIban() != null)
			payment = new EgonPaymentSepaDto("sepa", customerPayment.getIban());
		else
			payment = new EgonPaymentDebitDto("debit");

		EgonProductDto product = new EgonProductDto(provider.getRateId(),
				helper.toGermalDateStamp(Helper.getCurrentTimeBerlin()), delivery.getTotalConsumption(),
				provider.getType(), provider.getBranch(), deliveryType,
				LocalDate.of(2026, 7, 1).atStartOfDay(ZoneId.of("Europe/Berlin")).toLocalDate(), 0,
				customerConnection.getMeterNumber(), customerConnection.getMarketLocationId(), provider.getWorkPrice(),
				provider.getBasePriceYear());

		return new AdminCreateOrderEgonDto(address, billingAddress, contactPerson, contact, payment, List.of(product));
	}

	public static record OrderListResponse(@JsonValue List<OrderResponseDto> orders) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static record OrderResponseDto(String orderNo, boolean docInboxRequired, String providerName,
			String rateName, String orderType, String customerType) {
	}

}

record EgonAddressDto(String country, String zip, String city, String street, String houseNumber) {
}

record EgonContactPersonDto(String salutation, String firstName, String lastName, LocalDate birthday) {
}

record EgonContactDetailsDto(String phonePrefix, String phoneNumber, String email) {
}

record EgonPaymentSepaDto(String paymentType, String iban) {
}

record EgonPaymentDebitDto(String paymentType) {
}

record EgonProductDto(Long rateId, LocalDate sigDate, int consum, String type, String branch, String deliveryType,
		LocalDate deliveryDate, int fastDelivery, String counterNumber, String marketLocationId, double workPrice,
		double basePriceYear) {
}

@JsonIgnoreProperties(ignoreUnknown = true)
record OrderResponseDto(int orderNo, boolean docInboxRequired, String providerName, String rateName, String orderType,
		String customerType) {
}
