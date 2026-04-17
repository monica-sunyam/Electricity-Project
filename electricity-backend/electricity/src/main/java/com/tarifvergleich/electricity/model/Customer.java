package com.tarifvergleich.electricity.model;

import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tarifvergleich.electricity.util.Helper;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
	
	@Column(name = "otp_generated_on")
	private BigInteger otpGeneratedOn;
	
	@Column(name = "customer_unique_id")
	private String customerUniqueId;
	
	// This field is used for blocking and unblocking
	private Boolean status;
	
	@OneToMany(mappedBy = "customerId", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
	@JsonIgnoreProperties("customerId")
	private List<CustomerLoginHistory> customerLoginHistories;
	
	@OneToMany(mappedBy = "customerId", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
	@JsonIgnoreProperties("customerId")
	private List<CustomerAddress> customerAddresses;
	
	@OneToMany(mappedBy = "customerId", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
	@JsonIgnoreProperties("customerId")
	private List<CustomerDelivery> customerDelivery;
	
	@OneToMany(mappedBy = "customer", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
	@JsonIgnoreProperties("customer")
	private List<CustomerComparingEnergy> energycomparison;
	
	@ManyToOne
	@JoinColumn(name = "admin_id")
	@JsonIgnore
	private AdminUser admin;
	
	@PrePersist
	protected void prePersist() {
		joinedOn = Helper.getCurrentTimeBerlin();
		isVerified = false;
		isAcknowledged = false; 
		status = true;
		customerUniqueId = Helper.getUniqueIdForCustomerId();
	}
	
	public void addLoginHistory(CustomerLoginHistory record) {
		if(customerLoginHistories == null) {
			customerLoginHistories = new LinkedList<CustomerLoginHistory>();
		}
		
		customerLoginHistories.add(record);
	}
	
	public void addCustomerAddress(CustomerAddress record) {
		if(customerAddresses == null)
			customerAddresses = new LinkedList<CustomerAddress>();
		record.setCustomerId(this);
		customerAddresses.add(record);
	}
	
	public void addCustomerDelivery(CustomerDelivery record) {
		if(customerDelivery == null)
			customerDelivery = new LinkedList<CustomerDelivery>();
		record.setCustomerId(this);
		customerDelivery.add(record);
	}
	
	public void addEnergyComparison(CustomerComparingEnergy record) {
		if(energycomparison == null)
			energycomparison = new LinkedList<CustomerComparingEnergy>();
		record.setCustomer(this);
		energycomparison.add(record);
	}
	
	public void setUserAdmin(AdminUser admin) {
		this.admin = admin;
		admin.addCustomer(this);
	}
	
}
