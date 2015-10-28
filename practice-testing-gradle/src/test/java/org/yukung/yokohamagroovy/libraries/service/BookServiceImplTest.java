package org.yukung.yokohamagroovy.libraries.service;

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

    @InjectMocks
    private BookService service = new BookServiceImpl();

    @Mock
    private BooksRepository repository;

    @Test
    public void testCreate() throws Exception {
        // SetUp
        Book book = new Book("978-4-7981-3643-1",
                "Gradle徹底入門 次世代ビルドツールによる自動化基盤の構築",
                LocalDate.of(2014, 11, 4));

        when(repository.save(book)).thenReturn(Fixtures.Books.book01());

        // Exercise
        Book actual = service.create(book);

        // Verify
        assertThat(actual, is(notNullValue()));
    }

    @Test
    public void testFind() throws Exception {
        fail();
    }

    @Test
    public void testUpdate() throws Exception {
        fail();
    }

    @Test
    public void testDelete() throws Exception {
        fail();
    }
}
