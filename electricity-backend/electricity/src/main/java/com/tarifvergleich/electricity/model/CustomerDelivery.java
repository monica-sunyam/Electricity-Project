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
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
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
@Builder
@Getter
@Setter
@Table(name = "customer_delivery")
public class CustomerDelivery {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	private String title;
	
	@Column(name = "first_name")
	private String firstName;
	
	@Column(name = "last_name")
	private String lastName;
	
	private String salutation;
		
	@Column(name = "mobile_number")
	private String mobile;
	
	@Column(name = "telephone_number")
	private String telephone;
	
	@Column(name = "delivery_type")
	private String deliveryType; // This determines whether delivery is of ELECTRICITY or GAS or anything else.
	
	@Column(name = "dob")
	private BigInteger dob;
	
	@Column(name = "no_of_person")
	private Integer numberOfPerson;
	
	@Column(name = "total_consumption")
	private Integer totalConsumption;
	
	@Column(name = "order_placed_on")
	private BigInteger orderPlacedOn;
	
	@Column(name = "order_placed")
	private Boolean orderPlaced;
	
	@Column(name = "order_placed_in_egon")
	private Boolean orderPlacedInEgon;
	
	@Column(name = "unique_delivery_id")
	private String uniqueDeliveryId;
	
	@Column(name = "order_no", unique = true)
	private Long orderNo;
	
	@Column(name = "notification_enabled")
	private Boolean notificationEnabled;
	
	@Column(name = "expiry_on")
	private BigInteger expiryOn;
	
	@Column(name = "last_date_of_cancellation")
	private BigInteger lastDateOfCancellation;
	
	@Column(name = "is_expired")
	private Boolean isExpired;
	
	@Column(name = "is_cancelled")
	private Boolean isCancelled;
	
	@Column(name = "cancelled_on")
	private Boolean cancelledOn;
	
	@OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
	@JoinColumn(name = "customer_billing_id")
	private CustomerBillingAddress billingAddress;
	
	@ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
	@JoinColumn(name = "customer_address_id")
	private CustomerAddress address;
	
	@OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
	@JoinColumn(name = "customer_connection_id")
	private CustomerConnect customerConnection;
	
	@OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
	@JoinColumn(name = "customer_payment_id")
	private CustomerPayment customerPayment;
	
	@OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
	@JoinColumn(name = "customer_schedule_id")
	private CustomerContactSchedule customerSchedule;
	
	@OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "customer_selected_provider_id")
	private CustomerSelectedProvider customerProvider;
	
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "customer_booking_document_id")
	private CustomerBookingDocument customerBookingDocument;
	
	@OneToOne(mappedBy = "delivery", fetch = FetchType.LAZY)
	@JsonIgnore
	private CustomerOrder customerOrder;
	
	@OneToMany(mappedBy = "customerDelivery")
	@JsonIgnoreProperties("customerDelivery")
	private List<CustomerServiceRequest> customerServiceRequests;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "admin_id")
	@JsonIgnore
	private AdminUser admin;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "customer_id")
	@JsonIgnore
	private Customer customerId;
	
	@PrePersist
	protected void onCreate() {
		orderPlacedOn = Helper.getCurrentTimeBerlin();
		orderPlaced = false;
		uniqueDeliveryId = "D" + Helper.getUniqueIdForCustomerId();
		isExpired = false;
		isCancelled = false;
		notificationEnabled = true;
		orderPlacedInEgon = false;
	}
	
	public void setUserAdmin(AdminUser admin) {
		this.admin = admin;
	}
	
	public void addCustomerServiceRequest(CustomerServiceRequest request) {
		if(customerServiceRequests == null)
			customerServiceRequests = new LinkedList<CustomerServiceRequest>();
		request.setCustomerDelivery(this);
		customerServiceRequests.add(request);
	}
	
}
