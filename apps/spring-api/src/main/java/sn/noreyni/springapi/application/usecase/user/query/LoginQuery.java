package sn.noreyni.springapi.application.usecase.user.query;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import sn.noreyni.springapi.domain.repository.UserRepository;
import sn.noreyni.springapi.infrastructure.exception.BlogException;
import sn.noreyni.springapi.infrastructure.security.JwtService;
import sn.noreyni.springapi.infrastructure.security.ReactiveUserDetailsServiceImpl;
import sn.noreyni.springapi.web.request.LoginRequest;
import sn.noreyni.springapi.web.response.AuthResponse;

@Service
@RequiredArgsConstructor
public class LoginQuery {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final ReactiveUserDetailsServiceImpl userDetailsService;

    public Mono<AuthResponse> execute(LoginRequest request) {
        return userRepository.findByEmail(request.getEmail())
                .filter(user -> passwordEncoder.matches(request.getPassword(), user.getPassword()))
                .switchIfEmpty(Mono.error(new BlogException(HttpStatus.UNAUTHORIZED, "Invalid credentials")))
                .flatMap(user -> userDetailsService.findByUsername(user.getEmail())
                        .map(userDetails -> AuthResponse.builder()
                                .token(jwtService.generateToken(userDetails))
                                .email(user.getEmail())
                                .role(user.getRole())
                                .build()));
    }
}
