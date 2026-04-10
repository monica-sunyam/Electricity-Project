package com.tarifvergleich.electricity.model;

import java.math.BigInteger;
import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnore;

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

@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "customer_loginHistory")
public class CustomerLoginHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@Column(name = "loggedin_time")
	private BigInteger loggedInTime;
	
	@Column(name = "loggedout_time")
	private BigInteger loggedOutTime;
	
	@Column(name = "login_ip")
	private String loginIp;
	
	@ManyToOne
	@JoinColumn(name = "customer_id")
	@JsonIgnore
	private Customer customerId;
	
	@PrePersist
	protected void onCreate() {
		loggedInTime = BigInteger.valueOf(Instant.now().getEpochSecond());
	}
}
