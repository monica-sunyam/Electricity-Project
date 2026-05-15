package com.tarifvergleich.electricity.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tarifvergleich.electricity.model.ManageAdminDocument;

@Repository
public interface ManageAdminDocumentRepository extends JpaRepository<ManageAdminDocument, Integer> {

	Optional<ManageAdminDocument> findByIdAndAdminAdminId(Integer id, Integer adminId);

	List<ManageAdminDocument> findAllByAdminAdminIdOrderByDocumentCategoryAsc(Integer adminId);

	Page<ManageAdminDocument> findAllByAdminAdminId(Integer adminId, Pageable pageable);

	List<ManageAdminDocument> findAllByAdminAdminIdAndDocumentCategoryLike(Integer adminId, String documentCategory);
}
