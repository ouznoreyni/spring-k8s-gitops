package sn.noreyni.springapi.application.mapper;

import org.mapstruct.Mapper;
import sn.noreyni.springapi.application.dto.TagDto;
import sn.noreyni.springapi.domain.model.Tag;

@Mapper(componentModel = "spring")
public interface TagMapper {
    TagDto toDto(Tag tag);
    Tag toDomain(TagDto tagDto);
}
