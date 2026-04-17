package com.tarifvergleich.electricity.dto;

import com.tarifvergleich.electricity.model.CustomerAddress;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class CustomerAddressDto {

	private Integer id;
    private String zip;
    private String city;
    private String street;
    private String houseNumber;
    private Integer customerId;
    
    
    public static CustomerAddressDto getCustomerAddressResponseDto(CustomerAddress customerAddress) {
    	return CustomerAddressDto.builder()
    			.id(customerAddress.getId())
    			.zip(customerAddress.getZip())
    			.city(customerAddress.getCity())
    			.street(customerAddress.getStreet())
    			.houseNumber(customerAddress.getHouseNumber())
    			.build();
    }
}
