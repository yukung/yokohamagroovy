package org.yukung.yokohamagroovy.libraries.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import org.yukung.yokohamagroovy.libraries.entity.Book;

import javax.annotation.PostConstruct;
import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * @author yukung
 */
@Repository
public class BooksRepository {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    private SimpleJdbcInsert insert;

    private RowMapper<Book> mapper = (rs, rowNum) -> {
        String isbn = rs.getString("isbn");
        String bookTitle = rs.getString("book_title");
        LocalDate dateOfPublication = rs.getDate("date_of_publication").toLocalDate();
        return new Book(isbn, bookTitle, dateOfPublication);
    };

    @PostConstruct
    void init() {
        insert = new SimpleJdbcInsert((JdbcTemplate) jdbcTemplate.getJdbcOperations())
                .withTableName("books");
    }

    public Book save(Book book) {
//        SqlParameterSource param = new BeanPropertySqlParameterSource(book);
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("isbn", book.getIsbn());
        paramMap.put("bookTitle", book.getBookTitle());
        paramMap.put("dateOfPublication", Date.valueOf(book.getDateOfPublication()));
        SqlParameterSource param = new MapSqlParameterSource(paramMap);
        insert.execute(param);
        return book;
    }

    public Book findOne(String isbn) {
        SqlParameterSource param = new MapSqlParameterSource("isbn", isbn);
        return jdbcTemplate.queryForObject(
                "SELECT isbn, book_title, date_of_publication " +
                        "FROM books WHERE isbn = :isbn",
                param, mapper);
    }
}
