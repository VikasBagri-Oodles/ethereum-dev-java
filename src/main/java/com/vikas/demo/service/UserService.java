package com.vikas.demo.service;

import com.vikas.demo.domain.User;
import com.vikas.demo.dto.UserRegistrationDTO;
import com.vikas.demo.exception.BadRequestException;
import com.vikas.demo.exception.ResourceAlreadyExists;
import com.vikas.demo.exception.ResourceNotFoundException;
import com.vikas.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;

    public Long createUser(UserRegistrationDTO userRegistrationDTO) {

        verifyUserRegistrationData(userRegistrationDTO);
        User user = new User(userRegistrationDTO);
        return userRepository.save(user).getId();

    }

    private void verifyUserRegistrationData(UserRegistrationDTO userRegistrationDTO) {

        // firstName: cannot be null or empty
        if (Objects.isNull(userRegistrationDTO.getFirstName()) || userRegistrationDTO.getFirstName().isBlank()) {
            throw new BadRequestException("First name cannot be empty");
        }

        // lastName: cannot be null or empty
        if (Objects.isNull(userRegistrationDTO.getLastName()) || userRegistrationDTO.getLastName().isBlank()) {
            throw new BadRequestException("Last name cannot be empty");
        }

        // email: cannot be null or empty and has to be unique
        if (Objects.isNull(userRegistrationDTO.getEmail()) || userRegistrationDTO.getEmail().isBlank()) {
            throw new BadRequestException("Email cannot be empty");
        }
        Optional<User> user = userRepository.findByEmail(userRegistrationDTO.getEmail());
        if (user.isPresent()) {
            throw new ResourceAlreadyExists("Email already taken");
        }

    }

    public User findUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User with id %s does not exist".formatted(id)));
    }

}
