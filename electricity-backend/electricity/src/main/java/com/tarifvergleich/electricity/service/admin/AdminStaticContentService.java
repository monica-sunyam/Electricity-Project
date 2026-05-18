package com.tarifvergleich.electricity.service.admin;

import com.tarifvergleich.electricity.exception.InternalServerException;
import com.tarifvergleich.electricity.model.AdminStaticContent;
import com.tarifvergleich.electricity.repository.AdminStaticContentRepo;
import com.tarifvergleich.electricity.util.FileServiceSuperAdmin;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminStaticContentService {
    private final AdminStaticContentRepo adminStaticContentRepository;
    private final FileServiceSuperAdmin fileServiceSuperAdmin;

    public Map<String, Object> saveContent(AdminStaticContent content, MultipartFile file) throws IOException {
        if (content == null)
            throw new InternalServerException("Content missing", HttpStatus.OK);
        if (content.getTitle() == null || content.getTitle().isEmpty())
            throw new InternalServerException("Title missing", HttpStatus.OK);
        if (content.getDescription() == null || content.getDescription().isEmpty())
            throw new InternalServerException("Description missing", HttpStatus.OK);

        String contentPath = fileServiceSuperAdmin.saveFile(file, "static-content");

        content.setLogoPath(contentPath);
        adminStaticContentRepository.save(content);
        return Map.of("res", true, "data", content);
    }

    public List<AdminStaticContent> getAllContent() {
        return adminStaticContentRepository.findAll(Sort.by(Sort.Direction.DESC, "uploadTime"));
    }

    public AdminStaticContent updateContent(Long id, AdminStaticContent updatedContent, MultipartFile file) throws IOException {
        AdminStaticContent existingContent = adminStaticContentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Content not found with id: " + id));

        existingContent.setTitle(updatedContent.getTitle());
        existingContent.setDescription(updatedContent.getDescription());

        if (file != null && !file.isEmpty()) {
            String contentPath = fileServiceSuperAdmin.saveFile(file, "static-content");
            existingContent.setLogoPath(contentPath);
        }
        return adminStaticContentRepository.save(existingContent);
    }

    public void deleteContent(Long id) {
        adminStaticContentRepository.deleteById(id);
    }
}