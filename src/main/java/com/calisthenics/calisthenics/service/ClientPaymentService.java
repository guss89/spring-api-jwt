package com.calisthenics.calisthenics.service;

import com.calisthenics.calisthenics.dto.ClientPaymentRequest;
import com.calisthenics.calisthenics.dto.ClientPaymentResponse;
import com.calisthenics.calisthenics.exception.ResourceNotFoundException;
import com.calisthenics.calisthenics.model.ClientPayment;
import com.calisthenics.calisthenics.model.User;
import com.calisthenics.calisthenics.repository.ClientPaymentRepository;
import com.calisthenics.calisthenics.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClientPaymentService {
    private final UserRepository userRepository;
    private final ClientPaymentRepository clientPaymentRepository;

    public ClientPaymentService(
            ClientPaymentRepository clientPaymentRepository,
            UserRepository userRepository
    ){
        this.userRepository = userRepository;
        this.clientPaymentRepository = clientPaymentRepository;
    }

    public ClientPaymentResponse save(ClientPaymentRequest request) {
        User user = userRepository.findById(request.getClientId())
                .orElseThrow(()->new ResourceNotFoundException("User not found"));

        ClientPayment clientPayment =  new ClientPayment();
        clientPayment.setAmount(request.getAmount());
        clientPayment.setPaymentDate(request.getPaymentDate());
        clientPayment.setStatus(request.getStatus());
        clientPayment.setClient(user);

        ClientPayment saved = clientPaymentRepository.save(clientPayment);

        return  new ClientPaymentResponse(
                saved.getId(),
                saved.getAmount(),
                saved.getPaymentDate(),
                saved.getStatus(),
                saved.getClient().getId()
        );
    }
    public List<ClientPayment> getAll(){return clientPaymentRepository.findAll(); }

    public ClientPayment findById(Long id){
        return clientPaymentRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Client Payment not found"));
    }

    public ClientPayment update(Long id, ClientPaymentRequest request){
        ClientPayment clientPayment = findById(id);
        User user = userRepository.findById(request.getClientId())
                .orElseThrow(()->new ResourceNotFoundException("User not found"));

        clientPayment.setAmount(request.getAmount());
        clientPayment.setStatus(request.getStatus());
        clientPayment.setPaymentDate(request.getPaymentDate());
        clientPayment.setClient(user);
        return clientPaymentRepository.save(clientPayment);
    }

}
