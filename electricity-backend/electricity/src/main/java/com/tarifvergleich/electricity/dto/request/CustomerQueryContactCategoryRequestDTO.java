package com.tarifvergleich.electricity.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomerQueryContactCategoryRequestDTO {
    private String categoryName;
    private Integer adminId;
}
