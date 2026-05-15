package com.tarifvergleich.electricity.service.admin;

import com.tarifvergleich.electricity.exception.InternalServerException;
import com.tarifvergleich.electricity.model.AdminEmailRequestCategory;
import com.tarifvergleich.electricity.repository.AdminEmailRequestCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class AdminEmailRequestCategoryService {

    @Autowired
    private AdminEmailRequestCategoryRepository repository;

    public AdminEmailRequestCategory saveCategory(AdminEmailRequestCategory category) {

    	if(category.getName() == null || category.getName().trim().isEmpty()) {
    		throw new InternalServerException("Category name cannot be empty", HttpStatus.OK);
    	}
    	
        category.setCreatedDate(Instant.now());

        return repository.save(category);
    }

    public List<AdminEmailRequestCategory> getAllCategories() {
        return repository.findAll();
    }
}