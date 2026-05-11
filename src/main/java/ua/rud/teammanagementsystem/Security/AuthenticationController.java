package ua.rud.teammanagementsystem.Security;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthenticationController {
    private final AuthenticationService service;
    @PostMapping("/register")
    public AuthenticationResponse register(@RequestBody AuthenticationRequest request){
       return service.register(request);
    }
    @PostMapping("/authenticate")
    public AuthenticationResponse authenticate(@RequestBody AuthenticationRequest request){
        return service.authenticate(request);
    }
}
