package com.tarifvergleich.electricity.model;

import java.math.BigInteger;

import com.tarifvergleich.electricity.util.Helper;

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

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Table(name = "customer_service_request_messages")
@Entity
public class CustomerServiceRequestMessages {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@Column(name = "message", columnDefinition = "TEXT")
	private String message;
	
	@Column(name = "chat_user")
	private String chatUser;
	
	@Column(name = "send_on")
	private BigInteger sendOn;
	
	@ManyToOne
	@JoinColumn
	private CustomerServiceRequest customerServiceRequest;
	
	@PrePersist
	protected void onCreate() {
		sendOn = Helper.getCurrentTimeBerlin();
	}
}
