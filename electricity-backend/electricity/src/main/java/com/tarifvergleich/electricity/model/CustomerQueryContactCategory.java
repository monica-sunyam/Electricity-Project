package com.tarifvergleich.electricity.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tarifvergleich.electricity.util.Helper;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigInteger;
import java.util.List;

@Entity
@Table(name = "customer_query_category")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class CustomerQueryContactCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "category_name")
    private String categoryName;

    @Column(name = "added_on")
    private BigInteger addedOn;

    @Column(name = "updated_on")
    private BigInteger updatedOn;

    @OneToMany(mappedBy = "queryCategory")
    @JsonIgnoreProperties("queryCategory")
    private List<CustomerQueryContact> customerQueryContacts;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    @JsonIgnore
    private AdminUser admin;

    @PrePersist
    protected void onCreate() {
        addedOn = Helper.getCurrentTimeBerlin();
    }

    @PreUpdate
    public void updatedOn() {
        updatedOn = Helper.getCurrentTimeBerlin();
    }
}
