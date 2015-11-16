package org.yukung.yokohamagroovy.libraries.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import org.yukung.yokohamagroovy.libraries.entity.Category;

import javax.annotation.PostConstruct;

/**
 * @author yukung
 */
@Repository
public class CategoriesRepository {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    private SimpleJdbcInsert insert;

    @PostConstruct
    void init() {
        insert = new SimpleJdbcInsert((JdbcTemplate) jdbcTemplate.getJdbcOperations())
                .withTableName("categories")
                .usingGeneratedKeyColumns("CATEGORY_ID");
    }

    public Category save(Category category) {
        SqlParameterSource param =
                new BeanPropertySqlParameterSource(category);
        if (category.getCategoryId() == null) {
            Number key = insert.executeAndReturnKey(param);
            category.setCategoryId(key.longValue());
        } else {
            jdbcTemplate.update(
                    "UPDATE categories " +
                            "SET category_name = :categoryName " +
                            "WHERE category_id = :categoryId"
                    , param);
        }
        return category;
    }

    public Category findOne(Long categoryId) {
        SqlParameterSource param = new MapSqlParameterSource("id", categoryId);
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT category_id, category_name " +
                            "FROM categories WHERE category_id = :id"
                    , param, new BeanPropertyRowMapper<>(Category.class)
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public void delete(Long categoryId) {
        SqlParameterSource param = new MapSqlParameterSource("id", categoryId);
        jdbcTemplate.update("DELETE FROM categories WHERE category_id = :id", param);
    }
}
