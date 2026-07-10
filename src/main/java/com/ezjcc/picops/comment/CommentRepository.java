package com.ezjcc.picops.comment;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comment, UUID> {

    @Query("select c from Comment c left join fetch c.author "
         + "where c.picture.id = :pictureId order by c.createdAt asc")
    List<Comment> findForPicture(@Param("pictureId") UUID pictureId);
}
