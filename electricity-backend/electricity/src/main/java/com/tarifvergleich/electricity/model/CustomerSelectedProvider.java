package com.tarifvergleich.electricity.model;

import java.math.BigInteger;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.tarifvergleich.electricity.util.Helper;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
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
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "customer_selected_provider")
public class CustomerSelectedProvider {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	private String branch;
	
	@Column(name = "netz_provider_id")
	private Long netzProviderId;
	
	@Column(name = "provider_id")
	private Long providerId;
	
	@Column(name = "provider_svg_path")
	private String providerSVGPath;
	
	@Column(name = "provider_name")
	private String providerName;
	
	@Column(name = "rate_id")
	private Long rateId;
	
	@Column(name = "rate_name")
	private String rateName;
	
	@Column(name = "total_price")
	private Double totalPrice;
	
	@Column(name = "total_price_monthly")
	private Double totalPriceMonth;
	
	
	private String type;
	
	@JdbcTypeCode(SqlTypes.JSON)
	@Column(columnDefinition = "jsonb")
	private JsonNode raw;
	
	private BigInteger createdOn;
	
	@OneToOne(mappedBy = "customerProvider")
	@JsonIgnore
	private CustomerDelivery delivery;
	
	@PrePersist
	protected void onCreate() {
		createdOn = Helper.getCurrentTimeBerlin();
	}

}
