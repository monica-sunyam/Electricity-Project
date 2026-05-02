package com.tarifvergleich.electricity.model;

import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tarifvergleich.electricity.util.Helper;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "admin")
public class AdminUser {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "admin_id")
	private Integer adminId;
	
	private String name;
	
	private String email;
	
	private String password;
	
	@Enumerated(EnumType.STRING)
	private AdminRole adminRole;
	
	@Column(name = "ip_address")
	private String ipAddress;
	
	@Column(name = "created_on")
	private BigInteger createdOn;
	
	private String refreshToken;
	
	@Column(name = "last_logged_in")
	private BigInteger lastLogin;
	
	@OneToMany(mappedBy = "adminUser", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
	@JsonIgnoreProperties("adminUser")
	private List<AdminLoginHistory> loginHistory;
	
	@OneToMany(mappedBy = "adminId", cascade = {CascadeType.PERSIST, CascadeType.REMOVE, CascadeType.MERGE}, orphanRemoval = true)
	@JsonIgnoreProperties("adminId")
	private List<AdminAsset> adminAssets;
	
	@OneToMany(mappedBy = "adminId", cascade = {CascadeType.PERSIST, CascadeType.REMOVE, CascadeType.MERGE}, orphanRemoval = true)
	@JsonIgnoreProperties("adminId")
	private List<AdminServiceMenu> adminServiceMenu;
	
	@OneToMany(mappedBy = "admin", cascade = {CascadeType.PERSIST, CascadeType.REMOVE, CascadeType.MERGE}, orphanRemoval = true)
	@JsonIgnoreProperties("admin")
	private List<Customer> customer;
	
	@OneToMany(mappedBy = "admin", cascade = {CascadeType.PERSIST, CascadeType.REMOVE, CascadeType.MERGE}, orphanRemoval = true)
	@JsonIgnoreProperties("admin")
	private List<CustomerDelivery> customerDeliveries;
	
	@OneToMany(mappedBy = "admin", cascade = {CascadeType.PERSIST, CascadeType.REMOVE, CascadeType.MERGE}, orphanRemoval = true)
	@JsonIgnoreProperties("admin")
	private List<CustomerComparingEnergy> customerEnergyComparisons;
	
	@OneToMany(mappedBy = "admin", cascade = {CascadeType.PERSIST, CascadeType.REMOVE, CascadeType.MERGE}, orphanRemoval = true)
	@JsonIgnoreProperties("admin")
	private List<CustomerAttorny> customerAttornies;
	
	@OneToMany(mappedBy = "admin", cascade = {CascadeType.PERSIST, CascadeType.REMOVE, CascadeType.MERGE}, orphanRemoval = true)
	@JsonIgnoreProperties("admin")
	private List<CustomerChangePasswordHistory> customerChangePasswordHistories;
	
	@OneToMany(mappedBy = "admin", cascade = {CascadeType.PERSIST, CascadeType.REMOVE, CascadeType.MERGE}, orphanRemoval = true)
	@JsonIgnoreProperties("admin")
	private List<CustomerServices> services;
	
	
	@OneToMany(mappedBy = "admin", cascade = {CascadeType.PERSIST, CascadeType.REMOVE, CascadeType.MERGE}, orphanRemoval = true)
	@JsonIgnoreProperties("admin")
	private List<CustomerServiceRequest> customerServiceRequest;
	
	@OneToMany(mappedBy = "admin", cascade = {CascadeType.PERSIST, CascadeType.REMOVE, CascadeType.MERGE}, orphanRemoval = true)
	@JsonIgnoreProperties("admin")
	private List<ListOfHolidays> holidays;
	
	@OneToMany(mappedBy = "admin", cascade = {CascadeType.PERSIST, CascadeType.REMOVE, CascadeType.MERGE}, orphanRemoval = true)
	@JsonIgnoreProperties("admin")
	private List<CustomerBookingDocument> customerBookingDocuments;
	
	@PrePersist
	protected void onCreate() {
	    this.createdOn = Helper.getCurrentTimeBerlin();
	}
	
	public void addLoginHistory(AdminLoginHistory loginHistory) {
		
		if (this.loginHistory == null) {
            this.loginHistory = new LinkedList<AdminLoginHistory>();
        }
		
		this.loginHistory.add(loginHistory);
	}
	
	public void addAdminAsset(AdminAsset asset) {
		if(this.adminAssets == null) {
			this.adminAssets = new LinkedList<AdminAsset>();
		}
		
		this.adminAssets.add(asset);
	}
	
	public void addAdminServiceMenu(AdminServiceMenu menu) {
		if(this.adminServiceMenu == null)
			this.adminServiceMenu = new LinkedList<AdminServiceMenu>();
		
		this.adminServiceMenu.add(menu);
	}
	
	public void addCustomer(Customer customer) {
		if(this.customer == null)
			this.customer = new LinkedList<Customer>();
		
		this.customer.add(customer);
	}
	
	public void addDelivery(CustomerDelivery delivery) {
		if(this.customerDeliveries == null)
			this.customerDeliveries = new LinkedList<CustomerDelivery>();
		
		this.customerDeliveries.add(delivery);
	}
	
	public void addCustomerEnergyComparison(CustomerComparingEnergy energy) {
		if(customerEnergyComparisons == null)
			customerEnergyComparisons = new LinkedList<CustomerComparingEnergy>();
		
		energy.setAdmin(this);
		
		customerEnergyComparisons.add(energy);
	}
	
	public void addCustomerChangePasswordHistory(CustomerChangePasswordHistory changePasswordHistory) {
		if(customerChangePasswordHistories == null)
			customerChangePasswordHistories = new LinkedList<CustomerChangePasswordHistory>();
		changePasswordHistory.setAdmin(this);
		customerChangePasswordHistories.add(changePasswordHistory);
	}
	
	public void addCustomerAttorny(CustomerAttorny attorny) {
		if(customerAttornies == null)
			customerAttornies = new LinkedList<CustomerAttorny>();
		
		attorny.setAdmin(this);
		customerAttornies.add(attorny);
	}
	
	public void addCustomerService(CustomerServices service) {
		if(this.services == null)
			this.services = new LinkedList<CustomerServices>();
		
		service.setAdmin(this);
		services.add(service);
	}
	
	public void addCustomerServiceRequest(CustomerServiceRequest request) {
		if(this.customerServiceRequest == null)
			customerServiceRequest = new LinkedList<CustomerServiceRequest>();
		request.setAdmin(this);
		customerServiceRequest.add(request);
	}
	
	public void addHolidays(ListOfHolidays holiday) {
		if(this.holidays == null)
			holidays = new LinkedList<ListOfHolidays>();
		holiday.setAdmin(this);
		holidays.add(holiday);
	}
	
	public void addCustomerBookingDocuments(CustomerBookingDocument document) {
		if(this.customerBookingDocuments == null)
			customerBookingDocuments = new LinkedList<CustomerBookingDocument>();
		document.setAdmin(this);
		customerBookingDocuments.add(document);
	}
}
