package com.tarifvergleich.electricity.dto;

import java.math.BigInteger;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tarifvergleich.electricity.model.CustomerConnect;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomerConnectionRequestDto {

	private Integer id;

	private Boolean isMovingIn;

	@JsonFormat(pattern = "dd.MM.yyyy")
	private LocalDate moveInDate;


	private Boolean submitLater;

	private String meterNumber;

	private String marketLocationId;

	private String currentProvider;

	private Boolean autoCancellation;

	private Boolean alreadyCancelled;

	private Boolean selfCancellation;

	private Boolean delivery;

	@JsonFormat(pattern = "dd.MM.yyyy")
	private LocalDate desiredDelivery;
	
	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class CustomerConnectionResponse {
		private Integer id;
		private Boolean isMovingIn;
		private BigInteger moveInDate;
		private Boolean submitLater;
		private String meterNumber;
		private String marketLocationId;
		private String currentProvider;
		private Boolean autoCancellation;
		private Boolean alreadyCancelled;
		private Boolean selfCancellation;
		private Boolean delivery;
		private BigInteger desiredDelivery;
	}
	
	public static CustomerConnectionResponse getConnectionResponse(CustomerConnect connect) {
		
		if(connect == null) return null;
		
		return CustomerConnectionResponse.builder()
				.id(connect.getId())
				.isMovingIn(connect.getIsMovingIn())
				.moveInDate(connect.getMoveInDate())
				.submitLater(connect.getSubmitLater())
				.meterNumber(connect.getMeterNumber())
				.marketLocationId(connect.getMarketLocationId())
				.currentProvider(connect.getCurrentProvider())
				.autoCancellation(connect.getAutoCancellation())
				.alreadyCancelled(connect.getAlreadyCancelled())
				.selfCancellation(connect.getSelfCancellation())
				.delivery(connect.getDelivery())
				.desiredDelivery(connect.getDesiredDelivery())
				.build();
	}

}
