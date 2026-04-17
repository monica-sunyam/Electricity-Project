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
}
