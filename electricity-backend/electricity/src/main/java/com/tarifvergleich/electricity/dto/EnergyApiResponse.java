package com.tarifvergleich.electricity.dto;

import java.util.List;

import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
public class EnergyApiResponse {
	
	private List<EnergyRateDto> result;
    private int total;
}
