package com.tarifvergleich.electricity.dto;

import java.math.BigInteger;
import java.util.List;

import com.tarifvergleich.electricity.model.CustomerServices;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class CustomerServicesDto {

	private Integer serviceId;
	private String serviceName;
	private String serviceType; // This is the differentiator between GENERAL category and DELIVERY delivery
								// provider category and ALL for common category.
	private BigInteger addedOn;
	private Boolean status;
	private Integer adminId;
	private List<Integer> customerServiceRequestId;
	
	private Integer page;
	private Integer size;

	@AllArgsConstructor
	@NoArgsConstructor
	@Builder
	@Data
	public static class CustomerListOfServiceResDto {
		private Integer serviceId;
		private String serviceName;
		private String serviceType;
	}

	@AllArgsConstructor
	@NoArgsConstructor
	@Builder
	@Data
	public static class CustomerListOfServiceForAdminResDto {
		private Integer serviceId;
		private String serviceName;
		private String serviceType;
		private BigInteger addedOn;
		private Boolean status;
	}

	public static CustomerListOfServiceResDto mapCustomerService(CustomerServices service) {
		if (service == null)
			return null;

		return CustomerListOfServiceResDto.builder().serviceId(service.getId()).serviceName(service.getServiceName())
				.serviceType(service.getServiceType()).build();

	}

	public static CustomerListOfServiceForAdminResDto mapCustomerServiceForAdmin(CustomerServices service) {
		if (service == null)
			return null;

		return CustomerListOfServiceForAdminResDto.builder().serviceId(service.getId())
				.serviceName(service.getServiceName()).serviceType(service.getServiceType())
				.addedOn(service.getAddedOn()).status(service.getStatus()).build();
	}
}
