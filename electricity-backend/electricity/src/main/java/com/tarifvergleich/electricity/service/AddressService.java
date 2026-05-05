package com.tarifvergleich.electricity.service;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarifvergleich.electricity.dto.EgonAddressFetchingDto;
import com.tarifvergleich.electricity.dto.EgonAddressFetchingDto.EgonAddressfetchStreet;
import com.tarifvergleich.electricity.dto.GeoapifyFeatureResponse;
import com.tarifvergleich.electricity.dto.GeoapifyResponse;
import com.tarifvergleich.electricity.exception.EnergyApiUnavailableException;
import com.tarifvergleich.electricity.exception.InternalServerException;

@Service
public class AddressService {

	private final RestClient geoApi;

	private final RestClient egonApi;

	private final String apiKey;

	private final ObjectMapper objectMapper;
	
	public AddressService(
		    @Qualifier("geoapifyClient") RestClient geoApi,
		    @Qualifier("energyApiClient") RestClient egonApi,
		    @Value("${api.geoapify.apikey}") String apiKey,
		    @Qualifier("objectMapper") ObjectMapper objectMapper) {
		    this.geoApi = geoApi;
		    this.apiKey = apiKey;
		    this.egonApi = egonApi;
		    this.objectMapper = objectMapper;
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

		return Map.of("res", true, "data", resp.results().stream().filter(distinctByKey(res -> res.city()))
				.map(res -> Map.of("city", res.city(), "city_id", res.placeId())).distinct().toList());
	}

	public Map<String, Object> getStreetsByCity(String placeId) {

		if (placeId == null || placeId.isEmpty()) {
			throw new InternalServerException("Place ID is required", HttpStatus.BAD_REQUEST);
		}

		GeoapifyFeatureResponse resp = geoApi.get().uri(uriBuilder -> {
			uriBuilder.path("/v2/places");

			uriBuilder.queryParam("categories", "building,commercial");
			uriBuilder.queryParam("filter", "place:" + placeId);
			uriBuilder.queryParam("apiKey", apiKey);
			uriBuilder.queryParam("limit", "500"); // Use 50 as a safe standard limit

			URI url = uriBuilder.build();
			System.out.println("Requesting Geoapify URL: " + url);
			return url;
		}).retrieve().onStatus(HttpStatusCode::isError, (request, response) -> {
			throw new InternalServerException("Location api response error", HttpStatus.BAD_REQUEST);
		}).body(GeoapifyFeatureResponse.class);

		if (resp == null || resp.features() == null) {
			return Map.of("res", true, "data", List.of());
		}

		AtomicInteger index = new AtomicInteger(1);

		List<Map<String, Object>> streetData = resp.features().stream().map(feature -> {
			String street = (feature.properties() != null && feature.properties().street() != null)
					? feature.properties().street()
					: "Unknown Street";

			return Map.<String, Object>of("street", street, "street_id", index.getAndIncrement());
		}).filter(m -> !"Unknown Street".equals(m.get("street"))).filter(distinctByKey(m -> m.get("street")))
				.sorted((a, b) -> a.get("street").toString().compareTo(b.get("street").toString())).toList();

		return Map.of("res", true, "data", streetData);
	}

	public Map<String, Object> fetchCities(String zip) {
		EgonAddressFetchingDto resp = egonApi.get().uri("/cities/{zip}", zip).retrieve()
				.onStatus(HttpStatusCode::isError, (request, response) -> {
					Map<String, Object> body = objectMapper.readValue(response.getBody(),
							new TypeReference<Map<String, Object>>() {
							});
					body.put("code", (Object) 200);
					throw new EnergyApiUnavailableException("Invalid Iban id", body);
				}).body(EgonAddressFetchingDto.class);

		AtomicInteger index = new AtomicInteger(1);

		List<Map<String, Object>> cities = resp.result().stream().map(city -> {
			return Map.<String, Object>of("city", city.city(), "city_id", index.getAndIncrement());
		}).filter(distinctByKey(city -> city.get("city"))).sorted((a, b) -> {
			String cityA = (String) a.get("city");
			String cityB = (String) b.get("city");
			return cityA.compareTo(cityB);
		}).toList();

		return Map.of("res", true, "data", cities);

	}

	public Map<String, Object> fetchStreet(String zip, String city) {
		EgonAddressfetchStreet resp = egonApi.get().uri("/streets/{zip}/{city}", zip, city).retrieve()
				.onStatus(HttpStatusCode::isError, (request, response) -> {
					Map<String, Object> body = objectMapper.readValue(response.getBody(),
							new TypeReference<Map<String, Object>>() {
							});
					body.put("code", (Object) 200);
					throw new EnergyApiUnavailableException("Invalid Iban id", body);
				}).body(EgonAddressfetchStreet.class);

		AtomicInteger index = new AtomicInteger(1);

		List<Map<String, Object>> streets = resp.result().stream().map(street -> {
			return Map.<String, Object>of("street", street.street(), "street_id", index.getAndIncrement());
		}).filter(distinctByKey(street -> street.get("street"))).sorted((a, b) -> {
			String streetA = a.get("street").toString();
			String streetB = b.get("street").toString();
			return streetA.compareTo(streetB);
		}).toList();

		return Map.of("res", true, "data", streets);
	}

	public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
		Set<Object> seen = ConcurrentHashMap.newKeySet();
		return t -> seen.add(keyExtractor.apply(t));
	}
}
