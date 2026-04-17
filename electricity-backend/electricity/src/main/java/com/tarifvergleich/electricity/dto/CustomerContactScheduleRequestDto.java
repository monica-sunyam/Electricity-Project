package com.tarifvergleich.electricity.dto;

import com.tarifvergleich.electricity.model.CustomerContactSchedule;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerContactScheduleRequestDto {

	private Integer id;
	private Integer customerId;
	private Integer deliveryId;
    private String dayOfWeek;
    private String timeSlot;
    private String description;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CustomerContactScheduleResponse {
    	private Integer id;
        private String dayOfWeek;
        private String timeSlot;
        private String description;
    }
    
    public static CustomerContactScheduleResponse getContactScheduleResponse(CustomerContactSchedule schedule) {
    	
    	if(schedule == null) return null;
    	
    	return CustomerContactScheduleResponse.builder()
    			.id(schedule.getId())
    			.dayOfWeek(schedule.getDayOfWeek())
    			.timeSlot(schedule.getTimeSlot())
    			.description(schedule.getDescription())
    			.build();
    }
}
