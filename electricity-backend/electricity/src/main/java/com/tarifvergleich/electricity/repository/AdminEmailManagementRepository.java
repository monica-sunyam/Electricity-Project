package com.tarifvergleich.electricity.repository;

import com.tarifvergleich.electricity.model.AdminEmailManagement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminEmailManagementRepository extends JpaRepository<AdminEmailManagement, Long> {
}