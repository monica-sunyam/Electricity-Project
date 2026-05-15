package com.tarifvergleich.electricity.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "email_management")
public class AdminEmailManagement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title")
    private String title;

    @Column(name = "subtitle")
    private String subtitle;

    @Column(name = "email_content", columnDefinition = "TEXT")
    private String emailContent;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_date")
    private Instant createdDate;

    @ManyToOne
    @JoinColumn(name = "cate_id")
    private AdminEmailRequestCategory category;

    public Long getId() {
        return id;
    }

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

	public AdminEmailRequestCategory getCategory() {
		return category;
	}
	
	public void setCategory(AdminEmailRequestCategory category) {
		this.category = category;
	}
	
	public String getCreatedBy() {
		return createdBy;
	}
	
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
	
	public Instant getCreatedDate() {
		return createdDate;
	}
	
	public void setCreatedDate(Instant createdDate) {
		this.createdDate = createdDate;
	}

}