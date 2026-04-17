package com.tarifvergleich.electricity.dto;

import java.util.List;

import com.tarifvergleich.electricity.model.CustomerSelectedProvider;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnergyRateDto {
	private Long rateId;
    private String rateName;
    private Long providerId;
    private Long netzProviderId;
    private String providerName;
    private String providerSVG;
    private String providerSVGPath;
    
    // Pricing Data
    private double basePriceYear;
    private double basePriceMonth;
    private double workPrice;
    private double totalPrice;
    private double totalPriceMonth;
    private double savingPerYear;
    private double workPriceNt;
    private double optBonus;
    
    // Contract Details
    private int partialPayment;
    private String optGuarantee;
    private String optGuaranteeType;
    private String optTerm;
    private List<String> rateChangeType;
    private int cancel;
    private String termBeforeNewType;
    private String termBeforeNewMaxDate;
    
    // Status Flags
    private boolean selfPayment;
    private boolean requiredEmail;
    private boolean optEco;
    private boolean recommended;
    
    // Meta Data
    private String branch;
    private String type;
    
    public static EnergyRateDto getProviderResponse(CustomerSelectedProvider provider) {
    	return EnergyRateDto.builder()
    			.branch(provider.getBranch())
    			.netzProviderId(provider.getNetzProviderId())
    			.providerId(provider.getProviderId())
    			.providerSVG(provider.getProviderSVGPath())
    			.providerName(provider.getProviderName())
    			.rateId(provider.getRateId())
    			.rateName(provider.getRateName())
    			.totalPrice(provider.getTotalPrice())
    			.totalPriceMonth(provider.getTotalPriceMonth())
    			.build();
    }
}
