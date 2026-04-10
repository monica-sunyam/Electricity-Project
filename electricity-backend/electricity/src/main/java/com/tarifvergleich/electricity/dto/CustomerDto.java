package com.tarifvergleich.electricity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomerDto {
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
	private Boolean isAcknowledged;
	
	// This field is used for blocking and unblocking
	private Boolean status;
}
