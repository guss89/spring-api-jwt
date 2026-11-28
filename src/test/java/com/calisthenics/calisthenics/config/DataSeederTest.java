package com.calisthenics.calisthenics.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.calisthenics.calisthenics.model.ClientProfile;
import com.calisthenics.calisthenics.model.User;
import com.calisthenics.calisthenics.model.catalogs.Role;
import com.calisthenics.calisthenics.repository.ClientRepository;
import com.calisthenics.calisthenics.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class DataSeederTest {

    @Mock private UserRepository userRepository;
    @Mock private ClientRepository clientRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private SeedProperties properties;
    private DataSeeder seeder;

    @BeforeEach
    void setUp() {
        properties = new SeedProperties();
        fill(properties.getAdmin(), "admin");
        fill(properties.getCoach(), "coach");
        fill(properties.getClient(), "client");
        seeder = new DataSeeder(properties, userRepository, clientRepository, passwordEncoder);
    }

    private void fill(SeedProperties.Account a, String prefix) {
        a.setUsername(prefix + "-user");
        a.setEmail(prefix + "@test.local");
        a.setPassword("plain-" + prefix);
        a.setName(prefix);
    }

    private void stubSave() {
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(10L);
            return u;
        });
    }

    @Test
    void disabledDoesNotTouchRepositories() {
        properties.setEnabled(false);

        seeder.run();

        verifyNoInteractions(userRepository, clientRepository);
    }

    @Test
    void existingAdminIsNotCreated() {
        properties.setEnabled(true);
        when(userRepository.existsByRole(Role.ADMIN)).thenReturn(true);
        when(userRepository.findByUsername("coach-user")).thenReturn(Optional.of(
                User.builder().id(2L).role(Role.COACH).build()));
        when(userRepository.findByUsername("client-user")).thenReturn(Optional.of(
                User.builder().id(1L).role(Role.CLIENT).build()));
        when(clientRepository.existsByUserId(1L)).thenReturn(true);

        seeder.run();

        verify(userRepository, never()).save(any());
    }

    @Test
    void createsAllUsersWithBcryptAndClientProfile() {
        properties.setEnabled(true);
        when(userRepository.existsByRole(Role.ADMIN)).thenReturn(false);
        when(userRepository.findByUsername(any())).thenReturn(Optional.empty());
        when(clientRepository.existsByUserId(10L)).thenReturn(false);
        stubSave();

        seeder.run();

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(3)).save(captor.capture());
        List<User> saved = captor.getAllValues();
        assertEquals(List.of(Role.ADMIN, Role.COACH, Role.CLIENT),
                saved.stream().map(User::getRole).toList());
        User admin = saved.get(0);
        assertNotEquals("plain-admin", admin.getPasswordHash());
        String[] prefixes = {"admin", "coach", "client"};
        for (int i = 0; i < 3; i++) {
            assertTrue(passwordEncoder.matches("plain-" + prefixes[i], saved.get(i).getPasswordHash()));
            assertTrue(saved.get(i).getActive());
        }

        ArgumentCaptor<ClientProfile> profile = ArgumentCaptor.forClass(ClientProfile.class);
        verify(clientRepository).save(profile.capture());
        assertEquals("client-user", profile.getValue().getUser().getUsername());
    }

    @Test
    void existingCoachAndClientAreNotDuplicated() {
        properties.setEnabled(true);
        when(userRepository.existsByRole(Role.ADMIN)).thenReturn(true);
        when(userRepository.findByUsername("coach-user")).thenReturn(Optional.of(
                User.builder().id(2L).role(Role.COACH).build()));
        when(userRepository.findByUsername("client-user")).thenReturn(Optional.of(
                User.builder().id(3L).role(Role.CLIENT).username("client-user").build()));
        when(clientRepository.existsByUserId(3L)).thenReturn(true);

        seeder.run();

        verify(userRepository, never()).save(any());
        verify(clientRepository, never()).save(any());
    }

    @Test
    void existingClientWithoutProfileGetsProfile() {
        properties.setEnabled(true);
        when(userRepository.existsByRole(Role.ADMIN)).thenReturn(true);
        when(userRepository.findByUsername("coach-user")).thenReturn(Optional.of(
                User.builder().id(2L).role(Role.COACH).build()));
        when(userRepository.findByUsername("client-user")).thenReturn(Optional.of(
                User.builder().id(3L).role(Role.CLIENT).username("client-user").build()));
        when(clientRepository.existsByUserId(3L)).thenReturn(false);

        seeder.run();

        verify(userRepository, never()).save(any());
        verify(clientRepository).save(any(ClientProfile.class));
    }

    @Test
    void clientUsernameTakenByOtherRoleCreatesNoProfile() {
        properties.setEnabled(true);
        when(userRepository.existsByRole(Role.ADMIN)).thenReturn(true);
        when(userRepository.findByUsername("coach-user")).thenReturn(Optional.of(
                User.builder().id(2L).role(Role.COACH).build()));
        when(userRepository.findByUsername("client-user")).thenReturn(Optional.of(
                User.builder().id(3L).role(Role.COACH).username("client-user").build()));

        seeder.run();

        verify(clientRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void coachEmailInUseFailsNamingVariable() {
        properties.setEnabled(true);
        when(userRepository.existsByRole(Role.ADMIN)).thenReturn(true);
        when(userRepository.findByUsername("coach-user")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("coach@test.local")).thenReturn(Optional.of(
                User.builder().id(7L).role(Role.CLIENT).build()));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> seeder.run());

        assertTrue(ex.getMessage().contains("APP_SEED_COACH_EMAIL"));
        verify(userRepository, never()).save(any());
        verify(clientRepository, never()).save(any());
    }

    @Test
    void adminEmailInUseFailsNamingVariable() {
        properties.setEnabled(true);
        when(userRepository.existsByRole(Role.ADMIN)).thenReturn(false);
        when(userRepository.findByUsername("admin-user")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("admin@test.local")).thenReturn(Optional.of(
                User.builder().id(7L).role(Role.COACH).build()));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> seeder.run());

        assertTrue(ex.getMessage().contains("APP_SEED_ADMIN_EMAIL"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void adminUsernameInUseFailsNamingVariable() {
        properties.setEnabled(true);
        when(userRepository.existsByRole(Role.ADMIN)).thenReturn(false);
        when(userRepository.findByUsername("admin-user")).thenReturn(Optional.of(
                User.builder().id(7L).role(Role.COACH).build()));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> seeder.run());

        assertTrue(ex.getMessage().contains("APP_SEED_ADMIN_USERNAME"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void missingVariableFailsNamingIt() {
        properties.setEnabled(true);
        properties.getCoach().setPassword(" ");

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> seeder.run());

        assertTrue(ex.getMessage().contains("APP_SEED_COACH_PASSWORD"));
        verifyNoInteractions(userRepository, clientRepository);
    }
}
