package com.tarifvergleich.electricity.dto.response;

import com.tarifvergleich.electricity.dto.CustomerDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerQueryContactResponseDTO {
    private Integer customerQueryContactId;
    private String salutation;
    private String title;
    private String firstName;
    private String lastName;
    private String email;
    private String contactNumber;
    private String inquiry;
    private BigInteger createdOn;
    private Boolean isResolved;
    private BigInteger resolvedOn;
    private String categoryName;
    private Integer CategoryId;
    private CustomerDto.CustomerShortDetail customer;
}
