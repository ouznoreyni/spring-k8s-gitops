package sn.noreyni.springapi.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import sn.noreyni.springapi.application.facade.AuthFacade;
import sn.noreyni.springapi.domain.model.User;
import sn.noreyni.springapi.web.request.LoginRequest;
import sn.noreyni.springapi.web.request.RegisterRequest;
import sn.noreyni.springapi.web.response.AuthResponse;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication APIs")
@Slf4j
public class AuthController {

    private final AuthFacade authFacade;

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.debug("Registering user: {}", request.getEmail());
        User user = User.builder()
                .username(request.getUsername())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(request.getPassword())
                .build();

        return authFacade.register(user);
    }

    @PostMapping("/login")
    @Operation(summary = "Login and get token")
    public Mono<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.debug("Login attempt for user: {}", request.getEmail());
        return authFacade.login(request);
    }
}
