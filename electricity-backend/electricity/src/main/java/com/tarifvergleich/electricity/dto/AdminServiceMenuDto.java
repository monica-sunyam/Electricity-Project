package com.tarifvergleich.electricity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdminServiceMenuDto {

	private Integer id;            
    private Integer adminId;        
    
    private String heading;         
    private String subheading;  
    
    private Integer type;        
    private Integer highlight;
    
    private Boolean isRedirect;
    private String link;
    
    private Integer order;
}
