package com.calisthenics.calisthenics.service;

import com.calisthenics.calisthenics.dto.MembershipPlanRequest;
import com.calisthenics.calisthenics.dto.MembershipPlanResponse;
import com.calisthenics.calisthenics.exception.ConflictException;
import com.calisthenics.calisthenics.exception.ResourceNotFoundException;
import com.calisthenics.calisthenics.model.MembershipPlan;
import com.calisthenics.calisthenics.repository.ClientMembershipRepository;
import com.calisthenics.calisthenics.repository.MembershipPlanRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MembershipPlanService {
    private final MembershipPlanRepository membershipPlanRepository;
    private final ClientMembershipRepository clientMembershipRepository;

    public MembershipPlanService(
            MembershipPlanRepository membershipPlanRepository,
            ClientMembershipRepository clientMembershipRepository
    ) {
        this.membershipPlanRepository = membershipPlanRepository;
        this.clientMembershipRepository = clientMembershipRepository;
    }

    public MembershipPlanResponse save(MembershipPlanRequest request) {
        MembershipPlan plan = new MembershipPlan();
        plan.setName(request.getName());
        plan.setPrice(request.getPrice());
        plan.setDurationDays(request.getDurationDays());
        return toResponse(membershipPlanRepository.save(plan));
    }

    public List<MembershipPlanResponse> getAll() {
        return membershipPlanRepository.findAll().stream().map(this::toResponse).toList();
    }

    public MembershipPlanResponse getById(Long id) {
        return toResponse(findById(id));
    }

    public MembershipPlanResponse update(Long id, MembershipPlanRequest request) {
        MembershipPlan plan = findById(id);
        plan.setName(request.getName());
        plan.setPrice(request.getPrice());
        plan.setDurationDays(request.getDurationDays());
        return toResponse(membershipPlanRepository.save(plan));
    }

    public void delete(Long id) {
        MembershipPlan plan = findById(id);
        if (clientMembershipRepository.existsByMembershipPlanId(id)) {
            throw new ConflictException("Membership plan has associated memberships");
        }
        membershipPlanRepository.delete(plan);
    }

    private MembershipPlan findById(Long id) {
        return membershipPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Membership plan not found"));
    }

    private MembershipPlanResponse toResponse(MembershipPlan plan) {
        return new MembershipPlanResponse(
                plan.getId(),
                plan.getName(),
                plan.getPrice(),
                plan.getDurationDays(),
                plan.getCreatedAt()
        );
    }
}
