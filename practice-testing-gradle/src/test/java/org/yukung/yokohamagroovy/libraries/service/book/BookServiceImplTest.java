package org.yukung.yokohamagroovy.libraries.service.book;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.yukung.yokohamagroovy.libraries.entity.Book;
import org.yukung.yokohamagroovy.libraries.repository.BooksRepository;
import org.yukung.yokohamagroovy.libraries.verifier.Fixtures;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author yukung
 */
@RunWith(MockitoJUnitRunner.class)
public class BookServiceImplTest {

    public static final String ISBN = "978-4-7981-3643-1";
    public static final String BOOK_TITLE = "Gradle徹底入門 次世代ビルドツールによる自動化基盤の構築";
    public static final LocalDate DATE_OF_PUBLICATION = LocalDate.of(2014, 11, 4);

    @InjectMocks
    private BookService service = new BookServiceImpl();

    @Mock
    private BooksRepository repository;

    @Test
    public void testCreate() throws Exception {
        // SetUp
        Book book = new Book(ISBN, BOOK_TITLE, DATE_OF_PUBLICATION);

        when(repository.save(book)).thenReturn(Fixtures.Books.book01());

        // Exercise
        Book actual = service.create(book);

        // Verify
        assertThat(actual, is(notNullValue()));
        assertThat(actual.getIsbn(), is(ISBN));
        assertThat(actual.getBookTitle(), is(BOOK_TITLE));
        assertThat(actual.getDateOfPublication(), is(DATE_OF_PUBLICATION));
        verify(repository, times(1)).save(book);
    }

    @Test
    public void testFind() throws Exception {
        // SetUp
        when(repository.findOne(ISBN)).thenReturn(Fixtures.Books.book01());

        // Exercise
        Book actual = service.find(ISBN);

        // Verify
        assertThat(actual, is(notNullValue()));
        assertThat(actual.getIsbn(), is(ISBN));
        assertThat(actual.getBookTitle(), is(BOOK_TITLE));
        assertThat(actual.getDateOfPublication(), is(DATE_OF_PUBLICATION));
        verify(repository, times(1)).findOne(ISBN);
    }

    @Test
    public void testUpdate() throws Exception {
        // SetUp
        Book book = Fixtures.Books.book01();
        String UPDATED_BOOK_TITLE = "更新後タイトル";
        LocalDate UPDATED_DATE_OF_PUBLICATION = LocalDate.of(2015, 1, 1);
        book.setBookTitle(UPDATED_BOOK_TITLE);
        book.setDateOfPublication(UPDATED_DATE_OF_PUBLICATION);
        when(repository.save(book)).thenReturn(Fixtures.Books.book01_updated());
        when(repository.findOne(book.getIsbn())).thenReturn(Fixtures.Books.book01_updated());

        // Exercise
        service.update(book);

        // Verify
        Book actual = service.find(book.getIsbn());
        assertThat(actual, is(notNullValue()));
        assertThat(actual.getIsbn(), is(book.getIsbn()));
        assertThat(actual.getBookTitle(), is(UPDATED_BOOK_TITLE));
        assertThat(actual.getDateOfPublication(), is(UPDATED_DATE_OF_PUBLICATION));
        verify(repository, times(1)).save(book);
    }

    @Test
    public void testDelete() throws Exception {
        // SetUp
        Book book = Fixtures.Books.book01();
        doNothing().when(repository).delete(book.getIsbn());
        when(repository.findOne(book.getIsbn())).thenReturn(null);

        // Exercise
        service.delete(book);

        // Verify
        Book actual = service.find(book.getIsbn());
        assertThat(actual, is(nullValue()));
        verify(repository, times(1)).delete(book.getIsbn());
    }
}
