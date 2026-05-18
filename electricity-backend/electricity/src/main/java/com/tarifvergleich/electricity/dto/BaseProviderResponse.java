package com.tarifvergleich.electricity.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BaseProviderResponse(List<ProviderDataDto> result) {
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record ProviderDataDto(Integer providerId, String providerName, List<RateDto> rates) {
		@JsonIgnoreProperties(ignoreUnknown = true)
		public record RateDto(Integer rateId, String rateName, Double basePriceYear, Double basePriceMonth,
				Double workPrice, Double workPriceNt) {
		}
	}
}
