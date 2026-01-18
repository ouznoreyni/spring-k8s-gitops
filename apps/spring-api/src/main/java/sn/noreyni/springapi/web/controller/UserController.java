package sn.noreyni.springapi.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import sn.noreyni.springapi.application.dto.UserDto;
import sn.noreyni.springapi.application.facade.UserFacade;
import reactor.core.publisher.Mono;
import sn.noreyni.springapi.domain.model.User;
import sn.noreyni.springapi.web.request.CreateUserRequest;
import sn.noreyni.springapi.web.request.UserUpdateRequest;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management APIs")
public class UserController {

    private final UserFacade userFacade;

    @PostMapping
    @Operation(summary = "Create a new user")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<UserDto> createUser(@Valid @RequestBody CreateUserRequest request) {
        User user = User.builder()
                .username(request.getUsername())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(request.getPassword())
                .role(request.getRole())
                .build();
        return userFacade.createUser(user);
    }

    @GetMapping
    @Operation(summary = "Get paginated list of users")
    public Mono<Page<UserDto>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return userFacade.getUsers(PageRequest.of(page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    public Mono<UserDto> getUser(@PathVariable Long id) {
        return userFacade.getUserById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user")
    public Mono<UserDto> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        User user = User.builder()
                .username(request.getUsername())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .build();
        return userFacade.updateUser(id, user);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteUser(@PathVariable Long id) {
        return userFacade.deleteUser(id);
    }
}
