package com.catalog.controllers;

import com.catalog.models.DocumentComment;
import com.catalog.repository.DocumentCommentRepository;
import com.catalog.utils.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/doc-comments")
public class DocumentCommentController {

    private final DocumentCommentRepository documentCommentRepository;
    private final JwtUtil jwtUtil;

    public DocumentCommentController(DocumentCommentRepository documentCommentRepository,
                                     JwtUtil jwtUtil) {
        this.documentCommentRepository = documentCommentRepository;
        this.jwtUtil = jwtUtil;
    }

    // GET /api/doc-comments/{orderId}
    // Returns all comments for an order
    @GetMapping("/{orderId}")
    public ResponseEntity<List<DocumentComment>> getComments(@PathVariable int orderId) {
        return ResponseEntity.ok(
            documentCommentRepository.findByOrderIdOrderByCreatedAt(orderId)
        );
    }

    // POST /api/doc-comments/{orderId}
    // Reviewer adds a comment/flag on a document section
    @PostMapping("/{orderId}")
    public ResponseEntity<DocumentComment> addComment(
            @PathVariable int orderId,
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> body) {

        String token = authHeader.replace("Bearer ", "");
        int commenterId = jwtUtil.getUserId(token);
        String commenterRole = jwtUtil.getRole(token);

        DocumentComment comment = new DocumentComment();
        comment.setOrderId(orderId);
        comment.setCommenterId(commenterId);
        comment.setCommenterRole(commenterRole);
        comment.setHighlightedText(body.get("highlightedText"));
        comment.setCommentText(body.get("commentText"));
        comment.setResolved(false);
        comment.setCreatedAt(LocalDateTime.now());

        DocumentComment saved = documentCommentRepository.save(comment);
        return ResponseEntity.status(201).body(saved);
    }

    // PUT /api/doc-comments/{id}/resolve
    // Бухгалтер marks a comment as resolved
    @PutMapping("/{id}/resolve")
    public ResponseEntity<DocumentComment> resolveComment(@PathVariable int id) {
        return documentCommentRepository.findById(id)
                .map(comment -> {
                    comment.setResolved(true);
                    comment.setResolvedAt(LocalDateTime.now());
                    return ResponseEntity.ok(documentCommentRepository.save(comment));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}