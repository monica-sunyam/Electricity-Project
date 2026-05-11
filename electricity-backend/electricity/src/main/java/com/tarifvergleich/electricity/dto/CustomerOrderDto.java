package com.tarifvergleich.electricity.dto;

import java.math.BigInteger;

import com.tarifvergleich.electricity.model.CustomerOrder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class CustomerOrderDto {
	
	private Integer customerOrderId;
    private Long orderId;
    private Integer orderStatus;
    private Boolean adminPlacedOrder;
    private BigInteger adminOrderPlacedOn;
    private BigInteger createdOn;
    private BigInteger expiryOn;
    private BigInteger lastDateOfCancellation;
    
    private Integer deliveryId;
    private Integer adminId;
    private Integer customerId;
    private Integer bookingDocId;
    
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Data
    public static class CustomerOrderAdminResDto{
    	private Integer customerOrderId;
        private Long orderId;
        private Integer orderStatus;
        private Boolean adminPlacedOrder;
        private BigInteger adminOrderPlacedOn;
        private BigInteger expiryOn;
        private BigInteger lastDateOfCancellation;
        
        private Integer bookingDocId;
    }
    
    public static CustomerOrderAdminResDto mapAdminRes(CustomerOrder order) {
    	if(order == null) return null;
    	
    	return CustomerOrderAdminResDto.builder()
    			.customerOrderId(order.getId())
    			.orderId(order.getOrderId())
    			.orderStatus(order.getOrderStatus())
    			.adminPlacedOrder(order.getAdminPlacedOrder())
    			.adminOrderPlacedOn(order.getAdminOrderPlacedOn())
    			.expiryOn(order.getExpiryOn())
    			.lastDateOfCancellation(order.getLastDateOfCancellation())
    			.bookingDocId(order.getCustomerBookingDocument() != null ? order.getCustomerBookingDocument().getId() : null)
    			.build();
    }
}
