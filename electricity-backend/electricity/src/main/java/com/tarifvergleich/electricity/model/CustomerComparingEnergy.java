package com.tarifvergleich.electricity.model;

import java.math.BigInteger;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.tarifvergleich.electricity.util.Helper;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;



@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "customers_comparing_electricity")
@Builder
public class CustomerComparingEnergy {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	private String zip;
	
	private String city;
	
	private String street;
	
	private String houseNumber;
	
	private String consumption;
	
	@Column(name = "consumer_type")
	private String consumerType;

	@Column(name = "energy_branch")
	private String branch;
	
	@ManyToOne
	@JoinColumn(name = "customer_id")
	@JsonIgnore
	private Customer customer;
	
	@Column(name = "compared_on")
	private BigInteger comparedOn;
	
	@Column(name = "request_ip")
	private String requestIp;
	
	@Column(name = "request_device_detail")
	private String requestDeviceDetails;
	

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "base_provider_response", columnDefinition = "jsonb")
	private JsonNode baseProviderResponse; 


	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "rate_response", columnDefinition = "jsonb")
	private JsonNode energyRateResponse;
	
	@PrePersist
	protected void onCreate() {
		comparedOn = Helper.getCurrentTimeBerlin();
	}
	
	public void setCustomerModel(Customer customer) {
		customer.addEnergyComparison(this);
		this.customer = customer;
	}
}
