package vn.iotstar.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

import vn.iotstar.dto.ForgotPasswordDTO;
import vn.iotstar.dto.RegisterDTO;
import vn.iotstar.dto.ResetPasswordDTO;
import vn.iotstar.service.AuthService;
import vn.iotstar.service.OtpService;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final OtpService otpService;

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String register(Model model) {

        if (!model.containsAttribute("registerDTO")) {
            model.addAttribute(
                    "registerDTO",
                    new RegisterDTO()
            );
        }

        return "auth/register";
    }

    @PostMapping("/register")
    public String register(
            @Valid
            @ModelAttribute("registerDTO")
            RegisterDTO dto,

            BindingResult result,

            RedirectAttributes redirect,
            Model model) {

        if (result.hasErrors()) {
            return "auth/register";
        }

        try {

            authService.register(dto);

            redirect.addFlashAttribute(
                    "success",
                    "Đăng ký thành công. OTP đã được gửi đến email."
            );

            redirect.addFlashAttribute(
                    "email",
                    dto.getEmail()
            );

            return "redirect:/verify-otp?email=" + dto.getEmail();

        } catch (IllegalArgumentException e) {

            model.addAttribute(
                    "error",
                    e.getMessage()
            );

            return "auth/register";
        }
    }

    @GetMapping("/verify-otp")
    public String verifyOtp(
            @RequestParam(required = false) String email,
            Model model) {

        model.addAttribute(
                "email",
                email == null ? "" : email
        );

        return "auth/verify-otp";
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(
            @RequestParam String email,
            @RequestParam String otp,
            RedirectAttributes redirect) {

        boolean result =
                authService.verifyRegister(
                        email,
                        otp
                );

        if (!result) {

            redirect.addFlashAttribute(
                    "error",
                    "OTP không hợp lệ hoặc đã hết hạn."
            );

            return "redirect:/verify-otp?email=" + email;
        }

        redirect.addFlashAttribute(
                "success",
                "Xác thực thành công. Hãy đăng nhập."
        );

        return "redirect:/login";
    }

    @GetMapping("/resend-register-otp")
    public String resendRegisterOtp(
            @RequestParam(required = false) String email,
            RedirectAttributes redirect) {

        if (email == null || email.trim().isEmpty()) {

            redirect.addFlashAttribute(
                    "error",
                    "Email không được để trống."
            );

            return "redirect:/verify-otp";
        }

        try {

            otpService.sendRegisterOtp(
                    email.trim()
            );

            redirect.addFlashAttribute(
                    "success",
                    "Đã gửi lại OTP."
            );

        } catch (RuntimeException e) {

            redirect.addFlashAttribute(
                    "error",
                    e.getMessage()
            );
        }

        return "redirect:/verify-otp?email=" + email.trim();
    }

    @PostMapping("/resend-register-otp")
    public String resendRegisterOtpPost(
            @RequestParam String email,
            RedirectAttributes redirect) {

        try {

            otpService.sendRegisterOtp(
                    email.trim()
            );

            redirect.addFlashAttribute(
                    "success",
                    "Đã gửi lại OTP."
            );

        } catch (RuntimeException e) {

            redirect.addFlashAttribute(
                    "error",
                    e.getMessage()
            );
        }

        return "redirect:/verify-otp?email=" + email.trim();
    }

    @GetMapping("/forgot-password")
    public String forgotPassword(Model model) {

        if (!model.containsAttribute("forgotPasswordDTO")) {

            model.addAttribute(
                    "forgotPasswordDTO",
                    new ForgotPasswordDTO()
            );
        }

        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(
            @Valid
            @ModelAttribute("forgotPasswordDTO")
            ForgotPasswordDTO dto,

            BindingResult result,

            Model model,

            RedirectAttributes redirect) {

        if (result.hasErrors()) {
            return "auth/forgot-password";
        }

        try {

            authService.forgotPassword(
                    dto.getEmail()
            );

            redirect.addFlashAttribute(
                    "success",
                    "OTP đặt lại mật khẩu đã được gửi."
            );

            return "redirect:/reset-password?email="
                    + dto.getEmail();

        } catch (IllegalArgumentException e) {

            model.addAttribute(
                    "error",
                    e.getMessage()
            );

            return "auth/forgot-password";
        }
    }

    @GetMapping("/reset-password")
    public String resetPassword(
            @RequestParam(required = false) String email,
            Model model) {

        ResetPasswordDTO dto =
                new ResetPasswordDTO();

        if (email != null && !email.trim().isEmpty()) {
            dto.setEmail(email.trim());
        }

        model.addAttribute(
                "resetPasswordDTO",
                dto
        );

        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(
            @Valid
            @ModelAttribute("resetPasswordDTO")
            ResetPasswordDTO dto,

            BindingResult result,

            @RequestParam String otp,

            Model model,

            RedirectAttributes redirect) {

        if (!dto.getPassword().equals(
                dto.getConfirmPassword())) {

            result.reject(
                    "password.error",
                    "Mật khẩu xác nhận không đúng."
            );
        }

        if (result.hasErrors()) {
            return "auth/reset-password";
        }

        boolean verified =
                authService.verifyResetOtp(
                        dto.getEmail(),
                        otp
                );

        if (!verified) {

            model.addAttribute(
                    "error",
                    "OTP không hợp lệ hoặc đã hết hạn."
            );

            return "auth/reset-password";
        }

        authService.resetPassword(
                dto.getEmail(),
                dto.getPassword()
        );

        redirect.addFlashAttribute(
                "success",
                "Đổi mật khẩu thành công."
        );

        return "redirect:/login";
    }

    @GetMapping("/logout")
    public String logout(
            HttpServletRequest request) {

        HttpSession session =
                request.getSession(false);

        if (session != null) {
            session.invalidate();
        }

        return "redirect:/login?logout=true";
    }
}