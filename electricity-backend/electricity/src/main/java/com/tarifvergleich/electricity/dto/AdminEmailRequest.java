package com.tarifvergleich.electricity.dto;

import java.util.List;

public class AdminEmailRequest {

    private String title;
    private String subtitle;
    private String emailContent;
    private String createdBy;

    private Long cateId;

    private List<Long> pdfIds;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getEmailContent() {
        return emailContent;
    }

    public void setEmailContent(String emailContent) {
        this.emailContent = emailContent;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Long getCateId() {
        return cateId;
    }

    public void setCateId(Long cateId) {
        this.cateId = cateId;
    }

    public List<Long> getPdfIds() {
        return pdfIds;
    }

    public void setPdfIds(List<Long> pdfIds) {
        this.pdfIds = pdfIds;
    }
}