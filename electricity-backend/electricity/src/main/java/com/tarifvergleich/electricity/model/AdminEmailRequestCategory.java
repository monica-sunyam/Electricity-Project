package com.tarifvergleich.electricity.model;

import jakarta.persistence.*;

import java.math.BigInteger;

import com.tarifvergleich.electricity.util.Helper;

@Entity
@Table(name = "email_request_category")
public class AdminEmailRequestCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cate_id")
    private Long cateId;

    @Column(name = "name")
    private String name;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_date")
    private BigInteger createdDate;

    public Long getCateId() {
        return cateId;
    }

    public void setCateId(Long cateId) {
        this.cateId = cateId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
    
    @PrePersist
    protected void onCreate() {
    	createdDate = Helper.getCurrentTimeBerlin();    
    }
}