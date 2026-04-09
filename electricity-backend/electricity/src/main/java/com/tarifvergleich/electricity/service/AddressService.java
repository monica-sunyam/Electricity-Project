package com.tarifvergleich.electricity.service;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.tarifvergleich.electricity.dto.GeoapifyFeatureResponse;
import com.tarifvergleich.electricity.dto.GeoapifyResponse;
import com.tarifvergleich.electricity.exception.InternalServerException;

@Service
public class AddressService {

	private final RestClient geoApi;
	private final String apiKey;

	public AddressService(@Qualifier("geoapifyClient") RestClient geoApi,
			@Value("${api.geoapify.apikey}") String apiKey) {
		this.geoApi = geoApi;
		this.apiKey = apiKey;
	}

	public Map<String, Object> getCitiesByZip(String zip) {

		if (zip == null || zip.isEmpty())
			throw new InternalServerException("Zip code not found", HttpStatus.BAD_REQUEST);

		GeoapifyResponse resp = geoApi.get().uri(uriBuilder -> {
			uriBuilder.path("/v1/geocode/search");

			uriBuilder.queryParam("postcode", zip);
			uriBuilder.queryParam("country", "germany");
			uriBuilder.queryParam("format", "json");
			uriBuilder.queryParam("apiKey", apiKey);

			return uriBuilder.build();
		}).retrieve().onStatus(HttpStatusCode::isError, (request, response) -> {
			throw new InternalServerException("Location api response error", HttpStatus.BAD_REQUEST);
		}).body(GeoapifyResponse.class);

		return Map.of("res", true, "data", resp.results().stream()
				.map(res -> Map.of("city", res.city(), "city_id", res.placeId())).distinct().toList());
	}

	public Map<String, Object> getStreetsByCity(String placeId) {

		if (placeId == null || placeId.isEmpty())
			throw new InternalServerException("Zip code not found", HttpStatus.BAD_REQUEST);

		GeoapifyFeatureResponse resp = geoApi.get().uri(uriBuilder -> {
			uriBuilder.path("/v2/places");

			uriBuilder.queryParam("categories", "building");
			uriBuilder.queryParam("filter", "place:" + placeId);
			uriBuilder.queryParam("apiKey", apiKey);

			return uriBuilder.build();
		}).retrieve().onStatus(HttpStatusCode::isError, (request, response) -> {
			throw new InternalServerException("Location api response error", HttpStatus.BAD_REQUEST);
		}).body(GeoapifyFeatureResponse.class);
		
		AtomicInteger index = new AtomicInteger(1);

		return Map.of("res", true, "data", resp.features().stream().map(res -> {
			return Map.of("street", res.properties().name(), "street_id", index.getAndIncrement());
		}).toList());
	}
}
