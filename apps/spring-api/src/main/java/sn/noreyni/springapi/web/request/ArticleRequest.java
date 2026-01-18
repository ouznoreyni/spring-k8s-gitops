package sn.noreyni.springapi.web.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import sn.noreyni.springapi.domain.model.ArticleStatus;

@Data
public class ArticleRequest {
    @NotBlank
    private String title;
    @NotBlank
    private String content;
    private String imageUrl;
    private ArticleStatus status;
}
