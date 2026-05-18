package com.tarifvergleich.electricity.service.admin;

import com.tarifvergleich.electricity.dto.AdminEmailRequest;
import com.tarifvergleich.electricity.exception.InternalServerException;
import com.tarifvergleich.electricity.model.AdminEmailManagement;
import com.tarifvergleich.electricity.model.ManageAdminDocument;
import com.tarifvergleich.electricity.repository.AdminEmailManagementRepository;
import com.tarifvergleich.electricity.repository.AdminEmailRequestCategoryRepository;
import com.tarifvergleich.electricity.repository.ManageAdminDocumentRepository;
import com.tarifvergleich.electricity.util.Helper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminEmailManagementService {

    @Autowired
    private AdminEmailManagementRepository repository;

    @Autowired
    private ManageAdminDocumentRepository manageAdminDocumentRepository;

    @Autowired
    private AdminEmailRequestCategoryRepository categoryRepository;

    public AdminEmailManagement saveEmail(AdminEmailRequest request) {

        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {

            throw new InternalServerException("Title cannot be empty", HttpStatus.OK);
        }

        if (request.getSubtitle() == null || request.getSubtitle().trim().isEmpty()) {

            throw new InternalServerException("Subtitle cannot be empty", HttpStatus.OK);
        }

        if (request.getEmailContent() == null || request.getEmailContent().trim().isEmpty()) {

            throw new InternalServerException("Email content cannot be empty", HttpStatus.OK);
        }

        if (request.getCateId() == null) {

            throw new InternalServerException("Category must be selected", HttpStatus.OK);
        }

        AdminEmailManagement email = new AdminEmailManagement();

        email.setTitle(request.getTitle());
        email.setSubtitle(request.getSubtitle());
        email.setEmailContent(request.getEmailContent());
        email.setCreatedBy("Admin");
        email.setCreatedDate(Helper.getCurrentTimeBerlin());

        email.setCategory(
                categoryRepository.findById(request.getCateId()).orElse(null)
        );

        AdminEmailManagement savedEmail = repository.save(email);
        
        List<ManageAdminDocument> documents =
                manageAdminDocumentRepository.findAllById(
                        request.getPdfIds().stream().map(Long::intValue).toList()
                );

        savedEmail.setDocuments(documents);

        savedEmail = repository.save(savedEmail);
        System.out.println("FULL REQUEST = " + request);
        System.out.println("PDF IDS = " + request.getPdfIds());

        
        return savedEmail;
    }

    public List<AdminEmailManagement> getAllEmails() {

        return repository.findAll();
    }
}
