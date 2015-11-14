package org.yukung.yokohamagroovy.libraries.repository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import org.yukung.yokohamagroovy.libraries.LibrariesApplication;
import org.yukung.yokohamagroovy.libraries.entity.User;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * @author yukung
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(LibrariesApplication.class)
@Transactional
@Rollback
public class UsersRepositoryTest {

    @Autowired
    private UsersRepository repository;

    @Test
    public void testInsert() throws Exception {
        User user = User.builder()
            .userName("山田太郎")
            .userAddress("東京都渋谷区")
            .phoneNumber("090-1111-1111")
            .emailAddress("yamada_taro@example.com")
            .otherUserDetails("登録対象")
            .build();
        User actual = repository.save(user);
        assertThat(actual, is(notNullValue()));
        assertThat(actual.getUserId(), is(notNullValue()));
        assertThat(actual.getUserName(), is("山田太郎"));
        assertThat(actual.getUserAddress(), is("東京都渋谷区"));
        assertThat(actual.getPhoneNumber(), is("090-1111-1111"));
        assertThat(actual.getEmailAddress(), is("yamada_taro@example.com"));
        assertThat(actual.getOtherUserDetails(), is("登録対象"));
    }

    @Test
    public void testRead() throws Exception {
        User user = User.builder()
                .userName("山田太郎")
                .userAddress("東京都渋谷区")
                .phoneNumber("090-1111-1111")
                .emailAddress("yamada_taro@example.com")
                .otherUserDetails("登録対象")
                .build();
        User saved = repository.save(user);
        User actual = repository.findOne(saved.getUserId());
        assertThat(actual, is(notNullValue()));
        assertThat(actual.getUserId(), is(saved.getUserId()));
        assertThat(actual.getUserName(), is("山田太郎"));
        assertThat(actual.getUserAddress(), is("東京都渋谷区"));
        assertThat(actual.getPhoneNumber(), is("090-1111-1111"));
        assertThat(actual.getEmailAddress(), is("yamada_taro@example.com"));
        assertThat(actual.getOtherUserDetails(), is("登録対象"));
    }
}
