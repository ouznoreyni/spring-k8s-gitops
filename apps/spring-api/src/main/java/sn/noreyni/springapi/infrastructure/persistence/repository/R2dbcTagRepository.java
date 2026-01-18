package sn.noreyni.springapi.infrastructure.persistence.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import sn.noreyni.springapi.infrastructure.persistence.entity.TagEntity;

public interface R2dbcTagRepository extends R2dbcRepository<TagEntity, Long> {
}
