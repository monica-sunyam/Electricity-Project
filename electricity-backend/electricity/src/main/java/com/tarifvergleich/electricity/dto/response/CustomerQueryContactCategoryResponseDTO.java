package com.tarifvergleich.electricity.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerQueryContactCategoryResponseDTO {

    private Integer id;
    private String categoryName;
    private BigInteger addedOn;
    private BigInteger updatedOn;
}