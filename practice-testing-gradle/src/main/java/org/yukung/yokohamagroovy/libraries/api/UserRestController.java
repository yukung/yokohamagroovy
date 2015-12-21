package org.yukung.yokohamagroovy.libraries.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
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

    @RequestMapping(method = RequestMethod.GET, path = "{id}")
    public User getUsers(@PathVariable("id") Long userId) {
        return userService.find(userId);
    }

    @RequestMapping(method = RequestMethod.PUT, path = "{id}")
    @ResponseStatus(HttpStatus.CREATED)
    public void putUsers(@PathVariable("id") Long userId, @RequestBody User user) {
        userService.update(user);
    }
}
