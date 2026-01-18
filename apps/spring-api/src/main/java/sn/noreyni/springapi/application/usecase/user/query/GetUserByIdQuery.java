package sn.noreyni.springapi.application.usecase.user.query;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import sn.noreyni.springapi.application.dto.UserDto;
import sn.noreyni.springapi.application.mapper.UserMapper;
import sn.noreyni.springapi.domain.repository.UserRepository;
import sn.noreyni.springapi.infrastructure.exception.BlogException;

@Service
@RequiredArgsConstructor
public class GetUserByIdQuery {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public Mono<UserDto> execute(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toDto)
                .switchIfEmpty(Mono.error(new BlogException(HttpStatus.NOT_FOUND, "User not found")));
    }
}
