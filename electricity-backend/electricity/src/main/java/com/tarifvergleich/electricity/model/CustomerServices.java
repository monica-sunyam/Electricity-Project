package com.tarifvergleich.electricity.model;

import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tarifvergleich.electricity.util.Helper;

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
@Getter
@Setter
@Builder
@Table(name = "services")
@Entity
public class CustomerServices {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@Column(name = "service_name")
	private String serviceName;
	
	@Column(name = "service_type")
	private String serviceType; // This is the differentiator between GENERAL category and DELIVERY provider category and ALL for common category.
	
	@Column(name = "added_on")
	private BigInteger addedOn;
	
	@Column(name = "updated_on")
	private BigInteger updatedOn;
	
	private Boolean status;
	
	@ManyToOne
	@JoinColumn(name = "admin_id")
	@JsonIgnore
	private AdminUser admin;
	
	@OneToMany(mappedBy = "service")
	@JsonIgnoreProperties("service")
	private List<CustomerServiceRequest> customerServiceRequest;
	
	@PrePersist
	protected void onCreate() {
		addedOn = Helper.getCurrentTimeBerlin();
		status = true;
	}
	
	public void addCustomerServiceRequest(CustomerServiceRequest serviceRequest) {
		if(customerServiceRequest == null)
			customerServiceRequest = new LinkedList<CustomerServiceRequest>();
		
		serviceRequest.setService(this);
		customerServiceRequest.add(serviceRequest);
	}
}
