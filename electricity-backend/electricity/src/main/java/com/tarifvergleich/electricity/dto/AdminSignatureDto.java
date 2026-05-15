package com.tarifvergleich.electricity.dto;

import java.math.BigInteger;

import com.tarifvergleich.electricity.model.AdminSignature;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class AdminSignatureDto {

	private Integer adminSignatureId;
	private String originalFileName;
	private String filePath;
	private BigInteger addedOn;
	private BigInteger lastUpdatedOn;
	private Integer adminId;

	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	@Data
	public static class AdminSignatureResponseDto {
		private Integer adminSignatureId;
		private String originalFileName;
		private String filePath;
		private BigInteger addedOn;
		private BigInteger lastUpdatedOn;
	}

	public static AdminSignatureResponseDto mapSignatureResponse(AdminSignature signature) {
		if (signature == null)
			return null;

		return AdminSignatureResponseDto.builder().adminSignatureId(signature.getId())
				.originalFileName(signature.getOriginalFileName()).filePath(signature.getFilePath())
				.addedOn(signature.getAddedOn()).lastUpdatedOn(signature.getLastUpdatedOn()).build();
	}
}
