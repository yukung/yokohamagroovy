package org.yukung.yokohamagroovy.libraries.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.yukung.yokohamagroovy.libraries.entity.User;
import org.yukung.yokohamagroovy.libraries.repository.UsersRepository;

/**
 * @author yukung
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UsersRepository repository;

    @Override
    public User create(User user) {
        return repository.save(user);
    }

    @Override
    public User find(Long userId) {
        return repository.findOne(userId);
    }

    @Override
    public void update(User user) {
        repository.save(user);
    }

    @Override
    public void delete(User user) {
        repository.delete(user.getUserId());
    }
}
