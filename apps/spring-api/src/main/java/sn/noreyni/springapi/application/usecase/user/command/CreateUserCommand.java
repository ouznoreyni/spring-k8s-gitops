package sn.noreyni.springapi.application.usecase.user.command;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import sn.noreyni.springapi.domain.model.User;
import sn.noreyni.springapi.domain.repository.UserRepository;
import sn.noreyni.springapi.infrastructure.exception.BlogException;
import sn.noreyni.springapi.infrastructure.security.JwtService;
import sn.noreyni.springapi.infrastructure.security.ReactiveUserDetailsServiceImpl;
import sn.noreyni.springapi.web.response.AuthResponse;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CreateUserCommand {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final ReactiveUserDetailsServiceImpl userDetailsService;

    public Mono<AuthResponse> execute(User user) {
        return userRepository.existsByEmail(user.getEmail())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new BlogException(HttpStatus.BAD_REQUEST, "Email already exists"));
                    }
                    user.setPassword(passwordEncoder.encode(user.getPassword()));
                    user.setRole("ROLE_USER");
                    user.setCreatedAt(LocalDateTime.now());
                    return userRepository.save(user);
                })
                .flatMap(savedUser -> userDetailsService.findByUsername(savedUser.getEmail())
                        .map(userDetails -> AuthResponse.builder()
                                .token(jwtService.generateToken(userDetails))
                                .email(savedUser.getEmail())
                                .role(savedUser.getRole())
                                .build()));
    }
}
