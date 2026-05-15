package com.tarifvergleich.electricity.repository;

import com.tarifvergleich.electricity.model.AdminStaticContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminStaticContentRepo extends JpaRepository<AdminStaticContent,Long> {
}
