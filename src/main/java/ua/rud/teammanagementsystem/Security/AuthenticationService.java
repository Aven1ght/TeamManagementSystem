package ua.rud.teammanagementsystem.Security;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ua.rud.teammanagementsystem.entity.User;
import ua.rud.teammanagementsystem.Enums.Role;
import ua.rud.teammanagementsystem.Repositories.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;
    private final AuthenticationManager manager;
    private final Logger log = LoggerFactory.getLogger(AuthenticationService.class);

    public AuthenticationResponse register(AuthenticationRequest request){
        User user = User.builder()
                .username(request.getName())
                .password(encoder.encode(request.getPassword()))
                .email(request.getEmail())
                .role(Role.USER)
                .build();
        userRepository.save(user);
        String jwt = jwtService.generateToken(user);
        log.info("New user {} registered successfully", request.getName());
        return AuthenticationResponse.builder().token(jwt).build();
    }
    public AuthenticationResponse authenticate(AuthenticationRequest request){
        manager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getName(),
                        request.getPassword()
                )
        );

        var user = userRepository
                .findByUsername(request.getName())
                .orElseThrow();

        String jwt = jwtService.generateToken(user);
        log.info("User {} was successfully authenticated", request.getName());
        return AuthenticationResponse.builder()
                .token(jwt)
                .build();
    }
}