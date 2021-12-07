package edu.cnm.deepdive.controller;

import edu.cnm.deepdive.model.entity.User;
import edu.cnm.deepdive.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService service;

    @Autowired
    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping(value = "/{externalKey}", produces = MediaType.APPLICATION_JSON_VALUE)
    public User get(@PathVariable UUID externalKey) {
        return service
                .getByExternalKey(externalKey)
                .orElseThrow();
    }

    @PutMapping(value = "/me", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public User me(@RequestBody User user) {
        return service.update(user, service.getCurrentUser());
    }

}