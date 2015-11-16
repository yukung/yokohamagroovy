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
import org.yukung.yokohamagroovy.libraries.entity.Author;

import javax.annotation.PostConstruct;

/**
 * @author yukung
 */
@Repository
public class AuthorsRepository {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    private SimpleJdbcInsert insert;

    @PostConstruct
    void init() {
        insert = new SimpleJdbcInsert((JdbcTemplate) jdbcTemplate.getJdbcOperations())
                .withTableName("authors")
                .usingGeneratedKeyColumns("author_id");
    }

    public Author save(Author author) {
        SqlParameterSource param = new BeanPropertySqlParameterSource(author);
        if (author.getAuthorId() == null) {
            Number key = insert.executeAndReturnKey(param);
            author.setAuthorId(key.longValue());
        } else {
            jdbcTemplate.update(
                    "UPDATE authors " +
                            "SET author_firstname = :authorFirstname," +
                            " author_surname = :authorSurname " +
                            "WHERE author_id = :authorId",
                    param);
        }
        return author;
    }

    public Author findOne(Long authorId) {
        SqlParameterSource param = new MapSqlParameterSource("id", authorId);
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT author_id, author_firstname, author_surname " +
                            "FROM authors " +
                            "WHERE author_id = :id",
                    param, new BeanPropertyRowMapper<>(Author.class));
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public void delete(Long authorId) {
        SqlParameterSource param = new MapSqlParameterSource("id", authorId);
        jdbcTemplate.update("DELETE FROM authors WHERE author_id = :id", param);
    }
}
