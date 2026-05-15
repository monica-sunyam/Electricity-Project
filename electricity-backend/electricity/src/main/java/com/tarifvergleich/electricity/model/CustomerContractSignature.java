package com.tarifvergleich.electricity.model;

import java.math.BigInteger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tarifvergleich.electricity.util.Helper;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "customer_contract_signatures")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CustomerContractSignature {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	private String signature;
	
	@Column(name = "signature_bank")
	private String signatureBank;
	
	@Column(name = "signature_customer")
	private String signatureCustomer;
	
	@Column(name = "signature_data_protection")
	private String signatureDataProtection;
	
	@Column(name = "signed_on")
	private BigInteger signedOn;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "customer_id")
	@JsonIgnore
	private Customer customer;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "admin_id")
	@JsonIgnore
	private AdminUser admin;
	
	@OneToOne(mappedBy = "customerContractSignature", fetch = FetchType.LAZY)
	@JsonIgnore
	private CustomerOrder customerOrder;
	
	@PrePersist
	protected void onCreate() {
		signedOn = Helper.getCurrentTimeBerlin();
	}
}
