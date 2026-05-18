package com.tarifvergleich.electricity.controller.admin;

import com.tarifvergleich.electricity.model.AdminStaticContent;
import com.tarifvergleich.electricity.service.admin.AdminStaticContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminStaticContentController {

    private final AdminStaticContentService adminStaticContentService;

    @PostMapping("/static-add")
    public ResponseEntity<?> addContent(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam(value = "file", required = false) MultipartFile file) {

        AdminStaticContent content = new AdminStaticContent();
        content.setTitle(title);
        content.setDescription(description);

        try {
            return ResponseEntity.ok(adminStaticContentService.saveContent(content, file));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/static-all")
    public ResponseEntity<List<AdminStaticContent>> getAllContent() {
        return ResponseEntity.ok(adminStaticContentService.getAllContent());
    }

    @PostMapping("/static-update/{id}")
    public ResponseEntity<AdminStaticContent> updateContent(
            @PathVariable Long id,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam(value = "file", required = false) MultipartFile file) {

        AdminStaticContent content = new AdminStaticContent();
        content.setTitle(title);
        content.setDescription(description);

        try {
            AdminStaticContent updatedContent = adminStaticContentService.updateContent(id, content, file);
            return ResponseEntity.ok(updatedContent);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/static-delete/{id}")
    public ResponseEntity<Map<String, String>> deleteContent(@PathVariable Long id) {
        adminStaticContentService.deleteContent(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Content successfully deleted");
        return ResponseEntity.ok(response);
    }
}