package com.tarifvergleich.electricity.repository;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tarifvergleich.electricity.model.ListOfHolidays;

@Repository
public interface ListOfHolidaysRepository extends JpaRepository<ListOfHolidays, Integer> {

	List<ListOfHolidays> findAllByAdminAdminIdAndRangeIdOrderByIdAsc(Integer adminId, String rangeId);
	
	Boolean existsByAdminAdminIdAndStartDate(Integer adminId, BigInteger startDate);
	Boolean existsByIdAndAdminAdminId(Long id, Integer adminId);
	
	Optional<ListOfHolidays> findByAdminAdminIdAndStartDate(Integer adminId, BigInteger startDate);
	
	Optional<ListOfHolidays> findFirstByRangeIdOrderByIdDesc(String rangeId);
	
	List<ListOfHolidays> findAllByRangeId(String rangeId);
	
	List<ListOfHolidays> findAllByAdminAdminIdAndYearOrderByStartDateAsc(Integer adminId, Integer year);
	
	void deleteByIdAndAdminAdminId(Long id, Integer adminId);
	
}
