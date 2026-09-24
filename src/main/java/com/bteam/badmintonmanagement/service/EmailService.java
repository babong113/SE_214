package com.bteam.badmintonmanagement.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;
    public void sendMailResetPasswordOtp(String sendToEmail,String otp)
    {
        SimpleMailMessage message=new SimpleMailMessage();
        message.setTo(sendToEmail);
        message.setSubject("Yêu cầu đổi mật khẩu ");
        message.setText(
                "Mã OTP đặt lại mật khẩu của bạn là: " + otp + "\n\n"
                        + "Vui lòng nhập mã này để đặt lại mật khẩu."

        );

        javaMailSender.send(message);
    }
}