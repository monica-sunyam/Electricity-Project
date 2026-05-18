package com.tarifvergleich.electricity.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tarifvergleich.electricity.util.Helper;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
@Entity
@Data
@Builder
@Table(name = "customer_query")
@NoArgsConstructor
@AllArgsConstructor
public class CustomerQueryContact {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String salutation;

    private String title;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "phone_number")
    private String phoneNumber;

    private String email;

    @Column(name = "query", columnDefinition = "TEXT")
    private String query;

    @Column(name = "created_on")
    private BigInteger createdOn;

    @Column(name = "is_resolved")
    private Boolean isResolved;

    @Column(name = "resolved_on")
    private BigInteger resolvedOn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "query_category")
    @JsonIgnore
    private CustomerQueryContactCategory queryCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    @JsonIgnore
    private AdminUser admin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    @JsonIgnore
    private Customer customer;

    @PrePersist
    protected void onCreate() {
        isResolved = false;
        createdOn = Helper.getCurrentTimeBerlin();
    }
}
