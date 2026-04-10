package com.tarifvergleich.electricity.model;

import java.math.BigInteger;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Table(name = "customer")
public class Customer {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "customer_id")
	private Integer customerId;
	
	@Column(name = "first_name")
	private String firstName;
	
	@Column(name = "last_name")
	private String lastName;
	
	private String password;
	
	@Column(unique = true)
	private String email;
	
	private String otp;
	
	@Column(name = "user_type")
	private String userType;
	
	private String title;
	
	@Column(name = "salutation")
	private String salutation;
	
	@Column(name = "company_name")
	private String companyName;
	
	@Column(name = "mobile_number")
	private String mobileNumber;
	
	@Column(name = "joined_on")
	private BigInteger joinedOn;
	
	@Column(name = "updated_on")
	private BigInteger updatedOn;
	
	@Column(name = "is_verified")
	private Boolean isVerified;
	
	@Column(name = "is_acknowledged")
	private Boolean isAcknowledged;
	
	// This field is used for blocking and unblocking
	private Boolean status;
	
	@OneToMany(mappedBy = "customerId", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
	@JsonIgnoreProperties("customerId")
	private List<CustomerLoginHistory> customerLoginHistories;
	
	@PrePersist
	protected void prePersist() {
		joinedOn = BigInteger.valueOf(Instant.now().getEpochSecond());
		isVerified = false;
		isAcknowledged = false; 
		status = true;
	}
	
	public void addLoginHistory(CustomerLoginHistory record) {
		if(customerLoginHistories == null) {
			customerLoginHistories = new LinkedList<CustomerLoginHistory>();
		}
		
		customerLoginHistories.add(record);
	}
	
}
