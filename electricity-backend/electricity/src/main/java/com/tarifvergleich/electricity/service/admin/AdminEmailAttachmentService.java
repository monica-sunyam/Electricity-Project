package com.tarifvergleich.electricity.service.admin;

import com.tarifvergleich.electricity.model.AdminEmailAttachment;
import com.tarifvergleich.electricity.repository.AdminEmailAttachmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AdminEmailAttachmentService {

    @Autowired
    private AdminEmailAttachmentRepository repository;

    public AdminEmailAttachment saveAttachment(AdminEmailAttachment attachment) {

        attachment.setCreatedDate(Instant.now());

        return repository.save(attachment);
    }
}