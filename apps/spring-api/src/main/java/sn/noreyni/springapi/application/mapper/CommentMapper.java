package sn.noreyni.springapi.application.mapper;

import org.mapstruct.Mapper;
import sn.noreyni.springapi.application.dto.CommentDto;
import sn.noreyni.springapi.domain.model.Comment;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    CommentDto toDto(Comment comment);
    Comment toDomain(CommentDto commentDto);
}
