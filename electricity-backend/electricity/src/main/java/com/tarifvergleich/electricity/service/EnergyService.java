package com.tarifvergleich.electricity.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.tarifvergleich.electricity.dto.AdminCreateOrderEgonDto;
import com.tarifvergleich.electricity.dto.AdminCreateOrderEgonDto.OrderListResponse;
import com.tarifvergleich.electricity.dto.BaseProviderResponse;
import com.tarifvergleich.electricity.dto.CheckIbanResponseDto;
import com.tarifvergleich.electricity.dto.EgonFileSignatureResponse.EgonDocumentDto;
import com.tarifvergleich.electricity.dto.EnergyApiResponse;
import com.tarifvergleich.electricity.exception.EnergyApiUnavailableException;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Service
public class EnergyService {

	private final RestClient energyApi;
	private final ObjectMapper objectMapper;

	public EnergyService(@Qualifier("energyApiClient") RestClient energyApi, ObjectMapper objectMapper) {
		this.energyApi = energyApi;
		this.objectMapper = objectMapper;
	}

	public EnergyApiResponse getRates(Map<String, Object> filters) {
		return energyApi.get().uri(uriBuilder -> {
			uriBuilder.path("/rates/");

			if (filters != null) {
				filters.forEach((key, value) -> {
					if (value != null)
						uriBuilder.queryParam(key, value);
				});
			}

			return uriBuilder.build();
		}).retrieve().onStatus(HttpStatusCode::isError, (request, response) -> {
			Map<String, Object> body = objectMapper.readValue(response.getBody(),
					new TypeReference<Map<String, Object>>() {
					});

			throw new EnergyApiUnavailableException("External Provider Error", body);
		}).body(EnergyApiResponse.class);
	}

	public BaseProviderResponse getProviders(Map<String, Object> filters) {
		return energyApi.get().uri(uriBuilder -> {
			uriBuilder.path("/baseProvider/");

			if (filters != null) {
				filters.forEach((key, value) -> {
					if (value != null)
						uriBuilder.queryParam(key, value);
				});
			}

			return uriBuilder.build();

		}).retrieve().onStatus(HttpStatusCode::isError, (request, response) -> {
			Map<String, Object> body = objectMapper.readValue(response.getBody(),
					new TypeReference<Map<String, Object>>() {
					});

			throw new EnergyApiUnavailableException("External Provider Error", body);
		}).body(BaseProviderResponse.class);
	}

	public CheckIbanResponseDto checkIban(String iban) {
		if (iban == null)
			return null;

		return energyApi.get().uri("/checkIban/{iban}", iban).retrieve()
				.onStatus(HttpStatusCode::isError, (request, response) -> {

					Map<String, Object> body = objectMapper.readValue(response.getBody(),
							new TypeReference<Map<String, Object>>() {
							});
					body.put("message", (Object) "Incorrent iban id");
					body.put("code", (Object) 200);
					throw new EnergyApiUnavailableException("Invalid Iban id", body);
				}).body(CheckIbanResponseDto.class);
	}

	public OrderListResponse placeOrder(AdminCreateOrderEgonDto requestPayload) {
		if (requestPayload == null)
			return null;

		return energyApi.put().uri("/order-with-price").body(requestPayload).retrieve()
				.onStatus(HttpStatusCode::isError, (request, response) -> {
					Map<String, Object> body = objectMapper.readValue(response.getBody(),
							new TypeReference<Map<String, Object>>() {
							});
					body.put("message", (Object) "Incorrent iban id");
					body.put("code", (Object) 200);
					throw new EnergyApiUnavailableException("Order cannot be placed", body);
				}).body(OrderListResponse.class);
	}
	
	public EgonDocumentDto createBookingPdf() {
		return null;
	}
	
	
}
