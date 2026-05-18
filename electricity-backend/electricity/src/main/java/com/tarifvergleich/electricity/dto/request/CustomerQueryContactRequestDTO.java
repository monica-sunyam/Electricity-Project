package com.tarifvergleich.electricity.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerQueryContactRequestDTO {
    private String salutation;
    private String title;
    private String firstName;
    private String lastName;
    private String email;
    private String contactNumber;
    private Integer CategoryId;
    private String inquiry;
    private Integer customerId;
    public Integer adminId;
}