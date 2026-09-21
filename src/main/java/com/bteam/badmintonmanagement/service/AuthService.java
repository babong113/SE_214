package com.bteam.badmintonmanagement.service;

import com.bteam.badmintonmanagement.dto.request.RequestLogin;
import com.bteam.badmintonmanagement.dto.request.RequestRegister;
import com.bteam.badmintonmanagement.dto.response.ResponseLogin;
import com.bteam.badmintonmanagement.dto.response.ResponseRegister;
import com.bteam.badmintonmanagement.entity.user.User;
import com.bteam.badmintonmanagement.entity.user.UserRole;
import com.bteam.badmintonmanagement.entity.user.UserStatus;
import com.bteam.badmintonmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public ResponseRegister register(RequestRegister request)
    {
        String email=request.getEmail().trim().toLowerCase();
        String phoneNumber=request.getPhoneNumber().trim();

        if (userRepository.existsByEmail(email)) {

            throw new RuntimeException("Email đã tồn tại");
        }

        if (userRepository.existsByPhoneNumber(phoneNumber))
        {

            throw new RuntimeException("Số điện thoại đã tồn tại");
        }

        User user=User.builder()
                .fullName(request.getFullName().trim())
                .phoneNumber(request.getPhoneNumber().trim())
                .email(request.getEmail().trim().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(String.valueOf(UserRole.CUSTOMER))
                .status(String.valueOf(UserStatus.ACTIVE))
                .build();

        User savedUser =
                userRepository.save(user);

        return ResponseRegister.builder()
                .id(savedUser.getId())
                .fullName(savedUser.getFullName())
                .phoneNumber(savedUser.getPhoneNumber())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .status(savedUser.getStatus())
                .build();
    }

    public ResponseLogin login(RequestLogin request)
    {
        String login=request.getLogin().trim().toLowerCase();
        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                login,
                                request.getPassword()
                        )
                );

        User user;

        if (login.contains("@")) {
            user = userRepository
                    .findByEmail(login.toLowerCase())
                    .orElseThrow(
                            () -> new RuntimeException(
                                    "Không tìm thấy tài khoản"
                            )
                    );
        } else {
            user = userRepository
                    .findByPhoneNumber(login)
                    .orElseThrow(
                            () -> new RuntimeException(
                                    "Không tìm thấy tài khoản"
                            )
                    );
        }
        return ResponseLogin.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .build();
    }

}
