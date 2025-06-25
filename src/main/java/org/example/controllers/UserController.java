package org.example.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.example.commons.APIConstants;
import org.example.commons.BaseResponse;
import org.example.entities.User;
import org.example.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Controller to handle all User-related REST endpoints.
 */
@RestController
@RequestMapping(APIConstants.USER)
public class UserController {

    private UserRepository userRepository;

    public UserController(UserRepository repository){
        this.userRepository = repository;
    }

    /**
     * Simple test endpoint to verify controller availability.
     */
    @GetMapping(APIConstants.TEST)
    public String test(HttpServletRequest request) {
        return "test";
    }

    /**
     * Create a new user using @RequestBody.
     * Automatically sets the created date.
     */
    @PostMapping(APIConstants.USER_SIGNUP)
    public ResponseEntity<BaseResponse<User>> createUser(@RequestBody User user) {
        if (userRepository.existsByMobile(user.getMobile())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    new BaseResponse<>(409, "User already exists", null)
            );
        }

        user.setCreatedDate(LocalDateTime.now());
        user.setUpdatedDate(null);
        userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                new BaseResponse<>(200, "User created successfully", user)
        );
    }

    // ────────────────────────────────────────────────────────────

    /**
     * Fetch all users from the database.
     */
    @GetMapping(APIConstants.ALL_USERS)
    public ResponseEntity<BaseResponse<List<User>>> getAllUsers() {
        List<User> users = userRepository.findAll();

        return ResponseEntity.ok(
                new BaseResponse<>(
                        200,
                        users.isEmpty() ? "No users found" : "Users fetched successfully",
                        users
                )
        );
    }

    /**
     * Delete all users from the database.
     */
    @DeleteMapping(APIConstants.ALL_USERS)
    public ResponseEntity<BaseResponse<String>> deleteAllUsers() {
        userRepository.deleteAll();
        return ResponseEntity.ok(
                new BaseResponse<>(200, "All users deleted successfully", null)
        );
    }

    /**
     * Fetch a single user by ID (passed as query param).
     */
    @GetMapping(APIConstants.GET_USER)
    public ResponseEntity<BaseResponse<User>> getUser(@RequestParam long userId) {
        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isEmpty()) {
            return ResponseEntity.ok(
                    new BaseResponse<>(404, "No user found with ID: " + userId, null)
            );
        }

        return ResponseEntity.ok(
                new BaseResponse<>(200, "User fetched successfully", userOptional.get())
        );
    }

    /**
     * Delete a user by ID.
     */
    @DeleteMapping(APIConstants.DELETE_USER)
    public ResponseEntity<BaseResponse<Void>> deleteUser(@RequestParam long userId) {
        if (!userRepository.existsById(userId)) {
            return ResponseEntity.ok(
                    new BaseResponse<>(404, "No user found with ID: " + userId, null)
            );
        }

        userRepository.deleteById(userId);
        return ResponseEntity.ok(
                new BaseResponse<>(200, "User with ID " + userId + " deleted successfully", null)
        );
    }

    /**
     * Update a user using @RequestParam.
     * Accepts all fields and updates user data.
     */
    @PostMapping(APIConstants.UPDATE_USER)
    public ResponseEntity<BaseResponse<User>> updateUser(
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String address,
            @RequestParam Long userId,
            @RequestParam int userStatus
    ) {
        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isEmpty()) {
            return ResponseEntity.ok(
                    new BaseResponse<>(404, "No user found with ID: " + userId, null)
            );
        }

        User user = userOptional.get();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPassword(password);
        user.setAddress(address);
        user.setUserStatus(userStatus);
        user.setUpdatedDate(LocalDateTime.now()); // updated time only, preserve created time
        userRepository.save(user);

        return ResponseEntity.status(HttpStatus.OK).body(
                new BaseResponse<>(201, "User updated successfully", user)
        );
    }
}
