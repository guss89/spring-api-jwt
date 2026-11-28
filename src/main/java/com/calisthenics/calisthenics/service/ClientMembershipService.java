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
import com.calisthenics.calisthenics.security.CurrentUser;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ClientMembershipService {
    private final ClientMembershipRepository clientMembershipRepository;
    private final MembershipPlanRepository membershipPlanRepository;
    private final UserRepository userRepository;
    private final ClientPaymentRepository clientPaymentRepository;

    public ClientMembershipService(
            ClientMembershipRepository clientMembershipRepository,
            MembershipPlanRepository membershipPlanRepository,
            UserRepository userRepository,
            ClientPaymentRepository clientPaymentRepository
    ) {
        this.clientMembershipRepository = clientMembershipRepository;
        this.membershipPlanRepository = membershipPlanRepository;
        this.userRepository = userRepository;
        this.clientPaymentRepository = clientPaymentRepository;
    }

    public ClientMembershipResponse save(ClientMembershipRequest request, CurrentUser caller) {
        caller.requireAccessTo(request.getUserId());
        User user = findClient(request.getUserId());
        MembershipPlan plan = findPlan(request.getMembershipPlanId());
        MembershipStatus status = request.getStatus() != null ? request.getStatus() : MembershipStatus.ACTIVE;

        if (status == MembershipStatus.ACTIVE
                && clientMembershipRepository.existsByUserIdAndStatus(user.getId(), MembershipStatus.ACTIVE)) {
            throw new IllegalArgumentException("User already has an active membership");
        }

        ClientMembership membership = new ClientMembership();
        membership.setUser(user);
        membership.setMembershipPlan(plan);
        membership.setStartDate(request.getStartDate());
        membership.setEndDate(calculateEndDate(request.getStartDate(), plan));
        membership.setStatus(status);

        return toResponse(clientMembershipRepository.save(membership));
    }

    public List<ClientMembershipResponse> getAll(Long userId, CurrentUser caller) {
        List<ClientMembership> memberships;
        if (caller.admin()) {
            memberships = (userId == null)
                    ? clientMembershipRepository.findAll()
                    : clientMembershipRepository.findByUserId(userId);
        } else {
            if (userId != null) {
                caller.requireAccessTo(userId);
            }
            memberships = clientMembershipRepository.findByUserId(caller.id());
        }
        return memberships.stream().map(this::toResponse).toList();
    }

    public ClientMembershipResponse getById(Long id, CurrentUser caller) {
        ClientMembership membership = findById(id);
        caller.requireAccessTo(membership.getUser().getId());
        return toResponse(membership);
    }

    public ClientMembershipResponse update(Long id, ClientMembershipRequest request, CurrentUser caller) {
        ClientMembership membership = findById(id);
        caller.requireAccessTo(membership.getUser().getId());
        caller.requireAccessTo(request.getUserId());
        User user = findClient(request.getUserId());
        MembershipPlan plan = findPlan(request.getMembershipPlanId());
        MembershipStatus status = request.getStatus() != null ? request.getStatus() : membership.getStatus();

        if (!user.getId().equals(membership.getUser().getId())
                && clientPaymentRepository.existsByClientMembershipId(id)) {
            throw new ConflictException("Membership has associated payments and cannot be reassigned to another user");
        }

        if (status == MembershipStatus.ACTIVE
                && clientMembershipRepository.existsByUserIdAndStatusAndIdNot(
                        user.getId(), MembershipStatus.ACTIVE, id)) {
            throw new IllegalArgumentException("User already has an active membership");
        }

        membership.setUser(user);
        membership.setMembershipPlan(plan);
        membership.setStartDate(request.getStartDate());
        membership.setEndDate(calculateEndDate(request.getStartDate(), plan));
        membership.setStatus(status);

        return toResponse(clientMembershipRepository.save(membership));
    }

    public void delete(Long id, CurrentUser caller) {
        ClientMembership membership = findById(id);
        caller.requireAccessTo(membership.getUser().getId());
        if (clientPaymentRepository.existsByClientMembershipId(id)) {
            throw new ConflictException("Membership has associated payments");
        }
        clientMembershipRepository.delete(membership);
    }

    private ClientMembership findById(Long id) {
        return clientMembershipRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client membership not found"));
    }

    private MembershipPlan findPlan(Long planId) {
        return membershipPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Membership plan not found"));
    }

    private User findClient(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (user.getRole() != Role.CLIENT) {
            throw new IllegalArgumentException("User must have role CLIENT");
        }
        return user;
    }

    private LocalDate calculateEndDate(LocalDate startDate, MembershipPlan plan) {
        return startDate.plusDays(plan.getDurationDays() - 1L);
    }

    private ClientMembershipResponse toResponse(ClientMembership membership) {
        return new ClientMembershipResponse(
                membership.getId(),
                membership.getUser().getId(),
                membership.getMembershipPlan().getId(),
                membership.getStartDate(),
                membership.getEndDate(),
                membership.getStatus(),
                membership.getCreatedAt()
        );
    }
}
