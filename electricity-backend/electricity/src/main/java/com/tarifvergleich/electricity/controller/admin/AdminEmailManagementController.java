package com.tarifvergleich.electricity.controller.admin;

import com.tarifvergleich.electricity.model.AdminEmailManagement;
import com.tarifvergleich.electricity.model.AdminEmailRequestCategory;
import com.tarifvergleich.electricity.repository.AdminEmailRequestCategoryRepository;
import com.tarifvergleich.electricity.service.admin.AdminEmailManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/email-management")
@CrossOrigin("*")
public class AdminEmailManagementController {

    @Autowired
    private AdminEmailManagementService service;

    @Autowired
    private AdminEmailRequestCategoryRepository categoryRepository;
    
//    @PostMapping("/save")
//    public AdminEmailManagement saveEmail(@RequestBody AdminEmailManagement emailManagement) {
//        return service.saveEmail(emailManagement);
//    }

    @PostMapping(value = "/save", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> saveEmail(

            @RequestParam("title") String title,
            @RequestParam("subtitle") String subtitle,
            @RequestParam("emailContent") String emailContent,
            @RequestParam("createdBy") String createdBy,
            @RequestParam("cateId") Long cateId,
            @RequestParam(value = "files", required = false)
            MultipartFile[] files

    ) {

        try {

            AdminEmailManagement email =
                    new AdminEmailManagement();

            email.setTitle(title);
            email.setSubtitle(subtitle);
            email.setEmailContent(emailContent);
            email.setCreatedBy(createdBy);

            AdminEmailRequestCategory category =
                    categoryRepository.findById(cateId).orElse(null);

            email.setCategory(category);

            AdminEmailManagement savedEmail =
                    service.saveEmailWithAttachments(email, files);

            return ResponseEntity.ok(savedEmail);

        } catch (Exception e) {

            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/all")
    public List<AdminEmailManagement> getAllEmails() {
        return service.getAllEmails();
    }
}