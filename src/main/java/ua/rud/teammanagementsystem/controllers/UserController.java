package ua.rud.teammanagementsystem.controllers;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import ua.rud.teammanagementsystem.responses.UserResponse;
import ua.rud.teammanagementsystem.services.UserService;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
@SecurityRequirement(name = "bearerAuth")
public class UserController {
    private final UserService service;
    private final Logger log = LoggerFactory.getLogger(UserController.class);
    @GetMapping
public Page<UserResponse> getAllUsers(Pageable pageable){
        log.info("Called get all users");
        return service.getAllUsers(pageable);
    }
    @GetMapping("/{id}")
    public UserResponse getUserById(@PathVariable Long id){
        log.info("Called get user with id {}", id);
        return service.getUserById(id);
    }
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id){
        log.info("Called delete user with id {}", id);
        service.deleteUser(id);
    }

}
