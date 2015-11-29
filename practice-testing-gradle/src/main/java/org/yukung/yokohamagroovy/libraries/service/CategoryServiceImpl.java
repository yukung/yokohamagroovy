package org.yukung.yokohamagroovy.libraries.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.yukung.yokohamagroovy.libraries.entity.Category;
import org.yukung.yokohamagroovy.libraries.repository.CategoriesRepository;

/**
 * @author yukung
 */
@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoriesRepository repository;

    @Override
    public Category create(Category category) {
        return repository.save(category);
    }

    @Override
    public Category find(Long categoryId) {
        return repository.findOne(categoryId);
    }

    @Override
    public void update(Category category) {
        repository.save(category);
    }

    @Override
    public void delete(Category category) {

    }
}
