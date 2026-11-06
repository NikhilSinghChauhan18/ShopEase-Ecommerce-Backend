
package com.example.E_Commerce.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // Registration OTP Email
    public void sendOtpEmail(String toEmail, String otp) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(toEmail);
        message.setSubject("ShopEase - Email Verification OTP");

        message.setText(
                "Hello,\n\n" +
                        "Welcome to ShopEase!\n\n" +
                        "Your email verification OTP is: " + otp + "\n\n" +
                        "This OTP is valid for 5 minutes.\n\n" +
                        "Please do not share this OTP with anyone.\n\n" +
                        "Regards,\n" +
                        "ShopEase Team"
        );

        mailSender.send(message);
    }

    // Successful Login Notification Email
    public void sendLoginSuccessEmail(String toEmail, String userName) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(toEmail);
        message.setSubject("ShopEase - Login Successful");

        message.setText(
                "Hello " + userName + ",\n\n" +
                        "Your email has been successfully used to log in to your ShopEase account.\n\n" +
                        "Your login was successful!\n\n" +
                        "If this wasn't you, please secure your account immediately.\n\n" +
                        "Thank you for choosing ShopEase.\n\n" +
                        "Regards,\n" +
                        "ShopEase Team"
        );

        mailSender.send(message);
    }
}
