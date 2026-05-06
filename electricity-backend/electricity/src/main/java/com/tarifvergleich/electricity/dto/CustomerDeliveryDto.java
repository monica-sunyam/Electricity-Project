package com.tarifvergleich.electricity.dto;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonSetter;
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

	private Integer deliveryId;
	private String title;
	private String firstName;
	private String lastName;
	private String salutation;
	private String mobile;
	private String telephone;
	@JsonFormat(pattern = "dd.MM.yyyy")
	private LocalDate dob;
	private Integer persons;
	private Integer consumption;
	private String zip;
	private String city;
	private String street;
	private String houseNumber;
	
	private String deliveryType;

	private CustomerAddress customerAddress;
	private CustomerBillingAddress billingAddress;
	private CustomerConnect customerConnection;
	private CustomerPayment customerPayment;
	
	private Integer adminId;
	private Integer page;
	private Integer size;
	
	@JsonSetter("dob")
	public void setDob(String dob) {
	    if (dob == null || dob.isBlank()) {
	        this.dob = null;
	    } else {
	        this.dob = LocalDate.parse(dob, DateTimeFormatter.ofPattern("dd.MM.yyyy"));
	    }
	}
	
}
