package ua.rud.teammanagementsystem.Controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import ua.rud.teammanagementsystem.Responses.UserResponse;
import ua.rud.teammanagementsystem.Services.UserService;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService service;
    @GetMapping
public Page<UserResponse> getAllUsers(Pageable pageable){
        return service.getAllUsers(pageable);
    }
    @GetMapping("/{id}")
    public UserResponse getUserById(@PathVariable Long id){
        return service.getUserById(id);
    }
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id){
        service.deleteUser(id);
    }

}
