package ua.rud.teammanagementsystem.security;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthenticationController {
    private final Logger log = LoggerFactory.getLogger(AuthenticationController.class);
    private final AuthenticationService service;
    @PostMapping("/register")
    public AuthenticationResponse register(@RequestBody RegistrationRequest request){
        log.info("Called register new user");
       return service.register(request);
    }

    // Created to make it easier for the reviewer to test admin-level functionality.
    // I understand that this endpoint must be removed in a production environment.
    @PostMapping("/register/admin")
    public AuthenticationResponse registerAdmin(@RequestBody RegistrationRequest request){
        log.info("Called register new admin");
        return service.registerAdmin(request);
    }
    @PostMapping("/authenticate")
    public AuthenticationResponse authenticate(@RequestBody AuthenticationRequest request){
        log.info("Called authenticate user");
        return service.authenticate(request);
    }
}
