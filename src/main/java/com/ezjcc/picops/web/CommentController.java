package com.ezjcc.picops.web;

import com.ezjcc.picops.comment.CommentService;
import java.security.Principal;
import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CommentController {

    private final CommentService comments;
    private final CurrentUser currentUser;

    public CommentController(CommentService comments, CurrentUser currentUser) {
        this.comments = comments;
        this.currentUser = currentUser;
    }

    @PostMapping("/pictures/{id}/comments")
    public String add(@PathVariable UUID id, Principal principal, @RequestParam String body) {
        comments.add(id, currentUser.require(principal), body);
        return "redirect:/pictures/" + id + "/view";
    }

    @PostMapping("/comments/{id}/delete")
    public String delete(@PathVariable UUID id, Principal principal) {
        UUID pictureId = comments.delete(id, currentUser.require(principal));
        return "redirect:/pictures/" + pictureId + "/view";
    }
}
