package org.example.restapp.service;

import org.example.restapp.dto.UserRequest;
import org.example.restapp.model.User;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface UserService {
    User createUser(UserRequest request);
    User findById(Long id);
    User updateUser(Long id, UserRequest request);
    User partialUpdateUser(Long id, UserRequest request);
    void deleteUserById(Long id);
    List<User> findAll(Pageable pageable, LocalDate from, LocalDate to);

}
