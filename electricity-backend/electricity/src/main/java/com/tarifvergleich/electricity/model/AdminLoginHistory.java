package com.tarifvergleich.electricity.model;

import java.math.BigInteger;
import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "admin_login_history")
public class AdminLoginHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer adminLoginId;
	
	private String loginIp;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "admin_id")
	@JsonIgnore
	private AdminUser adminUser;
	
	private BigInteger loginTime;
	
	private BigInteger logoutTime;
	
	@PrePersist
	protected void onCreate() {
		loginTime = BigInteger.valueOf(Instant.now().getEpochSecond());
	}
}
