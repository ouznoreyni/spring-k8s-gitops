package sn.noreyni.springapi.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sn.noreyni.springapi.application.dto.UserDto;
import sn.noreyni.springapi.domain.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto toDto(User user);

    @Mapping(target = "password", ignore = true)
    User toDomain(UserDto userDto);
}
