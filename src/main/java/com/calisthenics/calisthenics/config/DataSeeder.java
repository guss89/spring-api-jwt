package com.calisthenics.calisthenics.config;

import com.calisthenics.calisthenics.model.ClientProfile;
import com.calisthenics.calisthenics.model.User;
import com.calisthenics.calisthenics.model.catalogs.Role;
import com.calisthenics.calisthenics.repository.ClientRepository;
import com.calisthenics.calisthenics.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final SeedProperties properties;
    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(
            SeedProperties properties,
            UserRepository userRepository,
            ClientRepository clientRepository,
            PasswordEncoder passwordEncoder) {
        this.properties = properties;
        this.userRepository = userRepository;
        this.clientRepository = clientRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!properties.isEnabled()) {
            return;
        }

        validate("ADMIN", properties.getAdmin());
        validate("COACH", properties.getCoach());
        validate("CLIENT", properties.getClient());

        if (!userRepository.existsByRole(Role.ADMIN)) {
            createUser(properties.getAdmin(), Role.ADMIN);
        }

        if (userRepository.findByUsername(properties.getCoach().getUsername()).isEmpty()) {
            createUser(properties.getCoach(), Role.COACH);
        }

        User client = userRepository.findByUsername(properties.getClient().getUsername())
                .orElseGet(() -> createUser(properties.getClient(), Role.CLIENT));

        if (client.getRole() != Role.CLIENT) {
            log.warn("Seed: el usuario {} existe con rol {}; no se crea perfil de cliente",
                    client.getUsername(), client.getRole());
        } else if (!clientRepository.existsByUserId(client.getId())) {
            clientRepository.save(ClientProfile.builder().user(client).build());
            log.info("Seed: perfil de cliente creado para {}", client.getUsername());
        }
    }

    private User createUser(SeedProperties.Account account, Role role) {
        if (role == Role.ADMIN && userRepository.findByUsername(account.getUsername()).isPresent()) {
            throw new IllegalStateException(
                    "APP_SEED_ADMIN_USERNAME ya está en uso por otro usuario");
        }
        if (userRepository.findByEmail(account.getEmail()).isPresent()) {
            throw new IllegalStateException(
                    "APP_SEED_" + role.name() + "_EMAIL ya está en uso por otro usuario");
        }
        User user = User.builder()
                .username(account.getUsername())
                .email(account.getEmail())
                .name(account.getName())
                .lastName(blankToNull(account.getLastName()))
                .phone(blankToNull(account.getPhone()))
                .passwordHash(passwordEncoder.encode(account.getPassword()))
                .role(role)
                .active(true)
                .build();
        User saved = userRepository.save(user);
        log.info("Seed: usuario {} creado ({})", role, account.getUsername());
        return saved;
    }

    private void validate(String roleName, SeedProperties.Account account) {
        require(account.getUsername(), "APP_SEED_" + roleName + "_USERNAME");
        require(account.getEmail(), "APP_SEED_" + roleName + "_EMAIL");
        require(account.getPassword(), "APP_SEED_" + roleName + "_PASSWORD");
        require(account.getName(), "APP_SEED_" + roleName + "_NAME");
    }

    private void require(String value, String variable) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Seed habilitado (APP_SEED_ENABLED=true) pero falta la variable de entorno " + variable);
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
