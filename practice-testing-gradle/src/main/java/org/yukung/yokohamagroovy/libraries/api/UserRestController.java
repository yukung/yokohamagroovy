package org.yukung.yokohamagroovy.libraries.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.yukung.yokohamagroovy.libraries.entity.User;
import org.yukung.yokohamagroovy.libraries.service.user.UserService;

/**
 * @author yukung
 */
@RestController
@RequestMapping("/api/users")
public class UserRestController {

    @Autowired
    private UserService userService;

    @RequestMapping(method = RequestMethod.POST)
    public User postUsers(@RequestBody User user) {
        return userService.create(user);
    }
}
