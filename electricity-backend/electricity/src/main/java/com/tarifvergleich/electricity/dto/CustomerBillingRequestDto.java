package com.tarifvergleich.electricity.dto;

import com.tarifvergleich.electricity.model.CustomerBillingAddress;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerBillingRequestDto {

	private String zip;
	private String city;
	private String street;
	private String houseNumber;
	private Boolean different;
	
	public static CustomerBillingRequestDto getCustomerBillingResponseDto(CustomerBillingAddress customerBilling) {
		return CustomerBillingRequestDto.builder()
				.zip(customerBilling.getZip())
				.city(customerBilling.getCity())
				.street(customerBilling.getStreet())
				.houseNumber(customerBilling.getHouseNumber())
				.different(customerBilling.getIsDifferent())
				.build();
	}
}
