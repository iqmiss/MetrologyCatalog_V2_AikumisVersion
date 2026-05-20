package com.catalog.repository;

import com.catalog.models.DocumentComment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DocumentCommentRepository extends JpaRepository<DocumentComment, Integer> {
    List<DocumentComment> findByOrderIdOrderByCreatedAt(int orderId);
    List<DocumentComment> findByOrderIdAndResolved(int orderId, boolean resolved);
    int countByOrderIdAndResolved(int orderId, boolean resolved);
}