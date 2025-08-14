package com.example.ats.controller;

import com.example.ats.model.User;
import com.example.ats.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * ✅ Get the current user's profile
     */
    @GetMapping("/profile")
    public ResponseEntity<User> getProfile(Authentication authentication) {
        String email = authentication.getName();
        User user = userService.findByEmail(email);
        return ResponseEntity.ok(user);
    }

    /**
     * ✅ Update profile basic info (only allowed fields)
     * Since contact info is removed, you can decide if updating username/email/role is allowed.
     * For now, this method can be removed or kept minimal.
     */
    @PutMapping("/profile")
    public ResponseEntity<User> updateProfile(Authentication authentication,
                                              @RequestBody User updatedUser) {
        // Remove or restrict updating contact info fields since those are removed
        // Optionally throw unsupported exception if updateProfile is not used
        return ResponseEntity.badRequest().body(null);
    }

    // Remove the entire endpoint for file uploads
    // @PostMapping(value = "/profile/files", consumes = {"multipart/form-data"})
    // public ResponseEntity<?> updateProfileFiles(...) {...}

}



