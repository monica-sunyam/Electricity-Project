package com.tarifvergleich.electricity.model;

import java.math.BigInteger;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

@Table(name = "customer_attorny")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class CustomerAttorny {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private String salutation;

	private String title;

	@Column(name = "first_name")
	private String firstName;

	@Column(name = "last_name")
	private String lastName;

	private String zip;

	private String city;

	private String street;
	
	@Column(name = "user_type")
	private String userType;

	@Column(name = "house_number")
	private String houseNumber;
	
	@Column(name = "customer_unique_id")
	private String customerUniqueId;
	
	@Column(name = "company_name")
	private String companyName;
	
	@Column(name = "legal_representative_first_name")
	private String legalRepresentativeFirstName;
	
	@Column(name = "legal_representative_last_name")
	private String legalRepresentativeLastName;
	
	@Column(name = "attorny_id")
	private String uniqueAttornyId;

	@Column(name = "submitted_on")
	private BigInteger submittedOn;
	
	@Column(name = "approval_status", comment = "0-pending, 1-approved, 2-rejected")
	private Integer approvalStatus;
	
	@Column(name = "approved_on")
	private BigInteger approvedOn;
	
	@Column(name = "rejected_on")
	private BigInteger rejectedOn;
	
	@Column(name = "is_revoked")
	private Boolean isRevoked;
	
	@Column(name = "revoked_on")
	private BigInteger revokedOn;
	
	@Column(name = "customer_signature_path", columnDefinition = "TEXT")
	private String customerSignaturePath;
	
	@ManyToOne
	@JoinColumn(name = "customer_id")
	@JsonIgnore
	private Customer customer;
	
	@ManyToOne
	@JoinColumn(name = "admin_id")
	@JsonIgnore
	private AdminUser admin;
	
	@PrePersist
	protected void onCreate() {
		submittedOn = Helper.getCurrentTimeBerlin();
		uniqueAttornyId = Helper.getUniqueId();
	}
	
	public void setCustomerRef(Customer customer) {
		this.customer = customer;
	}
	
	public void setAdminRef(AdminUser admin) {
		this.admin = admin;
	}
	
}
