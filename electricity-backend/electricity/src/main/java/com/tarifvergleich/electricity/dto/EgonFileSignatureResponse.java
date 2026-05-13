package com.tarifvergleich.electricity.dto;

public record EgonFileSignatureResponse() {

	public static record EgonDocumentDto(String file, // Holds the Base64 string
			MimeTypeDto mimeType, // Nested object for extension and mime
			long bytes // File size in bytes
	) {

		public static record MimeTypeDto(String ext, String mime) {
		}
	}
	
	public static record uploadDocumentPayloadDto(String file, Integer fileType) {
		
	}
}
