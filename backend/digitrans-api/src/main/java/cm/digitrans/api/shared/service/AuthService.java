package cm.digitrans.api.shared.service;

import cm.digitrans.api.shared.dto.AuthResponse;
import cm.digitrans.api.shared.dto.LoginRequest;
import cm.digitrans.api.shared.entity.Utilisateur;
import cm.digitrans.api.shared.repository.UtilisateurRepository;
import cm.digitrans.api.shared.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse login(LoginRequest request) {
        Utilisateur user = utilisateurRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Identifiants incorrects");
        }

        String token = jwtService.generateToken(user.getUsername(), user.getRole().name());

        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

    public Utilisateur register(Utilisateur user) {
        if (utilisateurRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Le nom d'utilisateur est déjà utilisé");
        }
        if (utilisateurRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("L'e-mail est déjà utilisé");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return utilisateurRepository.save(user);
    }
}
