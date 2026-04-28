package com.tarifvergleich.electricity.dto;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.tarifvergleich.electricity.dto.CustomerAttornyDto.CustomerAttornyForAdminCustomerList;
import com.tarifvergleich.electricity.dto.CustomerChangePasswordHistoryDto.CustomerChangePasswordHistoryResDto;
import com.tarifvergleich.electricity.dto.CustomerDeliveryResponseDto.CustomerAddressRes;
import com.tarifvergleich.electricity.model.Customer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomerDto {

//	@Schema
	private Integer id;
	private String password;
	private String email;
	private String otp;
	private String firstName;
	private String lastName;
	private String userType;
	private String title;
	private String salutation;
	private String companyName;
	private String mobileNumber;
	private Boolean isVerified;
	private BigInteger joinedOn;
	private BigInteger verifiedOn;
	private Boolean isAcknowledged;
	private List<CustomerDeliveryResponseDto> deliveryDetails;
	private CustomerAddressDto address;

	private String zip;
	private String city;
	private String street;
	private String houseNumber;

	private Integer page;
	private Integer size;
	private Integer adminId;

	// This field is used for blocking and unblocking
	private Boolean status;
	private String search;

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@Schema(description = "Detailed response object for Customer information")
	public static class AdminCustomerResponse {
		@Schema(description = "Internal database primary key", example = "101")
		private Integer id;
		@Schema(description = "Primary contact email address", example = "sayan@example.com")
		private String email;
		@Schema(description = "Customer's given name", example = "Sayan")
		private String firstName;
		@Schema(description = "Customer's family name", example = "Das")
		private String lastName;
		@Schema(description = "Type of user account", allowableValues = {"PRIVATE", "BUSINESS"}, example = "PRIVATE")
		private String userType;
		@Schema(description = "Academic or professional title", example = "Dr.")
		private String title;
		@Schema(description = "Formal salutation", example = "Herr")
		private String salutation;
		@Schema(description = "Name of the business (required if userType is BUSINESS)", example = "Sunyam Tech")
		private String companyName;
		@Schema(description = "International format mobile number", example = "+49123456789")
		private String mobileNumber;
		@Schema(description = "Flag indicating if the email/account is verified", example = "true")
		private Boolean isVerified;
		@Schema(description = "Unix timestamp of when the account was verified", example = "1714300000")
		private BigInteger verifiedOn;
		@Schema(description = "Unix timestamp of when the user registered", example = "1714295000")
		private BigInteger joinedOn;
		@Schema(description = "Indicates if the user has accepted terms and conditions", example = "true")
	    private Boolean isAcknowledged;
	    @Schema(description = "Nested address details of the customer")
	    private CustomerAddressDto address;
	    @Schema(description = "History of password update events")
	    private List<CustomerChangePasswordHistoryResDto> changePasswordHistory;
	    @Schema(description = "Unique public-facing customer identifier", example = "CUST-2026-X99")
	    private String uniqueCustomerId;
	    @Schema(description = "Account status (active/inactive)", example = "true")
	    private Boolean status;
	    @Schema(description = "List of power of attorney documents linked to this customer")
	    private List<CustomerAttornyForAdminCustomerList> attornies;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class SingleCustomerResponseDelivery {
		private Integer id;
		private String email;
		private String firstName;
		private String lastName;
		private String userType;
		private String title;
		private String salutation;
		private String companyName;
		private String mobileNumber;
		private Boolean isVerified;
		private BigInteger verifiedOn;
		private BigInteger joinedOn;
		private Boolean isAcknowledged;
		private CustomerAddressRes address;
		private Boolean status;
		private List<CustomerDeliveryResponseDto> deliveryDetails;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class CustomerShortDetail {
		private Integer id;
		private String email;
		private String firstName;
		private String lastName;
		private String userType;
		private String title;
		private String salutation;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class CustomerChangePasswordPayload {
		Integer adminId;
		private Integer id;
		String oldPassword;
		String newPassword;
		String confirmPassword;
		String otp;
	}

	public static SingleCustomerResponseDelivery getCustomerResponseDto(Customer customer) {
		return SingleCustomerResponseDelivery.builder().id(customer.getCustomerId()).email(customer.getEmail())
				.firstName(customer.getFirstName()).lastName(customer.getLastName())
				.salutation(customer.getSalutation()).title(customer.getTitle()).userType(customer.getUserType())
				.companyName(customer.getCompanyName()).mobileNumber(customer.getMobileNumber())
				.status(customer.getStatus()).isVerified(customer.getIsVerified()).joinedOn(customer.getJoinedOn())
				.isAcknowledged(customer.getIsAcknowledged())
				.address(CustomerAddressRes.builder().zip(customer.getZip()).city(customer.getCity())
						.street(customer.getStreet()).houseNumber(customer.getHouseNumber()).build())
				.deliveryDetails(
						customer.getCustomerDelivery().stream().map(CustomerDeliveryResponseDto::mapResponse).toList())
				.build();
	}

	public static AdminCustomerResponse getCustomerDtoResponseForAdmin(Customer customer) {
		if (customer == null)
			return null;
		return AdminCustomerResponse.builder().id(customer.getCustomerId()).email(customer.getEmail())
				.firstName(customer.getFirstName()).lastName(customer.getLastName())
				.salutation(customer.getSalutation()).title(customer.getTitle()).userType(customer.getUserType())
				.companyName(customer.getCompanyName()).mobileNumber(customer.getMobileNumber())
				.status(customer.getStatus()).isVerified(customer.getIsVerified()).verifiedOn(customer.getVerifiedOn())
				.joinedOn(customer.getJoinedOn()).isAcknowledged(customer.getIsAcknowledged())
				.uniqueCustomerId(customer.getCustomerUniqueId())
				.address(Optional.ofNullable(customer.getCustomerAddresses()).filter(list -> !list.isEmpty())
						.map(List::getLast).map(CustomerAddressDto::getCustomerAddressResponseDto).orElse(null))
				.changePasswordHistory(Optional.ofNullable(customer.getCustomerChangePasswordHistories())
						.orElse(Collections.emptyList()).stream().map(CustomerChangePasswordHistoryDto::mapAdminRes)
						.toList())
				.attornies(Optional.ofNullable(customer.getCustomerAttorny()).orElse(Collections.emptyList()).stream()
						.map(CustomerAttornyDto::customerAttornyForAdminCustomerList).toList())
				.build();
	}

	public static CustomerShortDetail customerShortResponse(Customer customer) {
		if (customer == null)
			return null;
		return CustomerShortDetail.builder().id(customer.getCustomerId()).email(customer.getEmail())
				.firstName(customer.getFirstName()).lastName(customer.getLastName()).title(customer.getTitle())
				.userType(customer.getUserType()).salutation(customer.getSalutation()).build();
	}

}
