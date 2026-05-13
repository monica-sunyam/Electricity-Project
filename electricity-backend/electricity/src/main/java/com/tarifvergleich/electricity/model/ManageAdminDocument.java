package com.tarifvergleich.electricity.model;

import java.math.BigInteger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tarifvergleich.electricity.util.Helper;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "manage_admin_documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ManageAdminDocument {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	private String originalFileName;
	
	private String filePath;
	
	@Column(name = "document_category")
	private String documentCategory;
	
	@Column(name = "added_on")
	private BigInteger addedOn;
	
	@Column(name = "last_updated_on")
	private BigInteger lastUpdatedOn;
	
	@ManyToOne
	@JoinColumn(name = "admin_id")
	@JsonIgnore
	private AdminUser admin;
	
	@PrePersist
	protected void onCreate() {
		addedOn = Helper.getCurrentTimeBerlin();
	}
	
	@PreUpdate
	public void updatedOn() {
		lastUpdatedOn = Helper.getCurrentTimeBerlin();
	}
}
