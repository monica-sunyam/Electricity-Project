package com.tarifvergleich.electricity.repository;

import com.tarifvergleich.electricity.model.AdminEmailAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminEmailAttachmentRepository extends JpaRepository<AdminEmailAttachment, Long> {
}