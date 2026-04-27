package com.tarifvergleich.electricity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class CustomerAttornyDto {

	private Integer id;
    private String salutation;
    private String title;
    private String firstName;
    private String lastName;
    private String userType;
    private String zip;
    private String city;
    private String street;
    private String houseNumber;
    
    // Identity fields
    private String customerUniqueId;
    private String companyName;
    private String legalRepresentativeFirstName;
    private String legalRepresentativeLastName;
    private String uniqueAttornyId;

    // Status and Auditing
    private Long submittedOn;
    private Integer approvalStatus;
    private Long approvedOn;
    private Long rejectedOn;
    private Boolean isRevoked;
    private Long revokedOn;
    
    private String customerSignaturePath;
    private String placeAndDate;

    private Integer customerId;
    private Integer adminId;
}
