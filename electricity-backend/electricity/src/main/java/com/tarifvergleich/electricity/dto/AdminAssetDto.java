package com.tarifvergleich.electricity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminAssetDto {
	
	private Integer id;
	private Integer adminId;
	private String heading;
	private String subHeading;
	private String contact;

	private Integer order;
	private Integer type;
	private String saving;
	
	private String savingDetail;

}
