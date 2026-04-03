package com.tarifvergleich.electricity.dto;

import lombok.Data;

@Data
public class AdminAssetDto {
	
	private Integer adminId;
	private String heading;
	private String subHeading;
	private String contentPlace;

	private Integer order;
	private String contentType;

}
