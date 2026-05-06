package com.tarifvergleich.electricity.dto;

import java.math.BigInteger;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class CustomerRequestCounsellingDto {

	private Integer cousellingId;
    private String mobileNumber;
    private String weekDay;
    private String timeSlot;
    private String description;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate scheduleDate;
    private BigInteger createdOn;
    private Integer customerId;  
    private Integer adminId;
}
