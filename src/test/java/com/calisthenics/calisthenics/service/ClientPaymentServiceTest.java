package com.calisthenics.calisthenics.service;

import com.calisthenics.calisthenics.dto.ClientPaymentRequest;
import com.calisthenics.calisthenics.dto.ClientPaymentResponse;
import com.calisthenics.calisthenics.exception.ResourceNotFoundException;
import com.calisthenics.calisthenics.model.ClientMembership;
import com.calisthenics.calisthenics.model.ClientPayment;
import com.calisthenics.calisthenics.model.User;
import com.calisthenics.calisthenics.model.catalogs.PaymentStatus;
import com.calisthenics.calisthenics.repository.ClientMembershipRepository;
import com.calisthenics.calisthenics.repository.ClientPaymentRepository;
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
class ClientPaymentServiceTest {

    @Mock private ClientPaymentRepository clientPaymentRepository;
    @Mock private UserRepository userRepository;
    @Mock private ClientMembershipRepository clientMembershipRepository;
    @InjectMocks private ClientPaymentService service;

    private User user;
    private ClientMembership membership;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).name("Ana").passwordHash("secret").build();
        membership = ClientMembership.builder().id(5L).user(user).build();
    }

    private ClientPaymentRequest request(PaymentStatus status) {
        ClientPaymentRequest r = new ClientPaymentRequest();
        r.setAmount(new BigDecimal("100.00"));
        r.setPaymentMethod("CASH");
        r.setPaymentDate(LocalDate.of(2026, 1, 10));
        r.setStatus(status);
        r.setReference("REF-1");
        r.setNotes("nota");
        r.setUserId(1L);
        r.setClientMembershipId(5L);
        return r;
    }

    private ClientPayment existing() {
        return ClientPayment.builder().id(10L).user(user).clientMembership(membership)
                .amount(new BigDecimal("50")).status(PaymentStatus.PENDING).build();
    }

    @Test
    void save_ok_devuelveResponse() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(clientMembershipRepository.findById(5L)).thenReturn(Optional.of(membership));
        when(clientPaymentRepository.save(any())).thenAnswer(inv -> {
            ClientPayment p = inv.getArgument(0);
            p.setId(10L);
            return p;
        });

        ClientPaymentResponse res = service.save(request(PaymentStatus.PENDING));

        assertThat(res.getId()).isEqualTo(10L);
        assertThat(res.getUserId()).isEqualTo(1L);
        assertThat(res.getClientMembershipId()).isEqualTo(5L);
        assertThat(res.getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(res.getAmount()).isEqualByComparingTo("100.00");
    }

    @Test
    void save_usuarioInexistente_lanza404() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.save(request(PaymentStatus.PAID)))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(clientPaymentRepository, never()).save(any());
    }

    @Test
    void save_membresiaInexistente_lanza404() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(clientMembershipRepository.findById(5L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.save(request(PaymentStatus.PAID)))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(clientPaymentRepository, never()).save(any());
    }

    @Test
    void save_membresiaDeOtroUsuario_lanza400() {
        User otro = User.builder().id(2L).name("Beto").passwordHash("secret").build();
        membership.setUser(otro);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(clientMembershipRepository.findById(5L)).thenReturn(Optional.of(membership));
        assertThatThrownBy(() -> service.save(request(PaymentStatus.PAID)))
                .isInstanceOf(IllegalArgumentException.class);
        verify(clientPaymentRepository, never()).save(any());
    }

    @Test
    void update_membresiaDeOtroUsuario_lanza400() {
        User otro = User.builder().id(2L).name("Beto").passwordHash("secret").build();
        membership.setUser(otro);
        when(clientPaymentRepository.findById(10L)).thenReturn(Optional.of(existing()));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(clientMembershipRepository.findById(5L)).thenReturn(Optional.of(membership));
        assertThatThrownBy(() -> service.update(10L, request(PaymentStatus.PAID)))
                .isInstanceOf(IllegalArgumentException.class);
        verify(clientPaymentRepository, never()).save(any());
    }

    @Test
    void getAll_sinUserId_usaFindAll() {
        when(clientPaymentRepository.findAll()).thenReturn(List.of(existing()));
        assertThat(service.getAll(null)).hasSize(1);
        verify(clientPaymentRepository, never()).findByUserId(any());
    }

    @Test
    void getAll_conUserId_usaFindByUserId() {
        when(clientPaymentRepository.findByUserId(1L)).thenReturn(List.of(existing()));
        List<ClientPaymentResponse> res = service.getAll(1L);
        assertThat(res).hasSize(1);
        assertThat(res.get(0).getUserId()).isEqualTo(1L);
        verify(clientPaymentRepository, never()).findAll();
    }

    @Test
    void getById_ok() {
        when(clientPaymentRepository.findById(10L)).thenReturn(Optional.of(existing()));
        assertThat(service.getById(10L).getId()).isEqualTo(10L);
    }

    @Test
    void getById_inexistente_lanza404() {
        when(clientPaymentRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getById(99L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_statusNull_noSobrescribe_yGuarda() {
        when(clientPaymentRepository.findById(10L)).thenReturn(Optional.of(existing()));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(clientMembershipRepository.findById(5L)).thenReturn(Optional.of(membership));
        when(clientPaymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ClientPaymentResponse res = service.update(10L, request(null));

        assertThat(res.getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(res.getAmount()).isEqualByComparingTo("100.00");
        verify(clientPaymentRepository).save(any());
    }

    @Test
    void update_statusInformado_seActualiza() {
        when(clientPaymentRepository.findById(10L)).thenReturn(Optional.of(existing()));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(clientMembershipRepository.findById(5L)).thenReturn(Optional.of(membership));
        when(clientPaymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThat(service.update(10L, request(PaymentStatus.PAID)).getStatus()).isEqualTo(PaymentStatus.PAID);
    }

    @Test
    void update_pagoInexistente_lanza404() {
        when(clientPaymentRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.update(99L, request(null))).isInstanceOf(ResourceNotFoundException.class);
        verify(clientPaymentRepository, never()).save(any());
    }

    @Test
    void update_membresiaInexistente_lanza404() {
        when(clientPaymentRepository.findById(10L)).thenReturn(Optional.of(existing()));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(clientMembershipRepository.findById(5L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.update(10L, request(null))).isInstanceOf(ResourceNotFoundException.class);
        verify(clientPaymentRepository, never()).save(any());
    }

    @Test
    void delete_ok() {
        ClientPayment p = existing();
        when(clientPaymentRepository.findById(10L)).thenReturn(Optional.of(p));
        service.delete(10L);
        verify(clientPaymentRepository).delete(p);
    }

    @Test
    void delete_inexistente_lanza404() {
        when(clientPaymentRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.delete(99L)).isInstanceOf(ResourceNotFoundException.class);
        verify(clientPaymentRepository, never()).delete(any());
    }
}
