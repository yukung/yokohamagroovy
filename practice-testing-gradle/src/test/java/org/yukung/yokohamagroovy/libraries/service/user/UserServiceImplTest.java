package org.yukung.yokohamagroovy.libraries.service.user;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.yukung.yokohamagroovy.libraries.entity.User;
import org.yukung.yokohamagroovy.libraries.repository.UsersRepository;
import org.yukung.yokohamagroovy.libraries.verifier.Fixtures;

import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author yukung
 */
@RunWith(MockitoJUnitRunner.class)
public class UserServiceImplTest {

    public static final Long USER_ID = 1234L;
    public static final String USER_NAME = "John Doe";
    public static final String USER_ADDRESS = "東京都渋谷区";
    public static final String PHONE_NUMBER = "090-1111-1111";
    public static final String EMAIL_ADDRESS = "john_doe@example.com";
    public static final String OTHER_USER_DETAILS = "test user1";

    @InjectMocks
    private UserService service = new UserServiceImpl();

    @Mock
    private UsersRepository repository;

    @Test
    public void testCreate() throws Exception {
        // SetUp
        User user = User.builder()
                .userName(USER_NAME)
                .userAddress(USER_ADDRESS)
                .phoneNumber(PHONE_NUMBER)
                .emailAddress(EMAIL_ADDRESS)
                .otherUserDetails(OTHER_USER_DETAILS)
                .build();
        when(repository.save(user)).thenReturn(Fixtures.Users.user01());

        // Exercise
        User actual = service.create(user);

        // Verify
        assertThat(actual, is(notNullValue()));
        assertThat(actual.getUserId(), is(any(Long.class)));
        assertThat(actual.getUserName(), is(USER_NAME));
        assertThat(actual.getUserAddress(), is(USER_ADDRESS));
        assertThat(actual.getPhoneNumber(), is(PHONE_NUMBER));
        assertThat(actual.getEmailAddress(), is(EMAIL_ADDRESS));
        assertThat(actual.getOtherUserDetails(), is(OTHER_USER_DETAILS));
        verify(repository, times(1)).save(user);
    }

    @Test
    public void testFind() throws Exception {
        // SetUp
        when(repository.findOne(USER_ID)).thenReturn(Fixtures.Users.user01());

        // Exercise
        User actual = service.find(USER_ID);

        // Verify
        assertThat(actual, is(notNullValue()));
        assertThat(actual.getUserId(), is(USER_ID));
        assertThat(actual.getUserName(), is(USER_NAME));
        assertThat(actual.getUserAddress(), is(USER_ADDRESS));
        assertThat(actual.getPhoneNumber(), is(PHONE_NUMBER));
        assertThat(actual.getEmailAddress(), is(EMAIL_ADDRESS));
        assertThat(actual.getOtherUserDetails(), is(OTHER_USER_DETAILS));
        verify(repository, times(1)).findOne(USER_ID);
    }

    @Test
    public void testUpdate() throws Exception {
        // SetUp
        User user = Fixtures.Users.user01();
        String UPDATED_USER_NAME = "名無しの権兵衛";
        String UPDATED_USER_ADDRESS = "神奈川県横浜市";
        String UPDATED_PHONE_NUMBER = "090-2222-2222";
        String UPDATED_EMAIL_ADDRESS = "nanashi@example.com";
        String UPDATED_OTHER_USER_DETAILS = "test user1 is updated";
        user.setUserName(UPDATED_USER_NAME);
        user.setUserAddress(UPDATED_USER_ADDRESS);
        user.setPhoneNumber(UPDATED_PHONE_NUMBER);
        user.setEmailAddress(UPDATED_EMAIL_ADDRESS);
        user.setOtherUserDetails(UPDATED_OTHER_USER_DETAILS);
        when(repository.save(user)).thenReturn(Fixtures.Users.user01_updated());
        when(repository.findOne(user.getUserId())).thenReturn(Fixtures.Users.user01_updated());

        // Exercise
        service.update(user);

        // Verify
        User actual = service.find(user.getUserId());
        assertThat(actual, is(notNullValue()));
        assertThat(actual.getUserId(), is(user.getUserId()));
        assertThat(actual.getUserName(), is(UPDATED_USER_NAME));
        assertThat(actual.getUserAddress(), is(UPDATED_USER_ADDRESS));
        assertThat(actual.getPhoneNumber(), is(UPDATED_PHONE_NUMBER));
        assertThat(actual.getEmailAddress(), is(UPDATED_EMAIL_ADDRESS));
        assertThat(actual.getOtherUserDetails(), is(UPDATED_OTHER_USER_DETAILS));
        verify(repository, times(1)).save(user);
    }

    @Test
    public void testDelete() throws Exception {
        // SetUp
        User user = Fixtures.Users.user01();
        doNothing().when(repository).delete(user.getUserId());
        when(repository.findOne(user.getUserId())).thenReturn(null);

        // Exercise
        service.delete(user);

        // Verify
        User actual = service.find(user.getUserId());
        assertThat(actual, is(nullValue()));
        verify(repository, times(1)).delete(user.getUserId());
    }
}
