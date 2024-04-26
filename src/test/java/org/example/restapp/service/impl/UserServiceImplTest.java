package org.example.restapp.service.impl;

import jakarta.validation.ValidationException;
import org.example.restapp.exception.UserNotFoundException;
import org.example.restapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.beans.factory.annotation.Value;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.example.restapp.dto.UserRequest;
import org.example.restapp.model.User;
import org.example.restapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SpringBootTest
public class UserServiceImplTest {

    @MockBean
    private UserRepository userRepositoryMock;

    @Autowired
    private UserService userService;

    @Value("${user.min-age}")
    private int MIN_AGE;

    private UserRequest userRequest;
    private User user;
    private List<User> userList;
    private Page<User> userPage;

    @BeforeEach
    public void setUp() {
        user = User.builder()
                .id(1L)
                .email("user@example.com")
                .firstName("John")
                .lastName("Doe")
                .birthDate(LocalDate.of(1990, 1, 1))
                .address("123 Street")
                .phone("1234567890")
                .build();

        userRequest = new UserRequest(user.getEmail(), user.getFirstName(), user.getLastName(), user.getBirthDate(), user.getAddress(), user.getPhone());

        userList = new ArrayList<>();
        userList.add(user);
        userList.add(new User(5L, "user2@example.com", "Doe", "John", LocalDate.of(1990, 1, 1), user.getAddress(), user.getPhone()));
        userPage = new PageImpl<>(userList);
    }

    @Test
    void createUser_whenSuccessfulCreation_thenReturnsUser() {
        when(userRepositoryMock.save(any(User.class))).thenReturn(user);

        User actual = userService.createUser(userRequest);

        assertNotNull(actual);
        assertEquals(user, actual);
        verify(userRepositoryMock).save(any(User.class));
    }

    @Test
    void createUser_whenUnderage_thenThrowsValidationException() {
        userRequest.setBirthDate(LocalDate.now().minusYears(MIN_AGE - 1));

        assertThrows(ValidationException.class, () -> userService.createUser(userRequest));
    }

    @Test
    void findById_whenUserExists_thenReturnsFoundUser() {
        when(userRepositoryMock.findById(user.getId())).thenReturn(Optional.of(user));

        User actual = userService.findById(user.getId());
        assertEquals(user, actual);
    }

    @Test
    void findById_whenUserNotFound_thenThrowsException() {
        Long nonExistentId = 999L;
        when(userRepositoryMock.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.findById(nonExistentId));
    }

    @Test
    void updateUser_whenUserExists_thenUserIsUpdated() {
        UserRequest updateRequest = new UserRequest();
        updateRequest.setEmail("updated@example.com");
        updateRequest.setFirstName("UpdatedFirstName");
        updateRequest.setLastName("UpdatedLastName");
        updateRequest.setBirthDate(LocalDate.of(1992, 1, 1));
        updateRequest.setAddress("UpdatedAddress");
        updateRequest.setPhone("UpdatedPhone");

        User expected = User.builder()
                .id(user.getId())
                .email(updateRequest.getEmail())
                .firstName(updateRequest.getFirstName())
                .lastName(updateRequest.getLastName())
                .birthDate(updateRequest.getBirthDate())
                .address(updateRequest.getAddress())
                .phone(updateRequest.getPhone())
                .build();

        Long existingUserId = user.getId();
        when(userRepositoryMock.findById(existingUserId)).thenReturn(Optional.of(user));
        when(userRepositoryMock.save(any(User.class))).thenReturn(user);

        User actual = userService.updateUser(existingUserId, updateRequest);

        assertNotNull(actual);
        assertEquals(expected, actual);
        verify(userRepositoryMock).save(user);
    }

    @Test
    void updateUser_whenUserDoesNotExist_thenThrowsException() {
        Long nonExistingUserId = 2L;
        UserRequest updateRequest = new UserRequest();
        when(userRepositoryMock.findById(nonExistingUserId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.updateUser(nonExistingUserId, updateRequest));
    }

    @Test
    void partialUpdateUser_WhenUserExists_UserIsPartiallyUpdated() {
        UserRequest partialUpdateRequest = new UserRequest();
        partialUpdateRequest.setEmail("updated@example.com");

        Long existingUserId = user.getId();
        when(userRepositoryMock.findById(existingUserId)).thenReturn(Optional.of(user));
        when(userRepositoryMock.save(any(User.class))).thenReturn(user);

        user.setEmail(partialUpdateRequest.getEmail());
        User expected = user;
        User actual = userService.partialUpdateUser(existingUserId, partialUpdateRequest);

        assertNotNull(actual);
        assertEquals(expected, actual);
        verify(userRepositoryMock).save(user);
    }

    @Test
    void partialUpdateUser_whenUserDoesNotExist_thenThrowsException() {
        Long nonExistingUserId = 2L;
        UserRequest partialUpdateRequest = new UserRequest();
        when(userRepositoryMock.findById(nonExistingUserId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.partialUpdateUser(nonExistingUserId, partialUpdateRequest));
    }

    @Test
    void deleteUserById_whenUserExists_thenUserIsDeleted() {
        Long existingUserId = user.getId();
        when(userRepositoryMock.existsById(existingUserId)).thenReturn(true);
        doNothing().when(userRepositoryMock).deleteById(existingUserId);

        userService.deleteUserById(existingUserId);

        verify(userRepositoryMock).deleteById(existingUserId);
    }

    @Test
    void deleteUserById_whenUserDoesNotExist_thenThrowsException() {
        Long nonExistingUserId = 2L;
        when(userRepositoryMock.findById(nonExistingUserId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.deleteUserById(nonExistingUserId));
    }

    @Test
    void findAll_whenNoDateRangeProvided_thenReturnsAllUsers() {
        when(userRepositoryMock.findAll(any(Pageable.class))).thenReturn(userPage);

        Pageable pageable = PageRequest.of(0, 10);
        List<User> actual = userService.findAll(pageable, null, null);

        assertNotNull(actual);
        assertEquals(userList.size(), actual.size());
        assertEquals(userList.getFirst(), actual.getFirst());
        verify(userRepositoryMock).findAll(pageable);
    }

    @Test
    void findAll_whenDateRangeProvided_thenReturnsFilteredUsers() {
        LocalDate from = LocalDate.of(1990, 1, 1);
        LocalDate to = LocalDate.now();
        when(userRepositoryMock.findAllByBirthDateBetween(any(Pageable.class), eq(from), eq(to)))
                .thenReturn(userPage);

        Pageable pageable = PageRequest.of(0, 10);
        List<User> actual = userService.findAll(pageable, from, to);

        assertNotNull(actual);
        assertEquals(userList.size(), actual.size());
        assertEquals(userList.getFirst(), actual.getFirst());
        verify(userRepositoryMock).findAllByBirthDateBetween(pageable, from, to);
    }

}