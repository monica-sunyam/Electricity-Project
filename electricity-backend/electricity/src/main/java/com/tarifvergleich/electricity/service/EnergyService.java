package com.tarifvergleich.electricity.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.tarifvergleich.electricity.dto.EnergyApiResponse;
import com.tarifvergleich.electricity.exception.EnergyApiUnavailableException;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Service
public class EnergyService {

	private final RestClient energyApi;
	private final ObjectMapper objectMapper;
	
	public EnergyService(
            @Qualifier("energyApiClient") RestClient energyApi, 
            ObjectMapper objectMapper) {
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
}
