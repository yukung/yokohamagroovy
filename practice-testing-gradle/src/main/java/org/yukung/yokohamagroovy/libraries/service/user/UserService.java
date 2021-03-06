package org.yukung.yokohamagroovy.libraries.service.user;

import org.yukung.yokohamagroovy.libraries.entity.User;

/**
 * @author yukung
 */
public interface UserService {
    User create(User user);

    User find(Long userId);

    void update(User user);

    void delete(Long userId);
}
