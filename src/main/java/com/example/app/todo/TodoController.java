package com.example.app.todo;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/todos")
public class TodoController {
    private final TodoRepository repo;

    public TodoController(TodoRepository repo) { this.repo = repo; }

    @GetMapping
    public List<Todo> all() { return repo.findAll(); }

    @PostMapping
    public Todo create(@Valid @RequestBody Todo todo) { return repo.save(todo); }

    @PutMapping("/{id}")
    public ResponseEntity<Todo> update(@PathVariable Long id, @Valid @RequestBody Todo in) {
        return repo.findById(id).map(t -> {
            t.setTitle(in.getTitle());
            t.setDone(in.isDone());
            return ResponseEntity.ok(repo.save(t));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
