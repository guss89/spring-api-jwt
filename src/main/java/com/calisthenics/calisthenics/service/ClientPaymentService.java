package com.calisthenics.calisthenics.service;

import com.calisthenics.calisthenics.dto.ClientPaymentRequest;
import com.calisthenics.calisthenics.dto.ClientPaymentResponse;
import com.calisthenics.calisthenics.exception.ResourceNotFoundException;
import com.calisthenics.calisthenics.model.ClientMembership;
import com.calisthenics.calisthenics.model.ClientPayment;
import com.calisthenics.calisthenics.model.User;
import com.calisthenics.calisthenics.repository.ClientMembershipRepository;
import com.calisthenics.calisthenics.repository.ClientPaymentRepository;
import com.calisthenics.calisthenics.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClientPaymentService {
    private final UserRepository userRepository;
    private final ClientPaymentRepository clientPaymentRepository;
    private final ClientMembershipRepository clientMembershipRepository;

    public ClientPaymentService(
            ClientPaymentRepository clientPaymentRepository,
            UserRepository userRepository,
            ClientMembershipRepository clientMembershipRepository
    ){
        this.userRepository = userRepository;
        this.clientPaymentRepository = clientPaymentRepository;
        this.clientMembershipRepository = clientMembershipRepository;
    }

    public ClientPaymentResponse save(ClientPaymentRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(()->new ResourceNotFoundException("User not found"));
        ClientMembership clientMembership = clientMembershipRepository.findById(request.getClientMembershipId())
                .orElseThrow(()->new ResourceNotFoundException("Membership not found"));
        validateMembershipOwner(clientMembership, user);

        ClientPayment clientPayment =  new ClientPayment();
        clientPayment.setAmount(request.getAmount());
        clientPayment.setPaymentMethod(request.getPaymentMethod());
        clientPayment.setPaymentDate(request.getPaymentDate());
        clientPayment.setStatus(request.getStatus());
        clientPayment.setReference(request.getReference());
        clientPayment.setNotes(request.getNotes());
        clientPayment.setUser(user);
        clientPayment.setClientMembership(clientMembership);

        ClientPayment saved = clientPaymentRepository.save(clientPayment);

        return toResponse(saved);
    }

    public List<ClientPaymentResponse> getAll(Long userId){
        List<ClientPayment> payments = (userId == null)
                ? clientPaymentRepository.findAll()
                : clientPaymentRepository.findByUserId(userId);
        return payments.stream().map(this::toResponse).toList();
    }

    public ClientPaymentResponse getById(Long id){
        return toResponse(findById(id));
    }

    public ClientPayment findById(Long id){
        return clientPaymentRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Client Payment not found"));
    }

    public ClientPaymentResponse update(Long id, ClientPaymentRequest request){
        ClientPayment clientPayment = findById(id);
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(()->new ResourceNotFoundException("User not found"));

        ClientMembership clientMembership = clientMembershipRepository.findById(request.getClientMembershipId())
                .orElseThrow(()->new ResourceNotFoundException("Membership not found"));
        validateMembershipOwner(clientMembership, user);

        clientPayment.setAmount(request.getAmount());
        clientPayment.setPaymentMethod(request.getPaymentMethod());
        clientPayment.setPaymentDate(request.getPaymentDate());
        if (request.getStatus() != null) {
            clientPayment.setStatus(request.getStatus());
        }
        clientPayment.setReference(request.getReference());
        clientPayment.setNotes(request.getNotes());
        clientPayment.setUser(user);
        clientPayment.setClientMembership(clientMembership);

        return toResponse(clientPaymentRepository.save(clientPayment));
    }

    public void delete(Long id){
        ClientPayment clientPayment = findById(id);
        clientPaymentRepository.delete(clientPayment);
    }

    private void validateMembershipOwner(ClientMembership clientMembership, User user){
        if (clientMembership.getUser() == null || !user.getId().equals(clientMembership.getUser().getId())) {
            throw new IllegalArgumentException("Membership does not belong to the user");
        }
    }

    private ClientPaymentResponse toResponse(ClientPayment clientPayment){
        return new ClientPaymentResponse(
                clientPayment.getId(),
                clientPayment.getAmount(),
                clientPayment.getPaymentMethod(),
                clientPayment.getPaymentDate(),
                clientPayment.getStatus(),
                clientPayment.getReference(),
                clientPayment.getNotes(),
                clientPayment.getCreatedAt(),
                clientPayment.getUser().getId(),
                clientPayment.getClientMembership() != null ? clientPayment.getClientMembership().getId() : null
        );
    }

}
