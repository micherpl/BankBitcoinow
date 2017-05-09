package com.bankbitcoinow.services;


import com.bankbitcoinow.models.User;

public interface UserService {
    User save(User user);

    User findByEmail(String username);
}
