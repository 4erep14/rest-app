package org.example.restapp.service.impl;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.example.restapp.dto.UserRequest;
import org.example.restapp.exception.UserNotFoundException;
import org.example.restapp.model.User;
import org.example.restapp.repository.UserRepository;
import org.example.restapp.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Value("${user.min-age}")
    private Integer MIN_AGE;

    @Override
    public User createUser(UserRequest request) {
        if(request.getBirthDate().isAfter(LocalDate.now().minusYears(MIN_AGE))) {
            throw new ValidationException("User should be at least " + MIN_AGE + " years old");
        }

        User user = User.builder()
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .birthDate(request.getBirthDate())
                .address(request.getAddress())
                .phone(request.getPhone())
                .build();

        return userRepository.save(user);
    }

    @Override
    public User findById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User with id " + id + " not found"));
    }

    @Override
    public User updateUser(Long id, UserRequest request) {
        User userToUpdate = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with id " + id + " not found"));

        userToUpdate.setEmail(request.getEmail());
        userToUpdate.setFirstName(request.getFirstName());
        userToUpdate.setLastName(request.getLastName());
        userToUpdate.setBirthDate(request.getBirthDate());
        userToUpdate.setAddress(request.getAddress());
        userToUpdate.setPhone(request.getPhone());

        return userRepository.save(userToUpdate);
    }

    @Override
    public User partialUpdateUser(Long id, UserRequest request) {
        User userToUpdate = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with id " + id + " not found"));

        if (request.getEmail() != null) userToUpdate.setEmail(request.getEmail());
        if (request.getFirstName() != null) userToUpdate.setFirstName(request.getFirstName());
        if (request.getLastName() != null) userToUpdate.setLastName(request.getLastName());
        if (request.getBirthDate() != null) userToUpdate.setBirthDate(request.getBirthDate());
        if (request.getAddress() != null) userToUpdate.setAddress(request.getAddress());
        if (request.getPhone() != null) userToUpdate.setPhone(request.getPhone());

        return userRepository.save(userToUpdate);
    }

    @Override
    public void deleteUserById(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User with id " + id + " not found");
        }
        userRepository.deleteById(id);
    }

    @Override
    public List<User> findAll(Pageable pageable, LocalDate from, LocalDate to) {
        if(Objects.isNull(from) || Objects.isNull(to)) {
            return userRepository.findAll(pageable).getContent();
        }

        if (from.isAfter(to) || from.isAfter(LocalDate.now()) || to.isBefore(LocalDate.now())) {
            throw new ValidationException("Invalid date range");
        }
        return userRepository.findAllByBirthDateBetween(pageable, from, to).getContent();
    }
}
