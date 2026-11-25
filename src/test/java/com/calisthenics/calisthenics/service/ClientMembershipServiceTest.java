package com.calisthenics.calisthenics.service;

import com.calisthenics.calisthenics.dto.ClientMembershipRequest;
import com.calisthenics.calisthenics.dto.ClientMembershipResponse;
import com.calisthenics.calisthenics.exception.ConflictException;
import com.calisthenics.calisthenics.exception.ResourceNotFoundException;
import com.calisthenics.calisthenics.model.ClientMembership;
import com.calisthenics.calisthenics.model.MembershipPlan;
import com.calisthenics.calisthenics.model.User;
import com.calisthenics.calisthenics.model.catalogs.MembershipStatus;
import com.calisthenics.calisthenics.model.catalogs.Role;
import com.calisthenics.calisthenics.repository.ClientMembershipRepository;
import com.calisthenics.calisthenics.repository.ClientPaymentRepository;
import com.calisthenics.calisthenics.repository.MembershipPlanRepository;
import com.calisthenics.calisthenics.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientMembershipServiceTest {

    @Mock private ClientMembershipRepository clientMembershipRepository;
    @Mock private MembershipPlanRepository membershipPlanRepository;
    @Mock private UserRepository userRepository;
    @Mock private ClientPaymentRepository clientPaymentRepository;
    @InjectMocks private ClientMembershipService service;

    private User user;
    private MembershipPlan plan;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).name("Ana").passwordHash("secret").role(Role.CLIENT).build();
        plan = MembershipPlan.builder().id(2L).name("Mensual").price(new BigDecimal("30.00")).durationDays(30).build();
    }

    private ClientMembershipRequest request(MembershipStatus status) {
        ClientMembershipRequest r = new ClientMembershipRequest();
        r.setUserId(1L);
        r.setMembershipPlanId(2L);
        r.setStartDate(LocalDate.of(2026, 1, 1));
        r.setStatus(status);
        return r;
    }

    private ClientMembership existing(MembershipStatus status) {
        return ClientMembership.builder().id(10L).user(user).membershipPlan(plan)
                .startDate(LocalDate.of(2025, 1, 1)).endDate(LocalDate.of(2025, 1, 30)).status(status).build();
    }

    // ---- save ----

    @Test
    void save_ok_calculaEndDateInclusivo_yStatusActivoPorDefecto() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(membershipPlanRepository.findById(2L)).thenReturn(Optional.of(plan));
        when(clientMembershipRepository.existsByUserIdAndStatus(1L, MembershipStatus.ACTIVE)).thenReturn(false);
        when(clientMembershipRepository.save(any())).thenAnswer(inv -> {
            ClientMembership m = inv.getArgument(0);
            m.setId(10L);
            return m;
        });

        ClientMembershipResponse res = service.save(request(null));

        assertThat(res.getId()).isEqualTo(10L);
        assertThat(res.getUserId()).isEqualTo(1L);
        assertThat(res.getMembershipPlanId()).isEqualTo(2L);
        assertThat(res.getStartDate()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(res.getEndDate()).isEqualTo(LocalDate.of(2026, 1, 30));
        assertThat(res.getStatus()).isEqualTo(MembershipStatus.ACTIVE);
    }

    @Test
    void save_planDeUnDia_endDateIgualAStartDate() {
        plan.setDurationDays(1);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(membershipPlanRepository.findById(2L)).thenReturn(Optional.of(plan));
        when(clientMembershipRepository.existsByUserIdAndStatus(1L, MembershipStatus.ACTIVE)).thenReturn(false);
        when(clientMembershipRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThat(service.save(request(null)).getEndDate()).isEqualTo(LocalDate.of(2026, 1, 1));
    }

    @Test
    void save_statusExpired_noValidaMembresiaActiva() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(membershipPlanRepository.findById(2L)).thenReturn(Optional.of(plan));
        when(clientMembershipRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ClientMembershipResponse res = service.save(request(MembershipStatus.EXPIRED));

        assertThat(res.getStatus()).isEqualTo(MembershipStatus.EXPIRED);
        verify(clientMembershipRepository, never()).existsByUserIdAndStatus(any(), any());
    }

    @Test
    void save_usuarioInexistente_lanza404() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.save(request(null))).isInstanceOf(ResourceNotFoundException.class);
        verify(clientMembershipRepository, never()).save(any());
    }

    @Test
    void save_planInexistente_lanza404() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(membershipPlanRepository.findById(2L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.save(request(null))).isInstanceOf(ResourceNotFoundException.class);
        verify(clientMembershipRepository, never()).save(any());
    }

    @Test
    void save_usuarioNoCliente_lanza400() {
        user.setRole(Role.COACH);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        assertThatThrownBy(() -> service.save(request(null))).isInstanceOf(IllegalArgumentException.class);
        verify(clientMembershipRepository, never()).save(any());
    }

    @Test
    void save_usuarioYaTieneMembresiaActiva_lanza400() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(membershipPlanRepository.findById(2L)).thenReturn(Optional.of(plan));
        when(clientMembershipRepository.existsByUserIdAndStatus(1L, MembershipStatus.ACTIVE)).thenReturn(true);
        assertThatThrownBy(() -> service.save(request(null))).isInstanceOf(IllegalArgumentException.class);
        verify(clientMembershipRepository, never()).save(any());
    }

    // ---- getAll / getById ----

    @Test
    void getAll_sinUserId_usaFindAll() {
        when(clientMembershipRepository.findAll()).thenReturn(List.of(existing(MembershipStatus.ACTIVE)));
        assertThat(service.getAll(null)).hasSize(1);
        verify(clientMembershipRepository, never()).findByUserId(any());
    }

    @Test
    void getAll_conUserId_usaFindByUserId() {
        when(clientMembershipRepository.findByUserId(1L)).thenReturn(List.of(existing(MembershipStatus.ACTIVE)));
        List<ClientMembershipResponse> res = service.getAll(1L);
        assertThat(res).hasSize(1);
        assertThat(res.get(0).getUserId()).isEqualTo(1L);
        verify(clientMembershipRepository, never()).findAll();
    }

    @Test
    void getById_ok() {
        when(clientMembershipRepository.findById(10L)).thenReturn(Optional.of(existing(MembershipStatus.ACTIVE)));
        assertThat(service.getById(10L).getId()).isEqualTo(10L);
    }

    @Test
    void getById_inexistente_lanza404() {
        when(clientMembershipRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getById(99L)).isInstanceOf(ResourceNotFoundException.class);
    }

    // ---- update ----

    @Test
    void update_statusNull_noSobrescribe_yRecalculaEndDate() {
        when(clientMembershipRepository.findById(10L)).thenReturn(Optional.of(existing(MembershipStatus.EXPIRED)));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(membershipPlanRepository.findById(2L)).thenReturn(Optional.of(plan));
        when(clientMembershipRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ClientMembershipResponse res = service.update(10L, request(null));

        assertThat(res.getStatus()).isEqualTo(MembershipStatus.EXPIRED);
        assertThat(res.getStartDate()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(res.getEndDate()).isEqualTo(LocalDate.of(2026, 1, 30));
    }

    @Test
    void update_cambiaPlan_recalculaEndDate() {
        MembershipPlan semanal = MembershipPlan.builder().id(3L).durationDays(7).build();
        ClientMembershipRequest req = request(MembershipStatus.CANCELLED);
        req.setMembershipPlanId(3L);
        when(clientMembershipRepository.findById(10L)).thenReturn(Optional.of(existing(MembershipStatus.ACTIVE)));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(membershipPlanRepository.findById(3L)).thenReturn(Optional.of(semanal));
        when(clientMembershipRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ClientMembershipResponse res = service.update(10L, req);

        assertThat(res.getMembershipPlanId()).isEqualTo(3L);
        assertThat(res.getEndDate()).isEqualTo(LocalDate.of(2026, 1, 7));
        assertThat(res.getStatus()).isEqualTo(MembershipStatus.CANCELLED);
    }

    @Test
    void update_aActivaConOtraActiva_lanza400() {
        when(clientMembershipRepository.findById(10L)).thenReturn(Optional.of(existing(MembershipStatus.EXPIRED)));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(membershipPlanRepository.findById(2L)).thenReturn(Optional.of(plan));
        when(clientMembershipRepository.existsByUserIdAndStatusAndIdNot(1L, MembershipStatus.ACTIVE, 10L))
                .thenReturn(true);

        assertThatThrownBy(() -> service.update(10L, request(MembershipStatus.ACTIVE)))
                .isInstanceOf(IllegalArgumentException.class);
        verify(clientMembershipRepository, never()).save(any());
    }

    @Test
    void update_activaSinOtraActiva_ok() {
        when(clientMembershipRepository.findById(10L)).thenReturn(Optional.of(existing(MembershipStatus.ACTIVE)));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(membershipPlanRepository.findById(2L)).thenReturn(Optional.of(plan));
        when(clientMembershipRepository.existsByUserIdAndStatusAndIdNot(1L, MembershipStatus.ACTIVE, 10L))
                .thenReturn(false);
        when(clientMembershipRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThat(service.update(10L, request(null)).getStatus()).isEqualTo(MembershipStatus.ACTIVE);
    }

    @Test
    void update_membresiaInexistente_lanza404() {
        when(clientMembershipRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.update(99L, request(null))).isInstanceOf(ResourceNotFoundException.class);
        verify(clientMembershipRepository, never()).save(any());
    }

    @Test
    void update_usuarioNoCliente_lanza400() {
        user.setRole(Role.ADMIN);
        when(clientMembershipRepository.findById(10L)).thenReturn(Optional.of(existing(MembershipStatus.ACTIVE)));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        assertThatThrownBy(() -> service.update(10L, request(null))).isInstanceOf(IllegalArgumentException.class);
        verify(clientMembershipRepository, never()).save(any());
    }

    @Test
    void update_planInexistente_lanza404() {
        when(clientMembershipRepository.findById(10L)).thenReturn(Optional.of(existing(MembershipStatus.ACTIVE)));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(membershipPlanRepository.findById(2L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.update(10L, request(null))).isInstanceOf(ResourceNotFoundException.class);
        verify(clientMembershipRepository, never()).save(any());
    }

    @Test
    void update_cambiaDeUsuarioConPagos_lanzaConflicto() {
        User otro = User.builder().id(7L).name("Beto").passwordHash("secret").role(Role.CLIENT).build();
        ClientMembershipRequest req = request(null);
        req.setUserId(7L);
        when(clientMembershipRepository.findById(10L)).thenReturn(Optional.of(existing(MembershipStatus.EXPIRED)));
        when(userRepository.findById(7L)).thenReturn(Optional.of(otro));
        when(membershipPlanRepository.findById(2L)).thenReturn(Optional.of(plan));
        when(clientPaymentRepository.existsByClientMembershipId(10L)).thenReturn(true);

        assertThatThrownBy(() -> service.update(10L, req)).isInstanceOf(ConflictException.class);
        verify(clientMembershipRepository, never()).save(any());
    }

    @Test
    void update_cambiaDeUsuarioSinPagos_permiteCambio() {
        User otro = User.builder().id(7L).name("Beto").passwordHash("secret").role(Role.CLIENT).build();
        ClientMembershipRequest req = request(null);
        req.setUserId(7L);
        when(clientMembershipRepository.findById(10L)).thenReturn(Optional.of(existing(MembershipStatus.EXPIRED)));
        when(userRepository.findById(7L)).thenReturn(Optional.of(otro));
        when(membershipPlanRepository.findById(2L)).thenReturn(Optional.of(plan));
        when(clientPaymentRepository.existsByClientMembershipId(10L)).thenReturn(false);
        when(clientMembershipRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThat(service.update(10L, req).getUserId()).isEqualTo(7L);
    }

    // ---- delete ----

    @Test
    void delete_ok() {
        ClientMembership m = existing(MembershipStatus.ACTIVE);
        when(clientMembershipRepository.findById(10L)).thenReturn(Optional.of(m));
        when(clientPaymentRepository.existsByClientMembershipId(10L)).thenReturn(false);

        service.delete(10L);

        verify(clientMembershipRepository).delete(m);
    }

    @Test
    void delete_inexistente_lanza404() {
        when(clientMembershipRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.delete(99L)).isInstanceOf(ResourceNotFoundException.class);
        verify(clientMembershipRepository, never()).delete(any());
    }

    @Test
    void delete_conPagosAsociados_lanzaConflicto() {
        when(clientMembershipRepository.findById(10L)).thenReturn(Optional.of(existing(MembershipStatus.ACTIVE)));
        when(clientPaymentRepository.existsByClientMembershipId(10L)).thenReturn(true);

        assertThatThrownBy(() -> service.delete(10L)).isInstanceOf(ConflictException.class);
        verify(clientMembershipRepository, never()).delete(any());
    }
}
