//package com.example.utils;
//
//import com.example.entity.Role;
//import com.example.entity.User;
//import com.example.repo.RoleRepo;
//import com.example.repo.UserRepo;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//
//@Component
//public class DataInitializer implements CommandLineRunner {
//
//    private final RoleRepo roleRepository;
//    private final UserRepo userRepository;
//    private final PasswordEncoder passwordEncoder;
//
//    public DataInitializer(RoleRepo roleRepository,
//                           UserRepo userRepository,
//                           PasswordEncoder passwordEncoder) {
//        this.roleRepository = roleRepository;
//        this.userRepository = userRepository;
//        this.passwordEncoder = passwordEncoder;
//    }
//
//    @Override
//    @Transactional
//    public void run(String... args) throws Exception {
//
//        Role adminRole = roleRepository.findByRoleName("SUPER_ADMIN")
//                .orElseGet(() -> {
//                    Role role = new Role();
//                    role.setRoleName("SUPER_ADMIN");
//                    role.setIsActive(true);
//                    return roleRepository.save(role);
//                });
//
//        if (!userRepository.existsByUsername("super_admin")) {
//            User adminUser = new User();
//            adminUser.setUsername("Mervin J");
//            adminUser.setEmail("admin@erp.company.com");
//
//            adminUser.setPassword(passwordEncoder.encode("MeRvIn@04"));
//
//            adminUser.setRole(adminRole);
//            adminUser.setIsActive(true);
//
//            userRepository.save(adminUser);
//
//            System.out.println(">> ERP System: Default 'Super Admin' user created successfully.");
//        } else {
//            System.out.println(">> ERP System: 'Super Admin' user already exists. Skipping creation.");
//        }
//    }
//}