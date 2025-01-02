package com.todoapp.service;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Arrays;
import java.util.stream.Collectors;

public class EmailService {
    private static final String SENDER_EMAIL = "marcusmah6969@gmail.com";
    private static final String PASSWORD = "dghf nelm wsuh wknf"; // Don't use regular password

    public static void sendEmail(String recipient, String subject, String htmlContent) {
        System.out.println("Configuring email properties...");
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.ssl.protocols", "TLSv1.2");
        properties.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        properties.put("mail.debug", "true"); // Add debug mode

        try {
            System.out.println("Creating mail session...");
            Session session = Session.getInstance(properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(SENDER_EMAIL, PASSWORD);
                }
            });

            System.out.println("Creating message...");
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER_EMAIL));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
            message.setSubject(subject);
            message.setContent(htmlContent, "text/html; charset=utf-8");

            System.out.println("Sending email to: " + recipient);
            Transport.send(message);
            System.out.println("Email sent successfully!");
        } catch (MessagingException e) {
            System.err.println("Failed to send email: " + e.getMessage());
            System.err.println("Detailed error:");
            e.printStackTrace();
        }
    }

    public static void sendWelcomeEmail(String recipient, String username) {
        try {
            String subject = "Welcome to To-Do List App!";
            // Convert images to base64 to embed in email
            String todoIconBase64 = convertImageToBase64("/images/todo-icon.png");
            String githubIconBase64 = convertImageToBase64("/images/github.png");
            
            String htmlContent = String.format("""
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f8f9fa;">
                    <div style="background-color: white; border-radius: 10px; padding: 30px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
                        <div style="text-align: center; margin-bottom: 30px;">
                            <img src="data:image/png;base64,%s" 
                                 alt="ToDo App Logo" 
                                 style="width: 80px; height: 80px;">
                        </div>
                        
                        <h1 style="color: #2C3E50; text-align: center; font-size: 28px; margin-bottom: 30px;">
                            Welcome to ToDo List App!
                        </h1>
                        
                        <p style="color: #34495E; font-size: 16px; line-height: 1.6;">Dear %s,</p>
                        
                        <p style="color: #34495E; font-size: 16px; line-height: 1.6;">
                            Thank you for joining us! Here's what you can do with our app:
                        </p>
                        
                        <ul style="color: #34495E; font-size: 16px; line-height: 1.8; background-color: #f8f9fa; 
                                   padding: 20px 40px; border-radius: 8px; margin: 20px 0;">
                            <li style="margin-bottom: 12px;">✨ Create and manage tasks efficiently</li>
                            <li style="margin-bottom: 12px;">📅 Set due dates and track priorities</li>
                            <li style="margin-bottom: 12px;">📧 Receive timely email reminders</li>
                            <li style="margin-bottom: 12px;">📊 Monitor your productivity</li>
                        </ul>
                        
                        <p style="color: #34495E; font-size: 16px; line-height: 1.6; margin-top: 30px;">
                            Best regards,<br>
                            To-Do List App Team
                        </p>
                        
                        <div style="border-top: 1px solid #BDC3C7; margin-top: 30px; padding-top: 20px; 
                                    text-align: center;">
                            <p style="color: #7F8C8D; font-size: 12px; margin-bottom: 15px;">
                                This email was sent to %s
                            </p>
                            <a href="https://github.com/MarcusMQF/FOP-ToDoList-App" 
                               style="display: inline-block; text-decoration: none;">
                                <img src="data:image/png;base64,%s" 
                                     alt="GitHub" 
                                     style="width: 24px; height: 24px;">
                            </a>
                        </div>
                    </div>
                </div>
                """, todoIconBase64, username, recipient, githubIconBase64);

            sendEmail(recipient, subject, htmlContent);
        } catch (Exception e) {
            System.err.println("Failed to send welcome email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String convertImageToBase64(String imagePath) {
        try {
            // Adjust path to look in the resources folder
            InputStream is = EmailService.class.getResourceAsStream("/todoapp/resources/images" + imagePath);
            if (is == null) {
                System.err.println("Trying alternative path...");
                // Try alternative path if first one fails
                is = EmailService.class.getResourceAsStream("/images" + imagePath);
                if (is == null) {
                    throw new IOException("Image not found at paths: /todoapp/resources/images" + imagePath + 
                                       " or /images" + imagePath);
                }
            }
            byte[] imageBytes = is.readAllBytes();
            return Base64.getEncoder().encodeToString(imageBytes);
        } catch (IOException e) {
            System.err.println("Failed to load image: " + imagePath);
            e.printStackTrace();
            return "";
        }
    }

    public static void sendTaskReminderEmail(String recipient, String username, String taskDetails) {
        try {
            String subject = "Task Due Reminder";
            String todoIconBase64 = convertImageToBase64("todo-icon.png");
            String githubIconBase64 = convertImageToBase64("github.png");
            
            String htmlContent = String.format("""
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f8f9fa;">
                    <div style="background-color: white; border-radius: 10px; padding: 30px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
                        <div style="text-align: center; margin-bottom: 30px;">
                            <img src="data:image/png;base64,%s" 
                                 alt="ToDo App Logo" 
                                 style="width: 80px; height: 80px;">
                        </div>
                        
                        <h1 style="color: #2C3E50; text-align: center; font-size: 28px; margin-bottom: 30px;">
                            Task Due Reminder
                        </h1>
                        
                        <p style="color: #34495E; font-size: 16px; line-height: 1.6;">Dear %s,</p>
                        
                        <p style="color: #34495E; font-size: 16px; line-height: 1.6;">
                            This is a reminder that you have the following task(s) due within 24 hours:
                        </p>
                        
                        <ul style="color: #34495E; font-size: 16px; line-height: 1.8; background-color: #f8f9fa; 
                                   padding: 20px 40px; border-radius: 8px; margin: 20px 0;">
                            %s
                        </ul>
                        
                        <p style="color: #34495E; font-size: 16px; line-height: 1.6;">
                            Please login to your account to manage these tasks.
                        </p>
                        
                        <p style="color: #34495E; font-size: 16px; line-height: 1.6; margin-top: 30px;">
                            Best regards,<br>
                            To-Do List App Team
                        </p>
                        
                        <div style="border-top: 1px solid #BDC3C7; margin-top: 30px; padding-top: 20px; 
                                    text-align: center;">
                            <p style="color: #7F8C8D; font-size: 12px; margin-bottom: 15px;">
                                This email was sent to %s
                            </p>
                            <a href="https://github.com/MarcusMQF/FOP-ToDoList-App" 
                               style="display: inline-block; text-decoration: none;">
                                <img src="data:image/png;base64,%s" 
                                     alt="GitHub" 
                                     style="width: 24px; height: 24px;">
                            </a>
                        </div>
                    </div>
                </div>
                """, todoIconBase64, username, formatTaskDetailsHtml(taskDetails), recipient, githubIconBase64);

            sendEmail(recipient, subject, htmlContent);
        } catch (Exception e) {
            System.err.println("Failed to send reminder email: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static String formatTaskDetailsHtml(String taskDetails) {
        return Arrays.stream(taskDetails.split("\n"))
            .map(task -> String.format("<li style=\"margin-bottom: 12px;\">%s</li>", task))
            .collect(Collectors.joining("\n"));
    }
}
