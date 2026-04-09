package com.tarifvergleich.electricity.model;

import java.math.BigInteger;
import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
@Table(name = "menus")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdminAsset {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@ManyToOne(cascade = CascadeType.MERGE)
	@JoinColumn(name = "adminAssets")
	@JsonIgnore
	private AdminUser adminId;
	
	@Column(name="content_type")
	private Integer type;
	
	@Column(name = "content_url")
	private String contentUrl;
	
	@Column(name = "original_file_name")
	private String originalFileName;
	
	@Column(name = "contact_number")
	private String contactNumber;
	
	@Column(name = "saving")
	private String saving;
	
	@Column(name = "saving_price_detail", columnDefinition = "TEXT")
	private String savingPriceDetail;
	
	@Column(name = "created_on")
	private BigInteger createdOn;
	
	@Column(name = "updated_on")
	private BigInteger updatedOn;
	
	private String heading;
	
	@Column(name = "sub_heading", columnDefinition = "TEXT")
	private String subHeading;
	
	@Column(name = "asset_order")
	private Integer order;
	
	@PrePersist
	protected void onCreate() {
		createdOn = BigInteger.valueOf(Instant.now().getEpochSecond());			
	}
}
