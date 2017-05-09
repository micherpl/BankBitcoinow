package services;


import models.User;

public interface UserService {
    void save(User user);

    User findByUsername(String username);
}
