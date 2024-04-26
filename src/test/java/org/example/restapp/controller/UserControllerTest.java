package org.example.restapp.controller;

import org.example.restapp.dto.UserRequest;
import org.example.restapp.exception.UserNotFoundException;
import org.example.restapp.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.*;

import org.example.restapp.service.UserService;

import java.time.LocalDate;
import java.util.List;

@ExtendWith(SpringExtension.class)
public class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userServiceMock;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(userController)
                .build();
    }

    @Test
    public void createUser_whenSuccessfulCreation_thenReturnsCreatedUser() throws Exception {
        UserRequest userRequest = new UserRequest();
        User user = new User();
        user.setId(1L);

        when(userServiceMock.createUser(any(UserRequest.class))).thenReturn(user);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"email\":\"user@example.com\"," +
                                "\"firstName\":\"John\"," +
                                "\"lastName\":\"Doe\"," +
                                "\"birthDate\":\"1990-01-01\"," +
                                "\"address\":\"123 Street\"," +
                                "\"phone\":\"1234567890\"" +
                                "}")
                )
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").value(user.getId()));

        verify(userServiceMock).createUser(any(UserRequest.class));
    }

    @Test
    public void getUser_whenUserFound_thenReturnsUser() throws Exception {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        when(userServiceMock.findById(userId)).thenReturn(user);

        mockMvc.perform(get("/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId));

        verify(userServiceMock).findById(userId);
    }

    @Test
    public void getUser_whenUserNotFound_thenThrowsException() throws Exception {
        Long userId = 1L;

        when(userServiceMock.findById(userId)).thenThrow(new UserNotFoundException("User with id " + userId + " not found"));

        mockMvc.perform(get("/users/{id}", userId))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertInstanceOf(UserNotFoundException.class, result.getResolvedException()))
                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.message").value("User with id " + userId + " not found"));

        verify(userServiceMock).findById(userId);
    }

    @Test
    public void updateUser_whenSuccessful_thenReturnsUpdatedUser() throws Exception {
        Long userId = 1L;
        UserRequest updateRequest = new UserRequest();
        User updatedUser = new User();

        when(userServiceMock.updateUser(eq(userId), any(UserRequest.class))).thenReturn(updatedUser);

        mockMvc.perform(put("/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"email\":\"update@example.com\"," +
                                "\"firstName\":\"UpdatedFirstName\"," +
                                "\"lastName\":\"UpdatedLastName\"," +
                                "\"birthDate\":\"1992-01-01\"," +
                                "\"address\":\"UpdatedAddress\"," +
                                "\"phone\":\"UpdatedPhone\"" +
                                "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(updatedUser.getId()));

        verify(userServiceMock).updateUser(eq(userId), any(UserRequest.class));
    }

    @Test
    public void updateUser_whenUserNotFound_thenThrowsException() throws Exception {
        Long userId = 1L;
        UserRequest updateRequest = new UserRequest();

        when(userServiceMock.updateUser(eq(userId), any(UserRequest.class)))
                .thenThrow(new UserNotFoundException("User with id " + userId + " not found"));

        mockMvc.perform(put("/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"email\":\"update@example.com\"," +
                                "\"firstName\":\"UpdatedFirstName\"," +
                                "\"lastName\":\"UpdatedLastName\"," +
                                "\"birthDate\":\"1992-01-01\"," +
                                "\"address\":\"UpdatedAddress\"," +
                                "\"phone\":\"UpdatedPhone\"" +
                                "}"))
                .andExpect(status().isNotFound());

        verify(userServiceMock).updateUser(eq(userId), any(UserRequest.class));
    }

    @Test
    public void partialUpdateUser_whenSuccessful_thenReturnsUpdatedUser() throws Exception {
        Long userId = 1L;
        UserRequest partialUpdateRequest = new UserRequest();
        User partiallyUpdatedUser = new User();

        when(userServiceMock.partialUpdateUser(eq(userId), any(UserRequest.class))).thenReturn(partiallyUpdatedUser);

        mockMvc.perform(patch("/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"email\":\"update@example.com\"" +
                                "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(partiallyUpdatedUser.getId()));

        verify(userServiceMock).partialUpdateUser(eq(userId), any(UserRequest.class));
    }

    @Test
    public void deleteUser_whenSuccessful_thenReturnsNoContent() throws Exception {
        Long userId = 1L;
        doNothing().when(userServiceMock).deleteUserById(userId);

        mockMvc.perform(delete("/users/{id}", userId))
                .andExpect(status().isNoContent());

        verify(userServiceMock).deleteUserById(userId);
    }

    @Test
    public void deleteUser_whenUserNotFound_thenThrowsException() throws Exception {
        Long userId = 1L;
        doThrow(new UserNotFoundException("User with id " + userId + " not found")).when(userServiceMock).deleteUserById(userId);

        mockMvc.perform(delete("/users/{id}", userId))
                .andExpect(status().isNotFound());

        verify(userServiceMock).deleteUserById(userId);
    }

    @Test
    public void getAllUsers_whenCalled_thenReturnsUserList() throws Exception {
        int page = 0;
        int size = 10;
        LocalDate from = LocalDate.now().minusYears(1);
        LocalDate to = LocalDate.now();

        List<User> users = List.of(new User(), new User());

        when(userServiceMock.findAll(any(PageRequest.class), eq(from), eq(to))).thenReturn(users);

        mockMvc.perform(get("/users")
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size))
                        .param("from", from.toString())
                        .param("to", to.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(users.size())));

        verify(userServiceMock).findAll(any(PageRequest.class), eq(from), eq(to));
    }
}