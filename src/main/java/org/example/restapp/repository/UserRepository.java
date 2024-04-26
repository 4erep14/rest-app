package org.example.restapp.repository;

import org.example.restapp.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Page<User> findAllByBirthDateBetween(Pageable pageable, LocalDate from, LocalDate to);
}
