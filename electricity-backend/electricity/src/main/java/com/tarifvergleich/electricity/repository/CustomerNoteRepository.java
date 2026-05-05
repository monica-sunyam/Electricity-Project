package com.tarifvergleich.electricity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tarifvergleich.electricity.model.CustomerNote;

@Repository
public interface CustomerNoteRepository extends JpaRepository<CustomerNote, Integer> {

}
