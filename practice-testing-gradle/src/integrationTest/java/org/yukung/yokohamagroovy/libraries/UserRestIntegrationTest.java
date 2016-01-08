package org.yukung.yokohamagroovy.libraries;

import jp.classmethod.testing.database.DbUnitTester;
import jp.classmethod.testing.database.Fixture;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;
import org.yukung.yokohamagroovy.libraries.entity.User;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * @author yukung
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(LibrariesApplication.class)
@WebIntegrationTest(randomPort = true)
@Fixture(resources = "/fixtures/users/users.yml")
public class UserRestIntegrationTest {

    public static final Long USER_ID = 2L;

    @Value("${local.server.port}")
    private int port;

    private RestTemplate restTemplate = new TestRestTemplate();

    @Autowired
    @Rule
    public DbUnitTester tester;

    @Test
    public void testPostUser() throws Exception {
        // SetUp
        User user = User.builder()
                .userName("山田太郎")
                .userAddress("東京都渋谷区")
                .phoneNumber("090-1111-1111")
                .emailAddress("yamada_taro@example.com")
                .otherUserDetails("登録対象")
                .build();

        // Exercise
        ResponseEntity<User> responseEntity =
                restTemplate.postForEntity(baseUrl(), user, User.class);

        // Verify
        assertThat(responseEntity, is(notNullValue()));
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.CREATED));
        assertThat(responseEntity.getHeaders().getContentType(), is(MediaType.APPLICATION_JSON_UTF8));
        User actual = responseEntity.getBody();
        assertThat(actual, is(notNullValue()));
        assertThat(actual.getUserId(), is(greaterThan(0L)));
        assertThat(actual.getUserName(), is("山田太郎"));
        assertThat(actual.getUserAddress(), is("東京都渋谷区"));
        assertThat(actual.getPhoneNumber(), is("090-1111-1111"));
        assertThat(actual.getEmailAddress(), is("yamada_taro@example.com"));
        assertThat(actual.getOtherUserDetails(), is("登録対象"));
    }

    @Test
    public void testGetUser() throws Exception {
        // Exercise
        ResponseEntity<User> responseEntity = restTemplate.getForEntity(baseUrl() + USER_ID, User.class);

        assertThat(responseEntity, is(notNullValue()));
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
        assertThat(responseEntity.getHeaders().getContentType(), is(MediaType.APPLICATION_JSON_UTF8));
        User actual = responseEntity.getBody();
        assertThat(actual, is(notNullValue()));
        assertThat(actual.getUserId(), is(USER_ID));
        assertThat(actual.getUserName(), is("鈴木一郎"));
        assertThat(actual.getUserAddress(), is("神奈川県横浜市"));
        assertThat(actual.getPhoneNumber(), is("090-2222-2222"));
        assertThat(actual.getEmailAddress(), is("suzuki_ichiro@example.com"));
        assertThat(actual.getOtherUserDetails(), is("テストユーザー２"));
    }

    private String baseUrl() {
        return "http://localhost:" + port + "/api/users/";
    }
}
