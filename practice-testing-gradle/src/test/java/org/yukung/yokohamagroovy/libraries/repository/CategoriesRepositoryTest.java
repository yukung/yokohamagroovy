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
import org.yukung.yokohamagroovy.libraries.entity.Category;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * @author yukung
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(LibrariesApplication.class)
@Fixture(resources = "/fixtures/categories/categories.yml")
public class CategoriesRepositoryTest {

    @Autowired
    private CategoriesRepository repository;

    @Autowired
    @Rule
    public DbUnitTester tester;

    @Test
    public void testInsert() throws Exception {
        // SetUp
        Category category = Category.builder()
                .categoryName("雑誌")
                .build();

        // Exercise
        Category actual = repository.save(category);

        // Verify
        assertThat(actual, is(notNullValue()));
        assertThat(actual.getCategoryId(), is(notNullValue()));
        IDataSet expected = YamlDataSet.load(getClass().getResourceAsStream("/fixtures/categories/categories-inserted.yml"));
        tester.verifyTable("CATEGORIES", expected, "CATEGORY_ID");
    }

    @Test
    public void testRead() throws Exception {
        // Exercise
        Category category = repository.findOne(3L);

        // Verify
        assertThat(category, is(notNullValue()));
        assertThat(category.getCategoryId(), is(notNullValue()));
        assertThat(category.getCategoryName(), is(notNullValue()));
    }

    @Test
    public void testUpdate() throws Exception {
        // SetUp
        Category before = repository.findOne(3L);
        String expectedCategoryName = "小説";
        before.setCategoryName(expectedCategoryName);

        // Exercise
        repository.save(before);

        // Verify
        Category actual = repository.findOne(before.getCategoryId());
        assertThat(actual, is(notNullValue()));
        assertThat(actual.getCategoryId(), is(before.getCategoryId()));
        assertThat(actual.getCategoryName(), is(expectedCategoryName));
        IDataSet expected = YamlDataSet.load(getClass().getResourceAsStream("/fixtures/categories/categories-updated.yml"));
        tester.verifyTable("CATEGORIES", expected);
    }

    @Test
    public void testDelete() throws Exception {
        // Setup
        Category category = repository.findOne(3L);
        Long categoryId = category.getCategoryId();

        // Exercise
        repository.delete(categoryId);

        // Verify
        assertThat(repository.findOne(categoryId), is(nullValue()));
        IDataSet expected = YamlDataSet.load(getClass().getResourceAsStream("/fixtures/categories/categories-deleted.yml"));
        tester.verifyTable("CATEGORIES", expected);
    }
}
