package com.tarifvergleich.electricity.controller.admin;

import com.tarifvergleich.electricity.dto.AdminEmailRequest;
import com.tarifvergleich.electricity.model.AdminEmailManagement;
//import com.tarifvergleich.electricity.model.AdminEmailRequestCategory;
//import com.tarifvergleich.electricity.repository.AdminEmailRequestCategoryRepository;
import com.tarifvergleich.electricity.service.admin.AdminEmailManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/email-management")
@CrossOrigin("*")
public class AdminEmailManagementController {

    @Autowired
    private AdminEmailManagementService service;
    
//    @Autowired
//    private AdminEmailRequestCategoryRepository categoryRepository;
    
    @PostMapping("/save")
    public AdminEmailManagement saveEmail(
            @RequestBody AdminEmailRequest request
    ) {

//        AdminEmailManagement email = new AdminEmailManagement();
//
//        email.setTitle(request.getTitle());
//        email.setSubtitle(request.getSubtitle());
//        email.setEmailContent(request.getEmailContent());
//        email.setCreatedBy(request.getCreatedBy());
//
//        AdminEmailRequestCategory category =
//                categoryRepository.findById(request.getCateId()).orElse(null);
//
//        email.setCategory(category);

        return service.saveEmail(request);
    }

    @GetMapping("/all")
    public List<AdminEmailManagement> getAllEmails() {
        return service.getAllEmails();
    }
}