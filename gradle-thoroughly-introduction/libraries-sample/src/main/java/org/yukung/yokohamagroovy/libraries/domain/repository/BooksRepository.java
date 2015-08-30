package org.yukung.yokohamagroovy.libraries.domain.repository;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.yukung.yokohamagroovy.libraries.domain.tables.daos.BooksDao;
import org.yukung.yokohamagroovy.libraries.domain.tables.pojos.Books;
import org.yukung.yokohamagroovy.libraries.domain.tables.records.BooksRecord;

import static org.yukung.yokohamagroovy.libraries.domain.tables.Books.BOOKS;

/**
 * @author yukung
 */
@Repository
public class BooksRepository {

    @Autowired
    private DSLContext dsl;

    public Books findOne(String isbn) {
        return new BooksDao(dsl.configuration()).findById(isbn);
    }

    public Books save(Books books) {
        BooksRecord booksRecord = dsl
                .selectFrom(BOOKS)
                .where(BOOKS.ISBN.eq(books.getIsbn()))
                .fetchOne();
        if (booksRecord == null) {
            booksRecord = dsl.newRecord(BOOKS, books);
        } else {
            booksRecord.from(books);
        }
        booksRecord.store();
        return booksRecord.into(Books.class);
    }

    public void delete(Books books) {
        new BooksDao(dsl.configuration()).delete(books);
    }
}
