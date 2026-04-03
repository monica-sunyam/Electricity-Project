package com.tarifvergleich.electricity.dto;

import java.util.List;

import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
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
}
