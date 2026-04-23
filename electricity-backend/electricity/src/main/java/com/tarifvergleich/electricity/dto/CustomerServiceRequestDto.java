package com.tarifvergleich.electricity.dto;

import java.math.BigInteger;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class CustomerServiceRequestDto {

	private Integer serviceRequestId;
	private String serviceRequestType;
	private String title;
	private String message;
	private BigInteger createdOn;

	private Boolean isOpen;
	private Boolean inProgress;
	private Boolean isClosed;

	private BigInteger requestReopenedOn;
	private BigInteger requestClosedOn;

	private Integer serviceId;
	private String serviceName; // To display which service this request belongs to

	private Integer customerId;
	private Integer deliveryId;
	private Integer adminId;
}
