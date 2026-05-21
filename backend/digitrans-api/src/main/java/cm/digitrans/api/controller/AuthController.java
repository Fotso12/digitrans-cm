package cm.digitrans.api.controller;

import cm.digitrans.api.dto.AuthResponse;
import cm.digitrans.api.dto.LoginRequest;
import cm.digitrans.api.entity.Utilisateur;
import cm.digitrans.api.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    public ResponseEntity<Utilisateur> register(@Valid @RequestBody Utilisateur user) {
        return ResponseEntity.ok(authService.register(user));
    }
}
