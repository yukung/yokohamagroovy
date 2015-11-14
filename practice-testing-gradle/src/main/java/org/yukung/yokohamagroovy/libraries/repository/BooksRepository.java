package org.yukung.yokohamagroovy.libraries.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import org.yukung.yokohamagroovy.libraries.entity.Book;

import javax.annotation.PostConstruct;

/**
 * @author yukung
 */
@Repository
public class BooksRepository {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    private SimpleJdbcInsert insert;

    @PostConstruct
    void init() {
        insert = new SimpleJdbcInsert((JdbcTemplate) jdbcTemplate.getJdbcOperations())
                .withTableName("books");
    }

    public Book save(Book book) {
        SqlParameterSource param = new BeanPropertySqlParameterSource(book);
        insert.execute(param);
        return book;
    }
}
