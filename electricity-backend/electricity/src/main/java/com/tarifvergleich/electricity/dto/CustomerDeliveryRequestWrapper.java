package com.tarifvergleich.electricity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerDeliveryRequestWrapper {
	
	private Integer customerId;
	private Integer deliveryId;
    private CustomerDeliveryDto deliveryAddress;
    private CustomerBillingRequestDto billingAddress;
    private EnergyRateDto provider;
    
}
