package com.tarifvergleich.electricity.model;

import java.math.BigInteger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tarifvergleich.electricity.util.Helper;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "list_of_holidays")
@Builder
@Entity
public class ListOfHolidays {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private String name;
	
	private BigInteger startDate;
	private BigInteger endDate;
	
	private Integer year;
	
	@Column(name = "holiday_type", comment = "PUBLIC, COMPANY, OPTION") // SET PUBLIC FOR PUBLIC HOLIDAY AND COMAPNY FOR COMPANY RELATED HOLIDAY AND OPTION FOR OTHER RELATED
	private String holidayType;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "admin_id")
	@JsonIgnore
	private AdminUser admin;
	
	private BigInteger createdOn;
	private BigInteger updatedOn;
	
	@Column(name = "range_id")
	private String rangeId;
	
	@PrePersist
	protected void onCreate() {
		createdOn = Helper.getCurrentTimeBerlin();
	}
	
	@PreUpdate
	public void onUpdate() {
		updatedOn = Helper.getCurrentTimeBerlin();
	}
	
}
