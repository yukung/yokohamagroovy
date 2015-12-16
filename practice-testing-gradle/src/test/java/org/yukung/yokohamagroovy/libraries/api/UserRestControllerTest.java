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
import org.yukung.yokohamagroovy.libraries.entity.User;
import org.yukung.yokohamagroovy.libraries.service.user.UserService;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author yukung
 */
@RunWith(MockitoJUnitRunner.class)
public class UserRestControllerTest {

    public static final long USER_ID = 100L;
    private MockMvc mockMvc;

    @InjectMocks
    private UserRestController userRestController;
    @Mock
    private UserService userService;

    private ObjectMapper mapper = new ObjectMapper();

    @Before
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(userRestController).build();
    }

    @Test
    public void testPostUsers() throws Exception {
        // SetUp
        User user = User.builder()
                .userName("John Doe")
                .userAddress("somewhere")
                .phoneNumber("0123456789")
                .emailAddress("john_doe@exmaple.com")
                .otherUserDetails("hogefuga")
                .build();
        User added = User.builder()
                .userId(USER_ID)
                .userName("John Doe")
                .userAddress("somewhere")
                .phoneNumber("0123456789")
                .emailAddress("john_doe@exmaple.com")
                .otherUserDetails("hogefuga")
                .build();
        when(userService.create(user)).thenReturn(added);

        // Exercise&Verify
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(mapper.writeValueAsString(added)));
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userService, times(1)).create(captor.capture());
        verifyNoMoreInteractions(userService);

        User argument = captor.getValue();
        assertThat(argument, is(notNullValue()));
        assertThat(argument.getUserId(), is(USER_ID));
        assertThat(argument.getUserName(), is("John Doe"));
        assertThat(argument.getUserAddress(), is("somewhere"));
        assertThat(argument.getPhoneNumber(), is("0123456789"));
        assertThat(argument.getEmailAddress(), is("john_doe@example.com"));
        assertThat(argument.getOtherUserDetails(), is("hogefuga"));
    }
}
