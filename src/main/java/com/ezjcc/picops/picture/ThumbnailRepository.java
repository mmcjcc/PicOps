package com.ezjcc.picops.picture;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ThumbnailRepository extends JpaRepository<Thumbnail, UUID> {

    @Query("select t.data from Thumbnail t where t.pictureId = :id")
    Optional<byte[]> findData(@Param("id") UUID id);
}
