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
import org.yukung.yokohamagroovy.libraries.verifier.Fixtures.Books;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.yukung.yokohamagroovy.libraries.verifier.BookVerifier.*;

/**
 * @author yukung
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(LibrariesApplication.class)
@Fixture(resources = "/fixtures/books/books.yml")
public class BooksRepositoryTest {

    private static final LocalDate PUBLISH_DATE = LocalDate.of(2015, 9, 10);
    public static final String ISBN = "978-4-7981-3643-1";

    @Autowired
    private BooksRepository repository;

    @Autowired
    @Rule
    public DbUnitTester tester;

    @Test
    public void testInsert() throws Exception {
        // SetUp
        Book book = new Book("isbn1", "title1", PUBLISH_DATE);

        // Exercise
        Book actual = repository.save(book);

        // Verify
        assertThat(actual, is(notNullValue()));
        assertThat(actual.getDateOfPublication(), is(PUBLISH_DATE));
        IDataSet expected = YamlDataSet.load(getClass().getResourceAsStream("/fixtures/books/books-inserted.yml"));
        tester.verifyTable("BOOKS", expected, "DATE_OF_PUBLICATION");
    }

    @Test
    public void testRead() throws Exception {
        // Exercise
        Book book = repository.findOne(ISBN);

        // Verify
        assertThat(book, is(notNullValue()));
        assertThat(book.getIsbn(), is(Books.book01().getIsbn()));
        assertThat(book.getBookTitle(), is(Books.book01().getBookTitle()));
        assertThat(book.getDateOfPublication(), is(Books.book01().getDateOfPublication()));
        verify(book, Books.book01());
    }

    @Test
    public void testUpdate() throws Exception {
        // SetUp
        Book before = repository.findOne(ISBN);
        String expectedTitle = "title2";
        LocalDate expectedPublicationDate = PUBLISH_DATE;
        before.setBookTitle(expectedTitle);
        before.setDateOfPublication(expectedPublicationDate);

        // Exercise
        repository.save(before);
        Book book = repository.findOne(ISBN);

        // Verify
        assertThat(book, is(notNullValue()));
        assertThat(book.getIsbn(), is(ISBN));
        assertThat(book.getBookTitle(), is(expectedTitle));
        assertThat(book.getDateOfPublication(), is(expectedPublicationDate));
        IDataSet expected = YamlDataSet.load(getClass().getResourceAsStream("/fixtures/books/books-updated.yml"));
        tester.verifyTable("BOOKS", expected, "DATE_OF_PUBLICATION");
    }

    @Test
    public void testDelete() throws Exception {
        // SetUp
        Book before = repository.findOne(ISBN);

        // Exercise
        repository.delete(ISBN);

        // Verify
        assertThat(repository.findOne(ISBN), is(nullValue()));
        IDataSet expected = YamlDataSet.load(getClass().getResourceAsStream("/fixtures/books/books-deleted.yml"));
        tester.verifyTable("BOOKS", expected);
    }
}
