package com.mall.common.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class MailUtil {

    @Autowired
    private JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String fromEmail;

    public String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    public void sendVerificationEmail(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("【在线商城】邮箱验证码");
        message.setText("尊敬的用户，您好！\n\n" +
                "您正在注册在线商城账号，您的验证码是：" + code + "\n\n" +
                "验证码5分钟内有效，请尽快完成注册。\n" +
                "如非本人操作，请忽略此邮件。\n\n" +
                "在线商城团队");
        mailSender.send(message);
    }
    
    public void sendResetPasswordEmail(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("【在线商城】重置密码验证码");
        message.setText("尊敬的用户，您好！\n\n" +
                "您正在重置在线商城账号密码，您的验证码是：" + code + "\n\n" +
                "验证码5分钟内有效，请尽快完成密码重置。\n" +
                "如非本人操作，请立即修改密码以保护账号安全。\n\n" +
                "在线商城团队");
        mailSender.send(message);
    }
}