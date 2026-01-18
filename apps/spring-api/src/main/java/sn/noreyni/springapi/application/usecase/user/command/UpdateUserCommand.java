package sn.noreyni.springapi.application.usecase.user.command;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import sn.noreyni.springapi.application.dto.UserDto;
import sn.noreyni.springapi.application.mapper.UserMapper;
import sn.noreyni.springapi.domain.model.User;
import sn.noreyni.springapi.domain.repository.UserRepository;
import sn.noreyni.springapi.infrastructure.exception.BlogException;

@Service
@RequiredArgsConstructor
public class UpdateUserCommand {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public Mono<UserDto> execute(Long id, User updateInfo) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new BlogException(HttpStatus.NOT_FOUND, "User not found")))
                .flatMap(existingUser -> {
                    existingUser.setUsername(updateInfo.getUsername());
                    existingUser.setFirstName(updateInfo.getFirstName());
                    existingUser.setLastName(updateInfo.getLastName());
                    existingUser.setEmail(updateInfo.getEmail());
                    return userRepository.save(existingUser);
                })
                .map(userMapper::toDto);
    }
}
