
package com.example.E_Commerce.service;

import com.example.E_Commerce.entity.EmailOtp;
import com.example.E_Commerce.repository.EmailOtpRepository;
import com.example.E_Commerce.repository.UserRepository;
import com.example.E_Commerce.entity.User;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class EmailOtpService {

    private final EmailOtpRepository otpRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    private final SecureRandom secureRandom = new SecureRandom();

    public EmailOtpService(
            EmailOtpRepository otpRepository,
            EmailService emailService,
            PasswordEncoder passwordEncoder,
            UserRepository userRepository) {

        this.otpRepository = otpRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    // Generate and Send Initial OTP
    @Transactional
    public void generateAndSendOtp(String email) {

        String otp = generateOtp();

        LocalDateTime now = LocalDateTime.now();

        // Remove previous OTP
        otpRepository.deleteByEmail(email);

        EmailOtp emailOtp = new EmailOtp();

        emailOtp.setEmail(email);
        emailOtp.setOtpHash(passwordEncoder.encode(otp));
        emailOtp.setExpiryTime(now.plusMinutes(5));
        emailOtp.setAttempts(0);
        emailOtp.setCreatedAt(now);
        emailOtp.setResendCount(0);
        emailOtp.setLastSentAt(now);

        otpRepository.save(emailOtp);

        emailService.sendOtpEmail(email, otp);
    }


    // Generate OTP for an existing unverified user
    @Transactional
    public void generateOtpForUnverifiedUser(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found.")
                );

        if (user.isEmailVerified()) {
            throw new RuntimeException("Email is already verified.");
        }

        LocalDateTime now = LocalDateTime.now();

        EmailOtp emailOtp = otpRepository.findByEmail(email)
                .orElse(null);

        if (emailOtp != null) {

            // Enforce 60-second cooldown
            if (emailOtp.getLastSentAt() != null &&
                    now.isBefore(emailOtp.getLastSentAt().plusSeconds(60))) {

                throw new RuntimeException(
                        "Please wait 60 seconds before requesting another OTP."
                );
            }

            // Reset hourly resend window
            if (!now.isBefore(emailOtp.getCreatedAt().plusHours(1))) {
                emailOtp.setCreatedAt(now);
                emailOtp.setResendCount(0);
            }

            if (emailOtp.getResendCount() >= 3) {
                throw new RuntimeException(
                        "Maximum OTP resend limit reached. Try again later."
                );
            }

            emailOtp.setResendCount(emailOtp.getResendCount() + 1);

        } else {

            emailOtp = new EmailOtp();
            emailOtp.setEmail(email);
            emailOtp.setCreatedAt(now);
            emailOtp.setResendCount(0);
        }

        String otp = generateOtp();

        emailOtp.setOtpHash(passwordEncoder.encode(otp));
        emailOtp.setExpiryTime(now.plusMinutes(5));
        emailOtp.setAttempts(0);
        emailOtp.setLastSentAt(now);

        otpRepository.save(emailOtp);

        emailService.sendOtpEmail(email, otp);
    }

    // Verify OTP
    @Transactional
    public boolean verifyOtp(String email, String enteredOtp) {

        EmailOtp emailOtp = otpRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "OTP not found. Please request a new OTP."
                        )
                );

        LocalDateTime now = LocalDateTime.now();

        if (!now.isBefore(emailOtp.getExpiryTime())) {
            otpRepository.delete(emailOtp);

            throw new RuntimeException(
                    "OTP has expired. Please request a new OTP."
            );
        }

        if (emailOtp.getAttempts() >= 5) {
            otpRepository.delete(emailOtp);

            throw new RuntimeException(
                    "Maximum attempts exceeded. Please request a new OTP."
            );
        }

        if (!passwordEncoder.matches(
                enteredOtp,
                emailOtp.getOtpHash())) {

            emailOtp.setAttempts(emailOtp.getAttempts() + 1);
            otpRepository.save(emailOtp);

            throw new RuntimeException("Invalid OTP. Please try again.");
        }

        otpRepository.delete(emailOtp);

        return true;
    }

    // Resend OTP
    @Transactional
    public void resendOtp(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found.")
                );

        if (user.isEmailVerified()) {
            throw new RuntimeException("Email is already verified.");
        }

        EmailOtp emailOtp = otpRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "OTP not found. Please register again."
                        )
                );

        LocalDateTime now = LocalDateTime.now();

        // 60-second cooldown
        if (emailOtp.getLastSentAt() != null &&
                now.isBefore(emailOtp.getLastSentAt().plusSeconds(60))) {

            throw new RuntimeException(
                    "Please wait 60 seconds before requesting another OTP."
            );
        }

        // Reset resend window after one hour
        if (!now.isBefore(emailOtp.getCreatedAt().plusHours(1))) {
            emailOtp.setCreatedAt(now);
            emailOtp.setResendCount(0);
        }

        // Maximum 3 resends per hour
        if (emailOtp.getResendCount() >= 3) {
            throw new RuntimeException(
                    "Maximum OTP resend limit reached. Try again later."
            );
        }

        String otp = generateOtp();

        emailOtp.setOtpHash(passwordEncoder.encode(otp));
        emailOtp.setExpiryTime(now.plusMinutes(5));
        emailOtp.setAttempts(0);
        emailOtp.setResendCount(emailOtp.getResendCount() + 1);
        emailOtp.setLastSentAt(now);

        otpRepository.save(emailOtp);

        emailService.sendOtpEmail(email, otp);
    }

    // Generate secure six-digit OTP
    private String generateOtp() {

        return String.format(
                "%06d",
                secureRandom.nextInt(1_000_000)
        );
    }
}
