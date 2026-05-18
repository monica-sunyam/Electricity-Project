package com.tarifvergleich.electricity.service.common;

import com.tarifvergleich.electricity.dto.CustomerDto;
import com.tarifvergleich.electricity.dto.request.CustomerQueryContactRequestDTO;
import com.tarifvergleich.electricity.dto.response.CustomerQueryContactCategoryResponseDTO;
import com.tarifvergleich.electricity.dto.response.CustomerQueryContactResponseDTO;
import com.tarifvergleich.electricity.model.AdminUser;
import com.tarifvergleich.electricity.model.Customer;
import com.tarifvergleich.electricity.model.CustomerQueryContact;
import com.tarifvergleich.electricity.model.CustomerQueryContactCategory;
import com.tarifvergleich.electricity.repository.AdminUserRepository;
import com.tarifvergleich.electricity.repository.CustomerQueryContactCategoryRepository;
import com.tarifvergleich.electricity.repository.CustomerQueryContactRepository;
import com.tarifvergleich.electricity.repository.CustomerRepository;
import com.tarifvergleich.electricity.util.Helper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommonService {

    private final CustomerQueryContactRepository queryContactRepository;
    private final CustomerQueryContactCategoryRepository categoryRepository;
    private final CustomerRepository customerRepository;
    private final AdminUserRepository adminUserRepository;

    public List<CustomerQueryContactCategoryResponseDTO> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(this::mapToCategoryDTO)
                .collect(Collectors.toList());
    }

    public Map<String, Object> getAllCustomers() {

        List<CustomerQueryContactResponseDTO> collect = queryContactRepository.findAll()
                .stream()
                .map(this::mapToCustomerQueryContactDto)
                .collect(Collectors.toList());
        Map<String, Object> map = new HashMap<>();
        map.put("res", true);
        map.put("data", collect);
        return map;

    }

    public Map<String, Object> saveQuery(CustomerQueryContactRequestDTO dto) {
        CustomerQueryContactCategory category = categoryRepository
                .findById(dto.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Category not found with id: " + dto.getCategoryId()));

        Customer customer = customerRepository
                .findById(dto.getCustomerId())
                .orElse(null);

        AdminUser adminUser = adminUserRepository.findById(dto.getAdminId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Admin user not found with id: " + dto.getAdminId()));

        CustomerQueryContact contact = CustomerQueryContact.builder()
                .salutation(dto.getSalutation())
                .title(dto.getTitle())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .phoneNumber(dto.getContactNumber())
                .email(dto.getEmail())
                .query(dto.getInquiry())
                .isResolved(false)
                .createdOn(Helper.getCurrentTimeBerlin())
                .customer(customer)
                .admin(adminUser)
                .queryCategory(category)
                .build();

        CustomerQueryContact saved = queryContactRepository.save(contact);

        Map<String, Object> response = new HashMap<>();
        response.put("res", true);
        response.put("data", mapToCustomerQueryContactDto(saved));


        return response;
    }

    // Category mapper
    private CustomerQueryContactCategoryResponseDTO mapToCategoryDTO(
            CustomerQueryContactCategory category) {
        return CustomerQueryContactCategoryResponseDTO.builder()
                .id(category.getId())
                .categoryName(category.getCategoryName())
                .addedOn(category.getAddedOn())
                .updatedOn(category.getUpdatedOn())
                .build();
    }

    private CustomerQueryContactResponseDTO mapToCustomerQueryContactDto(
            CustomerQueryContact contact) {
        return CustomerQueryContactResponseDTO.builder()
                .customerQueryContactId(contact.getId())
                .salutation(contact.getSalutation())
                .title(contact.getTitle())
                .firstName(contact.getFirstName())
                .lastName(contact.getLastName())
                .email(contact.getEmail())
                .contactNumber(contact.getPhoneNumber())
                .inquiry(contact.getQuery())
                .createdOn(contact.getCreatedOn())
                .isResolved(contact.getIsResolved())
                .resolvedOn(contact.getResolvedOn())
                .categoryName(contact.getQueryCategory() != null
                        ? contact.getQueryCategory().getCategoryName()
                        : null)
                .CategoryId(contact.getQueryCategory() != null ?
                        contact.getQueryCategory().getId() : null)
                .customer(contact.getCustomer() != null ? CustomerDto.customerShortResponse(contact.getCustomer()) : null)
                .build();
    }
}