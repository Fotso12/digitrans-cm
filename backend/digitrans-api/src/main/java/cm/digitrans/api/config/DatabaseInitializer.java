package cm.digitrans.api.config;

import cm.digitrans.api.entity.Utilisateur;
import cm.digitrans.api.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseInitializer implements CommandLineRunner {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (utilisateurRepository.count() == 0) {
            log.info("Base de données vide. Initialisation des utilisateurs par défaut...");

            // 1. Création de l'Administrateur
            Utilisateur admin = Utilisateur.builder()
                    .username("admin")
                    .email("admin@agrocam.com")
                    .password(passwordEncoder.encode("12345678"))
                    .role(Utilisateur.Role.ADMIN)
                    .build();
            utilisateurRepository.save(admin);

            // 2. Création de l'Agent de terrain
            Utilisateur agent = Utilisateur.builder()
                    .username("agent")
                    .email("agent@agrocam.com")
                    .password(passwordEncoder.encode("12345678"))
                    .role(Utilisateur.Role.AGENT_TERRAIN)
                    .build();
            utilisateurRepository.save(agent);

            // 3. Création du Manager
            Utilisateur manager = Utilisateur.builder()
                    .username("manager")
                    .email("manager@agrocam.com")
                    .password(passwordEncoder.encode("12345678"))
                    .role(Utilisateur.Role.MANAGER)
                    .build();
            utilisateurRepository.save(manager);

            log.info("Initialisation réussie ! Comptes créés : 'admin' (ADMIN), 'agent' (AGENT_TERRAIN), 'manager' (MANAGER) avec le mot de passe '12345678'.");
        } else {
            log.info("Des utilisateurs existent déjà en base. Initialisation ignorée.");
        }
    }
}
