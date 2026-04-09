package com.tarifvergleich.electricity.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GeoapifyFeatureResponse(
    String type,
    List<Feature> features
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Feature(
        String type,
        Properties properties,
        Geometry geometry
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Properties(
        String name,
        String country,
        @JsonProperty("country_code") String countryCode,
        String city,
        String postcode,
        String district,
        String suburb,
        String street,
        String housenumber,
        String formatted,
        @JsonProperty("address_line1") String addressLine1,
        @JsonProperty("address_line2") String addressLine2,
        List<String> categories,
        String website,
        @JsonProperty("opening_hours") String openingHours,
        String operator,
        String description,
        @JsonProperty("place_id") String placeId,
        Facilities facilities,
        Building building
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Geometry(
        String type,
        List<Double> coordinates // [longitude, latitude]
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Facilities(
        Boolean wheelchair
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Building(
        Integer levels,
        String type,
        @JsonProperty("start_date") Object startDate // Can be String or Integer in JSON
    ) {}
}