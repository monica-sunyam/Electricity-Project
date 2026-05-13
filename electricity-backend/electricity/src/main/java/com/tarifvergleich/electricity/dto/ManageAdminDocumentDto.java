package com.tarifvergleich.electricity.dto;

import java.math.BigInteger;

import com.tarifvergleich.electricity.model.ManageAdminDocument;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ManageAdminDocumentDto {

	private Integer adminDocId;
	private String originalFileName;
	private String filePath;
	private String documentCategory;
	private BigInteger addedOn;
	private BigInteger lastUpdatedOn;
	private Integer adminId;
	
	
	@NoArgsConstructor
	@AllArgsConstructor
	@Data
	@Builder
	public static class ManageAdminDocumentResDto {
		private Integer adminDocId;
		private String originalFileName;
		private String filePath;
		private String documentCategory;
		private BigInteger addedOn;
		private BigInteger lastUpdatedOn;
	}
	
	public static ManageAdminDocumentResDto mapForAdmin(ManageAdminDocument adminDoc) {
		if(adminDoc == null) return null;
		
		return ManageAdminDocumentResDto.builder()
				.adminDocId(adminDoc.getId())
				.originalFileName(adminDoc.getOriginalFileName())
				.filePath(adminDoc.getFilePath())
				.documentCategory(adminDoc.getDocumentCategory())
				.addedOn(adminDoc.getAddedOn())
				.lastUpdatedOn(adminDoc.getLastUpdatedOn())
				.build();
	}
}
