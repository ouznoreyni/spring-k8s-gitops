package sn.noreyni.springapi.application.mapper;

import org.mapstruct.Mapper;
import sn.noreyni.springapi.application.dto.ArticleDto;
import sn.noreyni.springapi.application.dto.ArticleSummaryDto;
import sn.noreyni.springapi.domain.model.Article;

@Mapper(componentModel = "spring", uses = {TagMapper.class})
public interface ArticleMapper {
    ArticleDto toDto(Article article);
    ArticleSummaryDto toSummaryDto(Article article);
    Article toDomain(ArticleDto articleDto);
}
