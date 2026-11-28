package com.calisthenics.calisthenics.security;

import com.calisthenics.calisthenics.model.User;
import com.calisthenics.calisthenics.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    private CustomUserDetailsService(UserRepository userRepository){
        this.userRepository = userRepository;
    }


    @Override
    public  UserDetails loadUserByUsername(String email) throws  UsernameNotFoundException{

        User user = userRepository.findByEmail(email)
                .orElseThrow(()->new UsernameNotFoundException("User not found"));

        return org.springframework.security.core.userdetails.User
                .builder()
                .username(user.getEmail())
                .password(user.getPasswordHash())
                .roles(user.getRole().name())
                .disabled(!Boolean.TRUE.equals(user.getActive()))
                .build();
    }
}

