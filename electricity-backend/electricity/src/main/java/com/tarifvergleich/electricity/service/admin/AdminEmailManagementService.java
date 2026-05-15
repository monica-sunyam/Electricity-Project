package com.tarifvergleich.electricity.service.admin;

import com.tarifvergleich.electricity.exception.InternalServerException;
import com.tarifvergleich.electricity.model.AdminEmailAttachment;
import com.tarifvergleich.electricity.model.AdminEmailManagement;
import com.tarifvergleich.electricity.repository.AdminEmailAttachmentRepository;
import com.tarifvergleich.electricity.repository.AdminEmailManagementRepository;
import com.tarifvergleich.electricity.util.FileServiceSuperAdmin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;

@Service
public class AdminEmailManagementService {

	@Autowired
	private AdminEmailManagementRepository repository;
	
	@Autowired
	private FileServiceSuperAdmin fileServiceSuperAdmin;

	@Autowired
	private AdminEmailAttachmentRepository attachmentRepository;

	public AdminEmailManagement saveEmail(AdminEmailManagement emailManagement) {

		if (emailManagement.getTitle() == null || emailManagement.getTitle().trim().isEmpty()) {

			throw new InternalServerException("Title cannot be empty", HttpStatus.OK);
		}

		if (emailManagement.getSubtitle() == null || emailManagement.getSubtitle().trim().isEmpty()) {

			throw new InternalServerException("Subtitle cannot be empty", HttpStatus.OK);

		}

		if (emailManagement.getEmailContent() == null || emailManagement.getEmailContent().trim().isEmpty()) {

			throw new InternalServerException("Email content cannot be empty", HttpStatus.OK);
		}

		if (emailManagement.getCategory() == null) {

//			throw new RuntimeException("Category must be selected");
			throw new InternalServerException("Category must be selected", HttpStatus.OK);

		}

		emailManagement.setCreatedBy("Admin");
		emailManagement.setCreatedDate(Instant.now());
		return repository.save(emailManagement);

	}
	
	public AdminEmailManagement saveEmailWithAttachments( AdminEmailManagement email, MultipartFile[] files	) {

	    AdminEmailManagement savedEmail = repository.save(email);

	    if (files != null) {

	        for (MultipartFile file : files) {

	            String filePath = fileServiceSuperAdmin.saveFilePdf(file, "email");

	            AdminEmailAttachment attachment = new AdminEmailAttachment();

	            attachment.setFilePath(filePath);

	            attachment.setEmailManagement(savedEmail);

	            attachmentRepository.save(attachment);
	        }
	    }

	    return savedEmail;
	}

	public List<AdminEmailManagement> getAllEmails() {
		return repository.findAll();
	}
}