package com.tarifvergleich.electricity.model;

import java.math.BigInteger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tarifvergleich.electricity.util.Helper;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "admin_service_menu")
public class AdminServiceMenu {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@ManyToOne(cascade = CascadeType.MERGE)
	@JoinColumn(name = "adminServiceMenu")
	@JsonIgnore
	private AdminUser adminId;
	
	@Column(name = "content_url")
	private String contentUrl;
	
	@Column(name = "title")
	private String heading;
	
	@Column(name = "description", columnDefinition = "TEXT")
	private String subheading;
	
	@Column(name = "original_file_name")
	private String originalFileName;
	
	@Column(name = "content_service_type", comment = "1- free, 2- other")
	private Integer type;
	
	@Column(name = "content_highlight")
	private Integer highlight;
	
	@Column(name = "service_order")
	private Integer order;
	
	@Column(name = "is_redirect")
	private Boolean isRedirect;
	
	@Column(name = "link")
	private String link;
	
	@Column(name = "created_on")
	private BigInteger createdOn;
	
	@Column(name = "updated_on")
	private BigInteger updatedOn;
	
	@PrePersist
	public void onCreate() {
		createdOn = Helper.getCurrentTimeBerlin();
	}
	
}
