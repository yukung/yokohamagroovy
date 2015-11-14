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
import org.yukung.yokohamagroovy.libraries.entity.User;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * @author yukung
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(LibrariesApplication.class)
@Fixture(resources = "/fixtures/users/users.yml")
public class UsersRepositoryTest {

    @Autowired
    private UsersRepository repository;

    @Autowired
    @Rule
    public DbUnitTester tester;

    @Test
    public void testInsert() throws Exception {
        // SetUp
        User user = User.builder()
                .userName("山田太郎")
                .userAddress("東京都渋谷区")
                .phoneNumber("090-1111-1111")
                .emailAddress("yamada_taro@example.com")
                .otherUserDetails("登録対象")
                .build();

        // Exercise
        User actual = repository.save(user);

        // Verify
        assertThat(actual, is(notNullValue()));
        assertThat(actual.getUserId(), is(notNullValue()));
        IDataSet expected = YamlDataSet.load(getClass().getResourceAsStream("/fixtures/users/users-inserted.yml"));
        tester.verifyTable("USERS", expected, "USER_ID");
    }

    @Test
    public void testRead() throws Exception {
        // Exercise
        User actual = repository.findOne(2L);

        // Verify
        assertThat(actual, is(notNullValue()));
        assertThat(actual.getUserId(), is(2L));
        assertThat(actual.getUserName(), is("鈴木一郎"));
        assertThat(actual.getUserAddress(), is("神奈川県横浜市"));
        assertThat(actual.getPhoneNumber(), is("090-2222-2222"));
        assertThat(actual.getEmailAddress(), is("suzuki_ichiro@example.com"));
        assertThat(actual.getOtherUserDetails(), is("テストユーザー２"));
    }

    @Test
    public void testUpdate() throws Exception {
        // SetUp
        User before = repository.findOne(2L);
        String expectedOtherUserDetails = "更新しました。";
        before.setOtherUserDetails(expectedOtherUserDetails);

        // Exercise
        User actual = repository.save(before);

        // Verify
        assertThat(actual, is(notNullValue()));
        assertThat(actual.getUserId(), is(notNullValue()));
        assertThat(actual.getOtherUserDetails(), is(equalTo(expectedOtherUserDetails)));
        IDataSet expectedFixture = YamlDataSet.load(getClass().getResourceAsStream("/fixtures/users/users-updated.yml"));
        tester.verifyTable("USERS", expectedFixture);
    }

    @Test
    public void testDelete() throws Exception {
        // SetUp
        User user = repository.findOne(2L);
        Long userId = user.getUserId();

        // Exercise
        repository.delete(userId);

        // Verify
        assertThat(repository.findOne(userId), is(nullValue()));
        IDataSet expected = YamlDataSet.load(getClass().getResourceAsStream("/fixtures/users/users-deleted.yml"));
        tester.verifyTable("USERS", expected);
    }
}
