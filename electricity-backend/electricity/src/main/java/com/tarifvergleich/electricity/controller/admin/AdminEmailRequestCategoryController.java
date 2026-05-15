package com.tarifvergleich.electricity.controller.admin;

import com.tarifvergleich.electricity.model.AdminEmailRequestCategory;
import com.tarifvergleich.electricity.service.admin.AdminEmailRequestCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/email-category")
@CrossOrigin("*")
public class AdminEmailRequestCategoryController {

    @Autowired
    private AdminEmailRequestCategoryService service;

    @PostMapping("/save")
    public AdminEmailRequestCategory saveCategory(@RequestBody AdminEmailRequestCategory category) {
        return service.saveCategory(category);
    }

    @GetMapping("/all")
    public List<AdminEmailRequestCategory> getAllCategories() {
        return service.getAllCategories();
    }
}