package com.tarifvergleich.electricity.service.admin;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.tarifvergleich.electricity.dto.CustomerServicesDto;
import com.tarifvergleich.electricity.dto.CustomerServicesDto.CustomerListOfServiceForAdminResDto;
import com.tarifvergleich.electricity.exception.InternalServerException;
import com.tarifvergleich.electricity.model.AdminUser;
import com.tarifvergleich.electricity.model.CustomerServices;
import com.tarifvergleich.electricity.repository.AdminUserRepository;
import com.tarifvergleich.electricity.repository.CustomerServicesRepository;
import com.tarifvergleich.electricity.util.Helper;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminServicePointManagementService {

	private final AdminUserRepository adminUserRepo;
	private final CustomerServicesRepository customerServicesRepo;

	@Transactional
	public Map<String, Object> addCustomerServices(CustomerServicesDto servicesDto) {

		if (servicesDto.getAdminId() == null || servicesDto.getAdminId() <= 0)
			throw new InternalServerException("Admin id missing", HttpStatus.OK);

		if (servicesDto.getServiceName() == null || servicesDto.getServiceName().isEmpty())
			throw new InternalServerException("Service name missing", HttpStatus.OK);
		if (servicesDto.getServiceType() == null || (!servicesDto.getServiceType().equalsIgnoreCase("general")
				&& !servicesDto.getServiceType().equalsIgnoreCase("delivery")
				&& !servicesDto.getServiceType().equalsIgnoreCase("all")))
			throw new InternalServerException("Service type missing", HttpStatus.OK);

		AdminUser admin = adminUserRepo.findById(servicesDto.getAdminId())
				.orElseThrow(() -> new InternalServerException("Admin not found with this credential", HttpStatus.OK));

		CustomerServices service = null;
		if (servicesDto.getServiceId() != null && servicesDto.getServiceId() > 0) {
			service = customerServicesRepo.findById(servicesDto.getServiceId())
					.orElseThrow(() -> new InternalServerException("Invalid service id", HttpStatus.OK));
			if (!service.getAdmin().getAdminId().equals(admin.getAdminId()))
				throw new InternalServerException("Admin and service's admin mis-match", HttpStatus.OK);
			service.setUpdatedOn(Helper.getCurrentTimeBerlin());
		} else {
			service = new CustomerServices();
			service.setAdmin(admin);
		}

		service.setServiceName(servicesDto.getServiceName());
		service.setServiceType(servicesDto.getServiceType().toUpperCase());

		service = customerServicesRepo.save(service);

		return Map.of("res", true, "message", "Service added successfully", "serviceId", service.getId());
	}

	@Transactional
	public Map<String, Object> removeCustomerService(CustomerServicesDto servicesDto) {

		if (servicesDto.getAdminId() == null || servicesDto.getAdminId() <= 0)
			throw new InternalServerException("Admin id missing", HttpStatus.OK);
		if (servicesDto.getServiceId() == null || servicesDto.getServiceId() <= 0)
			throw new InternalServerException("Service is missing", HttpStatus.OK);

		CustomerServices service = customerServicesRepo.findById(servicesDto.getServiceId())
				.orElseThrow(() -> new InternalServerException("Customer service not found", HttpStatus.OK));

		if (!service.getAdmin().getAdminId().equals(servicesDto.getAdminId()))
			throw new InternalServerException("Admin does not contain this customer service", HttpStatus.OK);

		customerServicesRepo.deleteById(servicesDto.getServiceId());

		return Map.of("res", true, "message", "Customer service removed successfully");
	}

	public Map<String, Object> fetchServices(CustomerServicesDto servicesDto) {

		if (servicesDto.getAdminId() == null || servicesDto.getAdminId() <= 0)
			throw new InternalServerException("Admin id missing", HttpStatus.OK);

		if (servicesDto.getServiceId() != null && servicesDto.getServiceId() > 0) {

			CustomerServices service = customerServicesRepo
					.findByIdAndAdminAdminId(servicesDto.getServiceId(), servicesDto.getAdminId()).orElseThrow(
							() -> new InternalServerException("Service not found with this credential", HttpStatus.OK));

			return Map.of("res", true, "data", CustomerServicesDto.mapCustomerServiceForAdmin(service));
		}

		if (servicesDto.getPage() != null) {

			if (servicesDto.getSize() == null || servicesDto.getSize() <= 0)
				servicesDto.setSize(10);

			Pageable pageable = PageRequest.of(servicesDto.getPage() - 1, servicesDto.getSize(),
					Sort.by("addedOn").descending());

			Page<CustomerServices> servicesPage = customerServicesRepo.findAllByAdminAdminId(servicesDto.getAdminId(),
					pageable);

			List<CustomerServices> services = servicesPage.getContent();

			List<CustomerListOfServiceForAdminResDto> servicesResponse = services.stream()
					.map(CustomerServicesDto::mapCustomerServiceForAdmin).toList();

			return Map.of("res", true, "data", servicesResponse, "page", servicesPage.getPageable().getPageNumber() + 1,
					"totalPage", servicesPage.getTotalPages());

		}

		List<CustomerServices> services = customerServicesRepo
				.findAllByAdminAdminIdOrderByAddedOnDesc(servicesDto.getAdminId());

		List<CustomerListOfServiceForAdminResDto> servicesResponse = services.stream()
				.map(CustomerServicesDto::mapCustomerServiceForAdmin).toList();

		return Map.of("res", true, "data", servicesResponse);
	}
}
