package org.yukung.yokohamagroovy.libraries.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.yukung.yokohamagroovy.libraries.entity.Author;
import org.yukung.yokohamagroovy.libraries.service.author.AuthorService;

import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author yukung
 */
@RunWith(MockitoJUnitRunner.class)
public class AuthorRestControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private AuthorRestController authorRestController;
    @Mock
    private AuthorService authorService;

    @Before
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(authorRestController).build();
    }

    @Test
    public void testPostAuthors() throws Exception {
        Author author = Author.builder()
                .authorFirstname("John")
                .authorSurname("Doe").build();
        when(authorService.create(author)).thenReturn(new Author(10L, "John", "Doe"));
        ObjectMapper mapper = new ObjectMapper();

        mockMvc.perform(post("/api/authors")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(author)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(content().json(mapper.writeValueAsString(new Author(10L, "John", "Doe"))));
    }
}
