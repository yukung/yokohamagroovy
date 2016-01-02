package org.yukung.yokohamagroovy.libraries.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.yukung.yokohamagroovy.libraries.entity.Category;
import org.yukung.yokohamagroovy.libraries.service.category.CategoryService;

/**
 * @author yukung
 */
@RestController
@RequestMapping("/api/categories")
public class CategoryRestController {

    @Autowired
    private CategoryService categoryService;

    @RequestMapping(method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public Category postCategories(@RequestBody Category category) {
        return categoryService.create(category);
    }
}
