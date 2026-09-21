package com.bteam.badmintonmanagement.service;

import com.bteam.badmintonmanagement.entity.user.User;
import com.bteam.badmintonmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user;
        if(username.contains("@"))
        {
            user=userRepository.findByEmail(username.toLowerCase())
                    .orElseThrow(()->new UsernameNotFoundException("không tìm thấy tài khoản"));

        }
        else
        {
            user=userRepository.findByPhoneNumber(username)
                    .orElseThrow(()->new UsernameNotFoundException("không tìm thấy tài khoản"));

        }
        return org.springframework.security.core.userdetails.User
                .withUsername(username)
                .password(user.getPasswordHash())
                .authorities(
                        "ROLE_"+user.getRole()
                ).build();
    }
}
