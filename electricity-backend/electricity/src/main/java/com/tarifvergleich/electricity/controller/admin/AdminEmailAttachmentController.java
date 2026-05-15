package com.tarifvergleich.electricity.controller.admin;

import com.tarifvergleich.electricity.model.AdminEmailAttachment;
import com.tarifvergleich.electricity.service.admin.AdminEmailAttachmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/email-attachment")
@CrossOrigin("*")
public class AdminEmailAttachmentController {

    @Autowired
    private AdminEmailAttachmentService service;

    @PostMapping("/save")
    public AdminEmailAttachment saveAttachment(@RequestBody AdminEmailAttachment attachment) {
        return service.saveAttachment(attachment);
    }
}