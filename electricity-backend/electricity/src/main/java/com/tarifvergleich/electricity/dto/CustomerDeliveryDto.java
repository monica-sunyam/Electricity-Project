package com.tarifvergleich.electricity.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tarifvergleich.electricity.model.CustomerAddress;
import com.tarifvergleich.electricity.model.CustomerBillingAddress;
import com.tarifvergleich.electricity.model.CustomerConnect;
import com.tarifvergleich.electricity.model.CustomerPayment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomerDeliveryDto {

	private Integer id;
	private String title;
	private String firstName;
	private String lastName;
	private String mobile;
	private String telephone;
	@JsonFormat(pattern = "dd.MM.yyyy")
	private LocalDate deliveryDate;
	private String zip;
	private String city;
	private String street;
	private String houseNumber;

	private CustomerAddress customerAddress;
	private CustomerBillingAddress billingAddress;
	private CustomerConnect customerConnection;
	private CustomerPayment customerPayment;
	
	private Integer adminId;
}
