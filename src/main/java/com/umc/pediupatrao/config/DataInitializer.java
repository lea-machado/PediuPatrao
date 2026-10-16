package com.umc.pediupatrao.config;

import com.umc.pediupatrao.entity.Usuario;
import com.umc.pediupatrao.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(UsuarioRepository repo, PasswordEncoder encoder,
            @Value("${admin_username:}") String username,
            @Value("${admin_password:}") String password) {
        return args -> {
            if (username.isBlank() || password.isBlank()) {
                return;
            }
            if (repo.findByUsername(username).isEmpty()) {
                Usuario user = new Usuario();
                user.setUsername(username);
                user.setPassword(encoder.encode(password));
                user.setRole("admin");
                repo.save(user);
                System.out.println("Usuário admin criado!");
            }
        };
    }
}