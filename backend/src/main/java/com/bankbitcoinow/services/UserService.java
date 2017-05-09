package com.bankbitcoinow.services;


import com.bankbitcoinow.models.User;

public interface UserService {
    void save(User user);

    User findByUsername(String username);
}
