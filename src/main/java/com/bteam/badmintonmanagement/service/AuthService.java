package com.bteam.badmintonmanagement.service;

import com.bteam.badmintonmanagement.dto.request.RequestForgotPassword;
import com.bteam.badmintonmanagement.dto.request.RequestLogin;
import com.bteam.badmintonmanagement.dto.request.RequestRegister;
import com.bteam.badmintonmanagement.dto.request.RequestResetPassword;
import com.bteam.badmintonmanagement.dto.response.ResponseLogin;
import com.bteam.badmintonmanagement.dto.response.ResponseRegister;
import com.bteam.badmintonmanagement.entity.user.User;
import com.bteam.badmintonmanagement.entity.user.UserRole;
import com.bteam.badmintonmanagement.entity.user.UserStatus;
import com.bteam.badmintonmanagement.exception.InvalidDataException;
import com.bteam.badmintonmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final SecureRandom secureRandom=new SecureRandom();

    public ResponseRegister register(RequestRegister request)
    {
        String email=request.getEmail().trim().toLowerCase();
        String phoneNumber=request.getPhoneNumber().trim();

        if (userRepository.existsByEmail(email)) {

            throw new InvalidDataException("Email đã tồn tại");
        }

        if (userRepository.existsByPhoneNumber(phoneNumber))
        {

            throw new InvalidDataException("Số điện thoại đã tồn tại");
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
        try {
            Authentication authentication =
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    login,
                                    request.getPassword()
                            )
                    );
        } catch (BadCredentialsException e) {

            throw new InvalidDataException("Tài khoản hoặc mật khẩu không đúng");

        } catch (DisabledException e){

            throw new InvalidDataException("Tài khoản đã bị khóa");
        }


        User user;

        if (login.contains("@")) {
            user = userRepository
                    .findByEmail(login.toLowerCase())
                    .orElseThrow(
                            () -> new InvalidDataException(
                                    "Không tìm thấy tài khoản"
                            )
                    );
        } else {
            user = userRepository
                    .findByPhoneNumber(login)
                    .orElseThrow(
                            () -> new InvalidDataException(
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

    public void forgotPassword(RequestForgotPassword request)
    {
        String email=request.getEmail().trim().toLowerCase();
        User user=userRepository.findByEmail(email)
                .orElseThrow(()->new InvalidDataException("Email không tồn tại"));

        String otp=String.format(
                "%06d",
                secureRandom.nextInt(1_000_000)
        );

        user.setResetOtp(otp);
        userRepository.save(user);

        emailService.sendMailResetPasswordOtp(email,otp);
    }

    public void resetPassword(RequestResetPassword request)
    {
        String email=request.getEmail().trim().toLowerCase();
        User user=userRepository.findByEmail(email)
                .orElseThrow(()->new InvalidDataException("Email không tồn tại"));

        if (!request.getNewPassword()
                .equals(request.getConfirmPassword())) {
            throw new InvalidDataException(
                    "Mật khẩu xác nhận không khớp"
            );
        }

        String savedOtp=user.getResetOtp();
        String requestOtp=request.getOtp().trim();

        if(savedOtp==null || !savedOtp.equals(requestOtp))
        {
            throw new InvalidDataException("Mã otp không hợp lệ");
        }
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));

        user.setResetOtp(null);

        userRepository.save(user);
    }

}
