//package com.example.utils;
//
//import com.example.entity.User;
//import com.example.repo.UserRepo;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Component;
//import java.util.List;
//
//public class PasswordRepairRunner implements CommandLineRunner {
//
//    private final UserRepo userRepo;
//    private final PasswordEncoder passwordEncoder;
//
//    public PasswordRepairRunner(UserRepo userRepo, PasswordEncoder passwordEncoder) {
//        this.userRepo = userRepo;
//        this.passwordEncoder = passwordEncoder;
//    }
//
//    @Override
//    public void run(String... args) throws Exception {
//        System.out.println("--- STARTING DATABASE PASSWORD REPAIR ---");
//
//        List<User> users = userRepo.findAll();
//        for (User user : users) {
//            // Resetting everyone to 'pass123'
//            // We use the encoder directly here to ensure a clean, single hash
//            String cleanHash = passwordEncoder.encode("pass123");
//            user.setPassword(cleanHash);
//            userRepo.save(user);
//
//            System.out.println("Fixed password for: " + user.getUsername());
//        }
//
//        System.out.println("--- REPAIR COMPLETE. Log in with 'pass123' ---");
//        System.out.println("IMPORTANT: Remove the @Component annotation from this class now.");
//    }
//}