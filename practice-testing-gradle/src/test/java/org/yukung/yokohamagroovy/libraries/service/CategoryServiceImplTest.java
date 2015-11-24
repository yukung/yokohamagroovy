package org.yukung.yokohamagroovy.libraries.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.yukung.yokohamagroovy.libraries.entity.Category;
import org.yukung.yokohamagroovy.libraries.repository.CategoriesRepository;
import org.yukung.yokohamagroovy.libraries.verifier.Fixtures;

import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author yukung
 */
@RunWith(MockitoJUnitRunner.class)
public class CategoryServiceImplTest {

    public static final String CATEGORY_NAME = "computer";

    @InjectMocks
    private CategoryService service = new CategoryServiceImpl();

    @Mock
    private CategoriesRepository repository;

    @Test
    public void testCreate() throws Exception {
        // SetUp
        Category category = Category.builder()
                .categoryName(CATEGORY_NAME)
                .build();
        when(repository.save(category)).thenReturn(Fixtures.Categories.category01());

        // Exercise
        Category actual = service.create(category);

        // Verify
        assertThat(actual, is(notNullValue()));
        assertThat(actual.getCategoryId(), is(any(Long.class)));
        assertThat(actual.getCategoryName(), is(CATEGORY_NAME));
        verify(repository, times(1)).save(category);
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
