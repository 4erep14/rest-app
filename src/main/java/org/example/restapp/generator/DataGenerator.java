package org.example.restapp.generator;

import com.github.javafaker.Faker;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.example.restapp.model.User;
import org.example.restapp.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

@Component
@RequiredArgsConstructor
public class DataGenerator {

    private final UserRepository userRepository;

    @PostConstruct
    public void init() {
        generateUsers();
    }

    private void generateUsers() {
        Faker faker = new Faker();
        for (int i = 0; i < 30; i++) {
            User user = new User();
            user.setFirstName(faker.name().firstName());
            user.setLastName(faker.name().lastName());
            user.setEmail(faker.internet().emailAddress());
            user.setPhone(faker.phoneNumber().phoneNumber());
            user.setBirthDate(LocalDate.ofInstant(faker.date().birthday(18, 65).toInstant(), ZoneId.systemDefault()));
            userRepository.save(user);
        }
    }
}
