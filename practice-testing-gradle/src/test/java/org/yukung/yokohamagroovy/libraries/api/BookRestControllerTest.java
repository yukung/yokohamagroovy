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
}
