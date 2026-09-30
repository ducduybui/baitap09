package vn.iotstar.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import vn.iotstar.entity.Role;
import vn.iotstar.entity.User;
import vn.iotstar.repository.RoleRepository;
import vn.iotstar.repository.UserRepository;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initData(
            RoleRepository roles,
            UserRepository users,
            PasswordEncoder encoder,
            @Value("${ADMIN_EMAIL:admin@gmail.com}") String adminEmail,
            @Value("${ADMIN_PASSWORD:user}") String adminPassword) {

        return args -> {
            Role userRole = roles.findByNameIgnoreCase("USER")
                    .orElseGet(() -> roles.save(Role.builder().name("USER").build()));

            Role adminRole = roles.findByNameIgnoreCase("ADMIN")
                    .orElseGet(() -> roles.save(Role.builder().name("ADMIN").build()));

            String adminEmailValue = adminEmail.trim().toLowerCase();
            User admin = users.findByEmailIgnoreCase(adminEmailValue).orElse(null);

            if (admin == null) {
                admin = User.builder()
                        .username("admin")
                        .email(adminEmailValue)
                        .password(encoder.encode(adminPassword))
                        .fullName("System Administrator")
                        .images("/images/avatar-default.png")
                        .enabled(true)
                        .role(adminRole)
                        .build();
            } else {
                admin.setEnabled(true);
                admin.setRole(adminRole);
                if (admin.getUsername() == null || admin.getUsername().isBlank()) {
                    admin.setUsername("admin");
                }
                if (!encoder.matches(adminPassword, admin.getPassword())) {
                    admin.setPassword(encoder.encode(adminPassword));
                }
                if (admin.getFullName() == null || admin.getFullName().isBlank()) {
                    admin.setFullName("System Administrator");
                }
                if (admin.getImages() == null || admin.getImages().isBlank()) {
                    admin.setImages("/images/avatar-default.png");
                }
            }
            users.save(admin);

            User demoUser = users.findByEmailIgnoreCase("user@gmail.com").orElse(null);
            if (demoUser == null) {
                demoUser = User.builder()
                        .username("user")
                        .email("user@gmail.com")
                        .password(encoder.encode("123456"))
                        .fullName("Người dùng thử")
                        .images("/images/avatar-default.png")
                        .enabled(true)
                        .role(userRole)
                        .build();
            } else {
                demoUser.setEnabled(true);
                demoUser.setRole(userRole);
                if (demoUser.getUsername() == null || demoUser.getUsername().isBlank()) {
                    demoUser.setUsername("user");
                }
                if (demoUser.getImages() == null || demoUser.getImages().isBlank()) {
                    demoUser.setImages("/images/avatar-default.png");
                }
            }
            users.save(demoUser);

            for (User existing : users.findAll()) {
                if (existing.getUsername() != null && !existing.getUsername().isBlank()) {
                    continue;
                }

                String base = existing.getEmail().split("@")[0]
                        .replaceAll("[^a-zA-Z0-9_]", "");
                if (base.isBlank()) {
                    base = "user" + existing.getId();
                }

                String candidate = base;
                int suffix = 1;
                while (users.existsByUsernameIgnoreCase(candidate)) {
                    candidate = base + suffix++;
                }

                existing.setUsername(candidate);
                existing.setImages("/images/avatar-default.png");
                users.save(existing);
            }
        };
    }
}
