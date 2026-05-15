package com.tarifvergleich.electricity.dto;

import java.util.ArrayList;
import java.util.List;

public record EgonFileSignatureResponse() {

	public static record EgonFileSignatureRequest(List<Signatures> signature) {

	}

	public static record Signatures(String signature, String signatureBank, String signatureReseller,
			String signatureCustomer, String signatureDataProtection) {
	}

	public static EgonFileSignatureRequest mapSignatures(String signature, String signatureBank,
			String signatureReseller, String signatureCustomer, String signatureDataProtection) {
		Signatures newSignature = new Signatures(signature, signatureBank, signatureReseller, signatureCustomer,
				signatureDataProtection);
		List<Signatures> signatures = new ArrayList<Signatures>();
		signatures.add(newSignature);
		return new EgonFileSignatureRequest(signatures);
	}

	public static record EgonDocumentDto(String file, // Holds the Base64 string
			MimeTypeDto mimeType, // Nested object for extension and mime
			long bytes // File size in bytes
	) {

		public static record MimeTypeDto(String ext, String mime) {
		}
	}

}
