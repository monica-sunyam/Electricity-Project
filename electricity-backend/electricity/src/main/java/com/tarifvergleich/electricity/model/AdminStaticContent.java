package com.tarifvergleich.electricity.model;

import com.tarifvergleich.electricity.util.Helper;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigInteger;

@Entity
@Data
@Table(name = "admin_static_content")
@NoArgsConstructor
@AllArgsConstructor
public class AdminStaticContent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title")
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_path")
    private String logoPath;

    @Column(name = "upload_time")
    private BigInteger uploadTime;

    @PrePersist
    protected  void onCreate(){
        uploadTime = Helper.getCurrentTimeBerlin();
    }
}