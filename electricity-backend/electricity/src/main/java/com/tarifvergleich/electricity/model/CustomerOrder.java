package com.tarifvergleich.electricity.model;

import java.math.BigInteger;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "customer_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerOrder {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "order_status", comment = "0 - open order")
	private Integer orderStatus;

	@Column(name = "admin_placed_order")
	private Boolean adminPlacedOrder;
	

	@Column(name = "order_id", unique = true)
	private Long orderId;

	@Column(name = "admin_placed_order_on")
	private BigInteger adminPlacedOrderOn;

	@Column(name = "created_on")
	private BigInteger createdOn;
	
	@Column(name = "is_expired")
	private Boolean isExpired;
	
	@Column(name = "email_send_to_customer_for_signature")
	private Boolean emailSendToCustomerForSignature;
	
	@Column(name = "expiry_on")
	private BigInteger expiryOn;
	
	@Column(name = "operation_period")
	private BigInteger operationPeriod;
	
	@Column(name = "last_date_of_cancellation")
	private BigInteger lastDateOfCancellation;
	
	@Column(name = "is_cancelled")
	private Boolean isCancelled;
	
	@Column(name = "cancelled_on")
	private Boolean cancelledOn;

	@OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
	@JoinColumn(name = "customer_delivery_id")
	private CustomerDelivery delivery;

	
	@OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "customer_doc_id")
	private CustomerBookingDocument customerBookingDocument;
	
	@OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "customer_contract_id")
	private CustomerContractSignature customerContractSignature;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "customer_id")
	@JsonIgnore
	private Customer customer;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "admin_id")
	@JsonIgnore
	private AdminUser admin;

	@PrePersist
	protected void onCreate() {
		createdOn = Helper.getCurrentTimeBerlin();
		adminPlacedOrder = false;
		orderStatus = 0;
		isCancelled = false;
		isExpired = false;
		emailSendToCustomerForSignature = false;
	}

}
