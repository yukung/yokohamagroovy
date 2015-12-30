package org.yukung.yokohamagroovy.libraries.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.yukung.yokohamagroovy.libraries.entity.Book;
import org.yukung.yokohamagroovy.libraries.service.book.BookService;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author yukung
 */
@RunWith(MockitoJUnitRunner.class)
public class BookRestControllerTest {

    public static final String ISBN = "123456789";
    private MockMvc mockMvc;

    @InjectMocks
    private BookRestController bookRestController;
    @Mock
    private BookService bookService;

    private ObjectMapper mapper = new ObjectMapper();

    @Before
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(bookRestController).build();
    }

    @Test
    public void testPostBooks() throws Exception {
        // SetUp
        Book book = new Book("123456789", "test", LocalDate.of(2000, 1, 1));
        when(bookService.create(book)).thenReturn(book);

        // Exercise&Verify
        mockMvc.perform(post("/api/books")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(book)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(content().json(mapper.writeValueAsString(book)));
        ArgumentCaptor<Book> captor = ArgumentCaptor.forClass(Book.class);
        verify(bookService, times(1)).create(captor.capture());
        verifyNoMoreInteractions(bookService);

        Book argument = captor.getValue();
        assertThat(argument, is(notNullValue()));
        assertThat(argument.getIsbn(), is("123456789"));
        assertThat(argument.getBookTitle(), is("test"));
        assertThat(argument.getDateOfPublication(), is(LocalDate.of(2000, 1, 1)));
    }

    @Test
    public void testGetBooks() throws Exception {
        // SetUp
        Book book = new Book(ISBN, "test", LocalDate.of(2000, 1, 1));
        when(bookService.create(book)).thenReturn(book);
        when(bookService.find(ISBN)).thenReturn(book);

        // Exercise&Verify
        mockMvc.perform(get("/api/books/" + ISBN)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(content().json(mapper.writeValueAsString(book)));
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(bookService, times(1)).find(captor.capture());
        verifyNoMoreInteractions(bookService);

        String argument = captor.getValue();
        assertThat(argument, is(notNullValue()));
        assertThat(argument, is(ISBN));
    }

    @Test
    public void testPutBooks() throws Exception {
        // SetUp
        Book book = new Book(ISBN, "更新後", LocalDate.of(2015, 6, 30));
        when(bookService.create(book)).thenReturn(book);
        when(bookService.find(ISBN)).thenReturn(book);
        doNothing().when(bookService).update(book);

        // Exercise&Verify
        mockMvc.perform(put("/api/books/" + ISBN)
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(book)))
                .andExpect(status().isCreated());
        ArgumentCaptor<Book> captor = ArgumentCaptor.forClass(Book.class);
        verify(bookService, times(1)).update(captor.capture());
        verifyNoMoreInteractions(bookService);

        Book argument = captor.getValue();
        assertThat(argument, is(notNullValue()));
        assertThat(argument.getIsbn(), is(ISBN));
        assertThat(argument.getBookTitle(), is("更新後"));
        assertThat(argument.getDateOfPublication(), is(LocalDate.of(2015, 6, 30)));
    }
}
