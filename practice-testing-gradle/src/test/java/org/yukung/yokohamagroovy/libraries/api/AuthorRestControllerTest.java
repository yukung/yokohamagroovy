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
import org.yukung.yokohamagroovy.libraries.entity.Author;
import org.yukung.yokohamagroovy.libraries.service.author.AuthorService;

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
public class AuthorRestControllerTest {

    public static final long AUTHOR_ID = 10L;

    private MockMvc mockMvc;

    @InjectMocks
    private AuthorRestController authorRestController;
    @Mock
    private AuthorService authorService;

    private ObjectMapper mapper = new ObjectMapper();

    @Before
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(authorRestController).build();
    }

    @Test
    public void testPostAuthors() throws Exception {
        // SetUp
        Author author = Author.builder()
                .authorFirstname("John")
                .authorSurname("Doe")
                .build();
        Author added = Author.builder()
                .authorId(10L)
                .authorFirstname("John")
                .authorSurname("Doe")
                .build();
        when(authorService.create(author)).thenReturn(added);

        // Exercise&Verify
        mockMvc.perform(post("/api/authors")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(author)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(content().json(mapper.writeValueAsString(added)));
        ArgumentCaptor<Author> captor = ArgumentCaptor.forClass(Author.class);
        verify(authorService, times(1)).create(captor.capture());
        verifyNoMoreInteractions(authorService);

       Author argument = captor.getValue();
        assertThat(argument, is(notNullValue()));
        assertThat(argument.getAuthorFirstname(), is("John"));
        assertThat(argument.getAuthorSurname(), is("Doe"));
    }

    @Test
    public void testGetAuthors() throws Exception {
        // SetUp
        Author author = Author.builder()
                .authorId(AUTHOR_ID)
                .authorFirstname("John")
                .authorSurname("Doe")
                .build();
        when(authorService.find(AUTHOR_ID)).thenReturn(author);

        // Exercise&Verify
        mockMvc.perform(get("/api/authors/" + AUTHOR_ID)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(content().json(mapper.writeValueAsString(new Author(10L, "John", "Doe"))));
        ArgumentCaptor<Long> captor = ArgumentCaptor.forClass(Long.class);
        verify(authorService, times(1)).find(captor.capture());
        verifyNoMoreInteractions(authorService);

        Long argument = captor.getValue();
        assertThat(argument, is(notNullValue()));
        assertThat(argument, is(AUTHOR_ID));
    }

    @Test
    public void testPutAuthors() throws Exception {
        // SetUp
        Author author = Author.builder()
                .authorId(AUTHOR_ID)
                .authorFirstname("名無しの")
                .authorSurname("権兵衛")
                .build();
        doNothing().when(authorService).update(author);

        // Exercise&Verify
        mockMvc.perform(put("/api/authors/" + AUTHOR_ID)
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(author)))
                .andExpect(status().isCreated());
        ArgumentCaptor<Author> captor = ArgumentCaptor.forClass(Author.class);
        verify(authorService, times(1)).update(captor.capture());
        verifyNoMoreInteractions(authorService);

        Author argument = captor.getValue();
        assertThat(argument, is(notNullValue()));
        assertThat(argument.getAuthorId(), is(AUTHOR_ID));
        assertThat(argument.getAuthorFirstname(), is("名無しの"));
        assertThat(argument.getAuthorSurname(), is("権兵衛"));
    }
}
