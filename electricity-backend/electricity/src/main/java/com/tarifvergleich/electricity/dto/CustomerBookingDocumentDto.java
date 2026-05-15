package com.tarifvergleich.electricity.dto;

import java.math.BigInteger;

import com.tarifvergleich.electricity.dto.CustomerDto.CustomerShortDetail;
import com.tarifvergleich.electricity.model.CustomerBookingDocument;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class CustomerBookingDocumentDto {

	private Integer bookingDocId;
	private String signedOriginalFileName;
	private String signedFileUrl;
	private Boolean signedDocumentSubmitted;
	private BigInteger addedOn;
	private BigInteger updatedOn;
	private Integer adminId;
	private Integer customerId;
	private Integer deliveryId;

	@AllArgsConstructor
	@NoArgsConstructor
	@Builder
	@Data
	public static class CustomerBookingDocumentAdminResDto {

		private Integer bookingDocId;
		private String signedOriginalFileName;
		private String signedFileUrl;
		private Boolean signedDocumentSubmitted;
		private BigInteger addedOn;
		private Integer deliveryId;
		private CustomerShortDetail customer;
	}

	public static CustomerBookingDocumentAdminResDto mapAdminBookingDocRes(CustomerBookingDocument document) {
		if (document == null)
			return null;

		return CustomerBookingDocumentAdminResDto.builder().bookingDocId(document.getId())
				.signedOriginalFileName(document.getSignedOriginalFileName()).signedFileUrl(document.getSignedFileUrl())
				.signedDocumentSubmitted(document.getSignedDocumentSubmitted()).addedOn(document.getAddedOn())
				.deliveryId(document.getCustomerDelivery().getId())
				.customer(CustomerDto.customerShortResponse(document.getCustomer())).build();
	}
}
