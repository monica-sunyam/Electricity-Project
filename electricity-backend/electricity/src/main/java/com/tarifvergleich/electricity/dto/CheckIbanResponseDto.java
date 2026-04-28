package com.tarifvergleich.electricity.dto;

import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;

public record CheckIbanResponseDto(@Schema(example = "ING-DiBa") String bankName,
		@Schema(example = "50010517") String bankIdentifierCode,
		@Schema(example = "0648489890") String bankAccountNumber,
		@Schema(example = "DE12500105170648489890") String iban, @Schema(example = "INGDDEFFXXX") String bic) {

	public record EgonErrorResponse(boolean error, String serviceName, String namespace, String method, String message,
			String code, Map<String, Object> data,
			List<EgonDebugInfo> debug) {
	}

	record EgonDebugInfo(String serviceName, String namespace, String method, Map<String, Object> data) {
	}
}
