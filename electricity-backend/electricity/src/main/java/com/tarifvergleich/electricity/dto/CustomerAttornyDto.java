package com.tarifvergleich.electricity.dto;

import java.math.BigInteger;

import com.tarifvergleich.electricity.model.CustomerAttorny;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class CustomerAttornyDto {

	private Integer attornyId;
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

	@NoArgsConstructor
	@AllArgsConstructor
	@Data
	@Builder
	public static class CustomerAttornyForAdminCustomerList {
		private Integer attornyId;
		private String salutation;
		private String title;
		private String firstName;
		private String lastName;
		private String userType;
		private String zip;
		private String city;
		private String street;
		private String houseNumber;

		private String companyName;
		private String legalRepresentativeFirstName;
		private String legalRepresentativeLastName;
		private String uniqueAttornyId;

		private BigInteger submittedOn;
		private Integer approvalStatus;
		private BigInteger approvedOn;
		private BigInteger rejectedOn;
		private Boolean isRevoked;
		private BigInteger revokedOn;

		private String customerSignaturePath;
		private String placeAndDate;
	}

	public static CustomerAttornyForAdminCustomerList customerAttornyForAdminCustomerList(CustomerAttorny attorny) {
		if (attorny == null)
			return null;

		return CustomerAttornyForAdminCustomerList.builder().attornyId(attorny.getId())
				.salutation(attorny.getSalutation()).title(attorny.getTitle()).firstName(attorny.getFirstName())
				.lastName(attorny.getLastName()).userType(attorny.getUserType()).zip(attorny.getZip())
				.city(attorny.getCity()).street(attorny.getStreet()).houseNumber(attorny.getHouseNumber())
				.companyName(attorny.getCompanyName())
				.legalRepresentativeFirstName(attorny.getLegalRepresentativeFirstName())
				.legalRepresentativeLastName(attorny.getLegalRepresentativeLastName())
				.uniqueAttornyId(attorny.getUniqueAttornyId()).submittedOn(attorny.getSubmittedOn())
				.approvalStatus(attorny.getApprovalStatus()).approvedOn(attorny.getApprovedOn())
				.rejectedOn(attorny.getRejectedOn()).isRevoked(attorny.getIsRevoked()).revokedOn(attorny.getRevokedOn())
				.customerSignaturePath(attorny.getCustomerSignaturePath()).placeAndDate(attorny.getPlaceAndDate())
				.build();
	}
}
