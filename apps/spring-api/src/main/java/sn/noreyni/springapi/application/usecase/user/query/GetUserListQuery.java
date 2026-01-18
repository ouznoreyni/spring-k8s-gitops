package sn.noreyni.springapi.application.usecase.user.query;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import sn.noreyni.springapi.application.dto.UserDto;
import sn.noreyni.springapi.application.mapper.UserMapper;
import sn.noreyni.springapi.domain.repository.UserRepository;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class GetUserListQuery {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public Mono<Page<UserDto>> execute(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(userMapper::toDto)
                .collectList()
                .zipWith(userRepository.count())
                .map(tuple -> new PageImpl<>(tuple.getT1(), pageable, tuple.getT2()));
    }
}
