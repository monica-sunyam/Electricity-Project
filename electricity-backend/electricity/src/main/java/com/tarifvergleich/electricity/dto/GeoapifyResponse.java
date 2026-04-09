package com.tarifvergleich.electricity.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GeoapifyResponse(
    List<GeoResult> results
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GeoResult(
        String name,
        String country,
        @JsonProperty("country_code") String countryCode,
        String city,
        String postcode,
        String district,
        String suburb,
        Double lon,
        Double lat,
        @JsonProperty("result_type") String result_type,
        String formatted,
        @JsonProperty("address_line1") String addressLine1,
        @JsonProperty("address_line2") String addressLine2,
        String category,
        Timezone timezone,
        Rank rank,
        @JsonProperty("place_id")
        String placeId,
        BoundingBox bbox,
        Datasource datasource
    ) {}

    public record Timezone(
        String name,
        @JsonProperty("offset_STD") String offsetStd,
        @JsonProperty("offset_DST") String offsetDst,
        String abbreviation_STD,
        String abbreviation_DST
    ) {}

    public record Rank(
        Double importance,
        Double popularity,
        Integer confidence,
        @JsonProperty("match_type") String matchType
    ) {}

    public record BoundingBox(
        @JsonProperty("lon1") Double lon1,
        @JsonProperty("lat1") Double lat1,
        @JsonProperty("lon2") Double lon2,
        @JsonProperty("lat2") Double lat2
    ) {}

    public record Datasource(
        String sourcename,
        String attribution,
        String url
    ) {}
}