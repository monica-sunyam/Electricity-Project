package com.tarifvergleich.electricity.dto;

import com.tarifvergleich.electricity.model.CustomerPayment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerPaymentRequestDto {

	private Integer customerId;
    private Integer deliveryId;
    private PaymentDto paymentData;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaymentDto{
    	private String paymentMethod;
        private String iban;
        private AccountHolderDto accountHolder;
        private Boolean sepaConsent;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AccountHolderDto {
        private String firstName;
        private String lastName;
    }
    
    
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CustomerPaymentResponse{
    	
    	private Integer id;
    	private String paymentMethod;
        private String iban;
        private String firstName;
        private String lastName;
        private Boolean sepaConsent;
    }
    
    public static CustomerPaymentResponse getCustomerPaymentResponse(CustomerPayment payment) {
    	
    	if(payment == null) return null;
    	
    	return CustomerPaymentResponse.builder()
    			.id(payment.getId())
    			.paymentMethod(payment.getPaymentMethod())
    			.iban(payment.getIban())
    			.firstName(payment.getAccountHolderFirstName())
    			.lastName(payment.getAccountHolderLastName())
    			.sepaConsent(payment.getSepaConsent())
    			.build();
    }

}
