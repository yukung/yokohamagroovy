package org.yukung.yokohamagroovy.libraries.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.yukung.yokohamagroovy.libraries.entity.Category;
import org.yukung.yokohamagroovy.libraries.service.category.CategoryService;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author yukung
 */
@RunWith(MockitoJUnitRunner.class)
public class CategoryRestControllerTest {

    public static final long CATEGORY_ID = 10L;

    private MockMvc mockMvc;

    @InjectMocks
    private CategoryRestController categoryRestController;

    @Mock
    private CategoryService categoryService;

    private ObjectMapper mapper = new ObjectMapper();

    @Before
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(categoryRestController).build();
    }

    @Test
    public void testPostCategories() throws Exception {
        // SetUp
        Category category = Category.builder().categoryName("test category1").build();
        Category added = Category.builder().categoryId(CATEGORY_ID).categoryName("test category1").build();
        when(categoryService.create(category)).thenReturn(added);

        // Exercise&Verify
        mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(category)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(mapper.writeValueAsString(added)));
        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        verify(categoryService, times(1)).create(captor.capture());
        verifyNoMoreInteractions(categoryService);

        Category argument = captor.getValue();
        assertThat(argument, is(notNullValue()));
        assertThat(argument.getCategoryName(), is("test category1"));
    }

    @Test
    public void testGetCategories() throws Exception {
        // SetUp
        Category category = Category.builder().categoryId(CATEGORY_ID).categoryName("test category1").build();
        when(categoryService.find(CATEGORY_ID)).thenReturn(category);

        // Exercise&Verify
        mockMvc.perform(get("/api/categories/" + CATEGORY_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(mapper.writeValueAsString(category)));
        ArgumentCaptor<Long> captor = ArgumentCaptor.forClass(Long.class);
        verify(categoryService, times(1)).find(captor.capture());
        verifyNoMoreInteractions(categoryService);

        Long argument = captor.getValue();
        assertThat(argument, is(notNullValue()));
        assertThat(argument, is(CATEGORY_ID));
    }
}
