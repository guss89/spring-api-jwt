package com.calisthenics.calisthenics.service;

import com.calisthenics.calisthenics.dto.MembershipPlanRequest;
import com.calisthenics.calisthenics.dto.MembershipPlanResponse;
import com.calisthenics.calisthenics.exception.ConflictException;
import com.calisthenics.calisthenics.exception.ResourceNotFoundException;
import com.calisthenics.calisthenics.model.MembershipPlan;
import com.calisthenics.calisthenics.repository.ClientMembershipRepository;
import com.calisthenics.calisthenics.repository.MembershipPlanRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MembershipPlanServiceTest {

    @Mock private MembershipPlanRepository membershipPlanRepository;
    @Mock private ClientMembershipRepository clientMembershipRepository;
    @InjectMocks private MembershipPlanService service;

    private MembershipPlanRequest request() {
        return new MembershipPlanRequest("Mensual", new BigDecimal("30.00"), 30);
    }

    private MembershipPlan existing() {
        return MembershipPlan.builder().id(1L).name("Viejo").price(new BigDecimal("10.00")).durationDays(7).build();
    }

    @Test
    void save_ok_devuelveResponse() {
        when(membershipPlanRepository.save(any())).thenAnswer(inv -> {
            MembershipPlan p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });

        MembershipPlanResponse res = service.save(request());

        assertThat(res.getId()).isEqualTo(1L);
        assertThat(res.getName()).isEqualTo("Mensual");
        assertThat(res.getPrice()).isEqualByComparingTo("30.00");
        assertThat(res.getDurationDays()).isEqualTo(30);
    }

    @Test
    void getAll_devuelveLista() {
        when(membershipPlanRepository.findAll()).thenReturn(List.of(existing()));
        assertThat(service.getAll()).hasSize(1);
    }

    @Test
    void getById_ok() {
        when(membershipPlanRepository.findById(1L)).thenReturn(Optional.of(existing()));
        assertThat(service.getById(1L).getId()).isEqualTo(1L);
    }

    @Test
    void getById_inexistente_lanza404() {
        when(membershipPlanRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getById(99L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_ok_actualizaCampos() {
        when(membershipPlanRepository.findById(1L)).thenReturn(Optional.of(existing()));
        when(membershipPlanRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        MembershipPlanResponse res = service.update(1L, request());

        assertThat(res.getName()).isEqualTo("Mensual");
        assertThat(res.getPrice()).isEqualByComparingTo("30.00");
        assertThat(res.getDurationDays()).isEqualTo(30);
    }

    @Test
    void update_inexistente_lanza404() {
        when(membershipPlanRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.update(99L, request())).isInstanceOf(ResourceNotFoundException.class);
        verify(membershipPlanRepository, never()).save(any());
    }

    @Test
    void delete_ok() {
        MembershipPlan plan = existing();
        when(membershipPlanRepository.findById(1L)).thenReturn(Optional.of(plan));
        when(clientMembershipRepository.existsByMembershipPlanId(1L)).thenReturn(false);

        service.delete(1L);

        verify(membershipPlanRepository).delete(plan);
    }

    @Test
    void delete_inexistente_lanza404() {
        when(membershipPlanRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.delete(99L)).isInstanceOf(ResourceNotFoundException.class);
        verify(membershipPlanRepository, never()).delete(any());
    }

    @Test
    void delete_conMembresiasAsociadas_lanzaConflicto() {
        when(membershipPlanRepository.findById(1L)).thenReturn(Optional.of(existing()));
        when(clientMembershipRepository.existsByMembershipPlanId(1L)).thenReturn(true);

        assertThatThrownBy(() -> service.delete(1L)).isInstanceOf(ConflictException.class);
        verify(membershipPlanRepository, never()).delete(any());
    }
}
