package com.tarifvergleich.electricity.model;

import java.math.BigInteger;

import com.tarifvergleich.electricity.util.Helper;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "contract_tokens")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ContractToken {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(unique = true, nullable = false)
	private String token;

	private Integer orderId;

	@Column(name = "created_on")
	private BigInteger createdOn;

	@Column(name = "expiry")
	private BigInteger expiryDate;

	private Boolean used;

	@PrePersist
	protected void onCreate() {
		createdOn = Helper.getCurrentTimeBerlin();
		used = false;
	}
}
