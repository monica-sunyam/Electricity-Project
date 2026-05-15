package com.tarifvergleich.electricity.dto;

import java.math.BigInteger;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class CustomerContractSignatureDto {

	private Integer id;
    private String signature;
    private String signatureBank;
    private String signatureCustomer;
    private String signatureDataProtection;
    private BigInteger signedOn;
    
    private Integer customerId;
    private Integer adminId;
    
    private Integer customerOrderId;
}
