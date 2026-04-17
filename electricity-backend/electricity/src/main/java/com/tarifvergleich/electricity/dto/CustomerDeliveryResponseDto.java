package com.tarifvergleich.electricity.dto;

import java.math.BigInteger;

import com.tarifvergleich.electricity.dto.CustomerConnectionRequestDto.CustomerConnectionResponse;
import com.tarifvergleich.electricity.dto.CustomerContactScheduleRequestDto.CustomerContactScheduleResponse;
import com.tarifvergleich.electricity.dto.CustomerPaymentRequestDto.CustomerPaymentResponse;
import com.tarifvergleich.electricity.model.CustomerDelivery;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerDeliveryResponseDto {

	private Integer deliveryId;
	private String email;
	private String title;
	private String firstName;
	private String lastName;
	private String mobile;
	private String telephone;
	private BigInteger deliveryDate;
	private BigInteger orderPlacedOn;
	private Boolean orderPlaced;

	private CustomerAddressRes customerAddress;
	private CustomerAddressRes billingAddress;
	private EnergyRateDto provider;

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class CustomerAddressRes {
		private String zip;
		private String city;
		private String street;
		private String houseNumber;
		private Boolean isDifferent;

	}
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class CustomerDeliveryResponseAll{
		private Integer deliveryId;
		private String email;
		private String title;
		private String firstName;
		private String lastName;
		private String mobile;
		private String telephone;
		private BigInteger deliveryDate;
		private BigInteger orderPlacedOn;
		private Boolean orderPlaced;

		private CustomerAddressRes customerAddress;
		private CustomerAddressRes billingAddress;
		private EnergyRateDto provider;
		
		private CustomerConnectionResponse  connection;
		private CustomerPaymentResponse payment;
		private CustomerContactScheduleResponse contactSchedule;
	}
	

	public static CustomerDeliveryResponseDto mapResponse(CustomerDelivery delivery) {
		return CustomerDeliveryResponseDto.builder().deliveryId(delivery.getId())
				.email(delivery.getCustomerId().getEmail()).title(delivery.getTitle())
				.firstName(delivery.getFirstName()).lastName(delivery.getLastName()).mobile(delivery.getMobile())
				.telephone(delivery.getTelephone()).deliveryDate(delivery.getDeliveryDate())
				.customerAddress(CustomerAddressRes.builder().zip(delivery.getAddress().getZip())
						.city(delivery.getAddress().getCity()).street(delivery.getAddress().getStreet())
						.houseNumber(delivery.getAddress().getHouseNumber()).build())
				.billingAddress(CustomerAddressRes.builder().zip(delivery.getBillingAddress().getZip())
						.city(delivery.getBillingAddress().getCity()).street(delivery.getBillingAddress().getStreet())
						.houseNumber(delivery.getBillingAddress().getHouseNumber())
						.isDifferent(delivery.getBillingAddress().getIsDifferent()).build())
				.orderPlaced(delivery.getOrderPlaced())
				.orderPlacedOn(delivery.getOrderPlacedOn())
				.provider(EnergyRateDto.getProviderResponse(delivery.getCustomerProvider())).build();
	}
	
	public static CustomerDeliveryResponseAll getDeliveryResponse(CustomerDelivery delivery) {
		return CustomerDeliveryResponseAll.builder()
				.deliveryId(delivery.getId())
				.email(delivery.getCustomerId().getEmail()).title(delivery.getTitle())
				.firstName(delivery.getFirstName()).lastName(delivery.getLastName()).mobile(delivery.getMobile())
				.telephone(delivery.getTelephone()).deliveryDate(delivery.getDeliveryDate())
				.customerAddress(CustomerAddressRes.builder().zip(delivery.getAddress().getZip())
						.city(delivery.getAddress().getCity()).street(delivery.getAddress().getStreet())
						.houseNumber(delivery.getAddress().getHouseNumber()).build())
				.billingAddress(CustomerAddressRes.builder().zip(delivery.getBillingAddress().getZip())
						.city(delivery.getBillingAddress().getCity()).street(delivery.getBillingAddress().getStreet())
						.houseNumber(delivery.getBillingAddress().getHouseNumber())
						.isDifferent(delivery.getBillingAddress().getIsDifferent()).build())
				.orderPlaced(delivery.getOrderPlaced())
				.orderPlacedOn(delivery.getOrderPlacedOn())
				.provider(EnergyRateDto.getProviderResponse(delivery.getCustomerProvider()))
				.connection(CustomerConnectionRequestDto.getConnectionResponse(delivery.getCustomerConnection()))
				.payment(CustomerPaymentRequestDto.getCustomerPaymentResponse(delivery.getCustomerPayment()))
				.contactSchedule(CustomerContactScheduleRequestDto.getContactScheduleResponse(delivery.getCustomerSchedule()))
				.build();
	}
}
