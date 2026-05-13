package com.tarifvergleich.electricity.service.admin;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.tarifvergleich.electricity.dto.CustomerOrderDto;
import com.tarifvergleich.electricity.dto.EgonFileSignatureResponse.EgonDocumentDto;
import com.tarifvergleich.electricity.exception.InternalServerException;
import com.tarifvergleich.electricity.model.CustomerBookingDocument;
import com.tarifvergleich.electricity.model.CustomerDelivery;
import com.tarifvergleich.electricity.model.CustomerOrder;
import com.tarifvergleich.electricity.repository.CustomerBookingDocumentRepository;
import com.tarifvergleich.electricity.repository.CustomerOrderRepository;
import com.tarifvergleich.electricity.service.EnergyService;
import com.tarifvergleich.electricity.util.FileServiceCustomer;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AsyncServiceAdmin {

	private final EnergyService energyService;
	private final FileServiceCustomer fileServiceCustomer;
	private final CustomerOrderRepository customerOrderRepo;
	private final CustomerBookingDocumentRepository customerBookingDocumentRepo;

	@Async
	@Transactional
	public void downloadUnsignedPdf(CustomerOrderDto customerOrderDto) {
		if (customerOrderDto.getAdminId() == null || customerOrderDto.getAdminId() <= 0)
			throw new InternalServerException("Admin id missing", HttpStatus.OK);
		if (customerOrderDto.getCustomerOrderId() == null || customerOrderDto.getCustomerOrderId() <= 0)
			throw new InternalServerException("Customer order id missing", HttpStatus.OK);

		CustomerOrder order = customerOrderRepo
				.findByIdAndAdminAdminId(customerOrderDto.getCustomerOrderId(), customerOrderDto.getAdminId())
				.orElseThrow(() -> new InternalServerException("Customer order not found with this credential",
						HttpStatus.OK));

		CustomerDelivery delivery = order.getDelivery();

		if (!delivery.getOrderPlaced() || delivery.getOrderNo() == null || delivery.getOrderNo() <= 0)
			throw new InternalServerException("Incomplete order", HttpStatus.OK);

		CustomerBookingDocument bookingDoc = null;

		bookingDoc = CustomerBookingDocument.builder().orderNo(delivery.getOrderNo()).customer(delivery.getCustomerId())
				.customerDelivery(delivery).admin(delivery.getAdmin()).customerOrder(order).build();

		EgonDocumentDto egonBookingResponse = energyService.createBookingPdf(delivery.getOrderNo().toString());

		try {
			String fileName = delivery.getFirstName() + delivery.getUniqueDeliveryId();
			String unsignedUrlPath = fileServiceCustomer.saveBase64Pdf(egonBookingResponse.file(), fileName,
					"customer-unsigned-documents");
			bookingDoc.setUnsignedOriginalFileName(fileName);
			bookingDoc.setFileUrl(unsignedUrlPath);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}

		bookingDoc = customerBookingDocumentRepo.save(bookingDoc);

		delivery.setCustomerBookingDocument(bookingDoc);
		order.setCustomerBookingDocument(bookingDoc);
		order.setDelivery(delivery);

		customerOrderRepo.save(order);

	}
}
