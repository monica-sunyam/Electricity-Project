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

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Table(name = "customer_service_requests")
@Entity
public class CustomerServiceRequest {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@Column(name = "service_request_type")
	private String serviceRequestType; // This is to determine whether the request is related to delivery or general.
	
	@Column(name = "title", columnDefinition = "TEXT")
	private String title;
	
	@Column(name = "description", columnDefinition = "TEXT")
	private String description;
	
	@Column(name = "created_on")
	private BigInteger createdOn;
	
	@Column(name = "is_request_open")
	private Boolean isOpen;
	
	@Column(name = "is_request_inprogess")
	private Boolean inProgress;
	
	@Column(name = "is_request_closed")
	private Boolean isClosed;
	
	@Column(name = "request_reopened_on")
	private BigInteger requestReopenedOn;
	
	@Column(name = "closed_on")
	private BigInteger requestClosedOn;
	
	@Column(name = "ticket_number")
	private String ticketNumber;
	
	@ManyToOne
	@JoinColumn(name = "customer_services_id")
	@JsonIgnore
	private CustomerServices  service;
	
	@ManyToOne
	@JoinColumn(name = "customer_id")
	@JsonIgnore
	private Customer customer;
	
	@ManyToOne
	@JoinColumn(name = "admin_id")
	@JsonIgnore
	private AdminUser admin;
	
	@ManyToOne
	@JoinColumn(name = "customer_delivery_id")
	@JsonIgnore
	private CustomerDelivery customerDelivery;
	
	@OneToMany(mappedBy = "customerServiceRequest", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
	@JsonIgnoreProperties("customerServiceRequest")
	private List<CustomerServiceRequestMessages> customerServiceRequestMessages;
	
	@PrePersist
	protected void onCreate() {
		createdOn = Helper.getCurrentTimeBerlin();
		ticketNumber = Helper.getUniqueTicketNumber();
		isOpen = true;
		inProgress = false;
		isClosed = false;		
	}
	
	public void addCustomerServiceRequestMessage(CustomerServiceRequestMessages message) {
		if(customerServiceRequestMessages == null)
			customerServiceRequestMessages = new LinkedList<CustomerServiceRequestMessages>();
		
		message.setCustomerServiceRequest(this);
		customerServiceRequestMessages.add(message);
	}
}
