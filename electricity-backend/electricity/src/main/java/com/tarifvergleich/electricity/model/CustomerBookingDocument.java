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
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "customer_booking_documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerBookingDocument {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "original_file_name")
	private String originalFileName;

	@Column(name = "file_url")
	private String fileUrl;

	@Column(name = "signed_original_file_name")
	private String signedOriginalFileName;

	@Column(name = "signed_file_url")
	private String signedFileUrl;

	@Column(name = "signed_document_submitted")
	private Boolean signedDocumentSubmitted;

	@Column(name = "added_on")
	private BigInteger addedOn;

	@Column(name = "updated_on")
	private BigInteger updatedOn;

	@ManyToOne
	@JoinColumn(name = "admin_id")
	@JsonIgnore
	private AdminUser admin;

	@ManyToOne
	@JoinColumn(name = "customer_id")
	@JsonIgnore
	private Customer customer;

	@OneToOne(mappedBy = "customerBookingDocument")
	@JsonIgnore
	private CustomerDelivery customerDelivery;

	@PrePersist
	protected void onCreate() {
		addedOn = Helper.getCurrentTimeBerlin();
	}

	@PreUpdate
	public void onUpdate() {
		updatedOn = Helper.getCurrentTimeBerlin();
	}
}
