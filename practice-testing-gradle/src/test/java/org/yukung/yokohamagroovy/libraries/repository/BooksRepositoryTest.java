package org.yukung.yokohamagroovy.libraries.repository;

import jp.classmethod.testing.database.DbUnitTester;
import jp.classmethod.testing.database.Fixture;
import jp.classmethod.testing.database.YamlDataSet;
import org.dbunit.dataset.IDataSet;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.yukung.yokohamagroovy.libraries.LibrariesApplication;
import org.yukung.yokohamagroovy.libraries.entity.Book;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * @author yukung
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(LibrariesApplication.class)
@Fixture(resources = "/fixtures/books/books.yml")
public class BooksRepositoryTest {

    private static final Date PUBLISH_DATE = Date.from(LocalDate.of(2015, 9, 10).atStartOfDay(ZoneId.systemDefault()).toInstant());

    @Autowired
    private BooksRepository repository;

    @Autowired
    @Rule
    public DbUnitTester tester;

    @Test
    public void testInsert() throws Exception {
        // SetUp
        Book book = Book.builder()
                .isbn("isbn1")
                .bookTitle("title1")
                .dateOfPublication(PUBLISH_DATE)
                .build();

        // Exercise
        Book actual = repository.save(book);

        // Verify
        assertThat(actual, is(notNullValue()));
        assertThat(actual.getDateOfPublication(), is(PUBLISH_DATE));
        IDataSet expected = YamlDataSet.load(getClass().getResourceAsStream("/fixtures/books/books-inserted.yml"));
        tester.verifyTable("BOOKS", expected, "DATE_OF_PUBLICATION");
    }
}
