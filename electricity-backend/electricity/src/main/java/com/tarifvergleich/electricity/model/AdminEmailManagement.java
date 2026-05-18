package com.tarifvergleich.electricity.model;

import jakarta.persistence.*;

import java.util.List;

import jakarta.persistence.ManyToMany;
import jakarta.persistence.JoinTable;
import jakarta.persistence.JoinColumn;

import java.math.BigInteger;

import com.tarifvergleich.electricity.util.Helper;

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
    private BigInteger createdDate;

    @ManyToOne
    @JoinColumn(name = "cate_id")
    private AdminEmailRequestCategory category;
    
    @ManyToMany
    @JoinTable(
        name = "email_management_documents", joinColumns = @JoinColumn(name = "email_management_id"),
        inverseJoinColumns = @JoinColumn(name = "document_id")
    )
    private List<ManageAdminDocument> documents;

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
	
	public BigInteger getCreatedDate() {
		return createdDate;
	}
	
	public void setCreatedDate(BigInteger createdDate) {
		this.createdDate = createdDate;
	}
	
	public List<ManageAdminDocument> getDocuments() {
	    return documents;
	}

	public void setDocuments(List<ManageAdminDocument> documents) {
	    this.documents = documents;
	}
	
	@PrePersist
	protected void onCreate() {
		createdDate = Helper.getCurrentTimeBerlin();
	}

}