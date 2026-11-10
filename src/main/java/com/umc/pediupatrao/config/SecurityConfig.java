package com.umc.pediupatrao.config;

import com.umc.pediupatrao.service.UsuarioDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.http.HttpMethod;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UsuarioDetailsService usuarioDetailsService;

    public SecurityConfig(UsuarioDetailsService usuarioDetailsService) {
        this.usuarioDetailsService = usuarioDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(usuarioDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/css/**", "/dist/**", "/plugins/**", "/js/**", "/images/**")
                        .permitAll()
                        .requestMatchers("/usuarios", "/usuarios/**", "/api/usuarios", "/api/usuarios/**")
                        .hasRole("admin")
                        .requestMatchers(HttpMethod.GET, "/auditoria", "/api/auditoria").hasAnyRole("admin", "gerente")
                        .requestMatchers("/api/auditoria", "/api/auditoria/**").denyAll()
                        .requestMatchers(HttpMethod.GET, "/funcionarios").hasAnyRole("admin", "gerente", "atendente")
                        .requestMatchers(HttpMethod.GET, "/entregadores").hasAnyRole("admin", "gerente", "atendente")
                        .requestMatchers(HttpMethod.POST, "/entregadores").hasRole("admin")
                        .requestMatchers("/entregadores/**").denyAll()
                        .requestMatchers(HttpMethod.PUT, "/api/pedidos/*/entregador").hasAnyRole("gerente", "atendente")
                        .requestMatchers(HttpMethod.GET, "/pedidos", "/api/pedidos")
                        .hasAnyRole("admin", "gerente", "atendente")
                        .requestMatchers(HttpMethod.POST, "/api/pedidos").hasAnyRole("gerente", "atendente")
                        .requestMatchers(HttpMethod.PUT, "/api/pedidos/*/solicitar-cancelamento").hasRole("atendente")
                        .requestMatchers(HttpMethod.POST, "/api/pedidos/*/aprovar-cancelamento")
                        .hasAnyRole("gerente", "atendente") // o atendente pode enviar a requisição, mas é necessário
                                                            // informar a credencial do gerente
                        .requestMatchers(HttpMethod.PUT, "/api/pedidos/*/solicitar-desconto").hasRole("atendente")
                        .requestMatchers(HttpMethod.POST, "/api/pedidos/*/aprovar-desconto")
                        .hasAnyRole("atendente", "gerente")
                        .requestMatchers(HttpMethod.PUT, "/api/pedidos/*/desconto").denyAll()
                        .requestMatchers(HttpMethod.PUT, "/api/pedidos/*/status").hasRole("gerente")
                        .requestMatchers(HttpMethod.PUT, "/api/pedidos/**").denyAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/pedidos/**").denyAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/clientes/**").hasRole("admin")
                        .requestMatchers(HttpMethod.GET, "/clientes", "/api/clientes", "/api/clientes/**")
                        .hasAnyRole("admin", "gerente", "atendente")
                        .requestMatchers(HttpMethod.GET, "/clientes/novo", "/clientes/editar/**").hasRole("atendente")
                        .requestMatchers(HttpMethod.POST, "/clientes/salvar", "/clientes/editar/**", "/api/clientes")
                        .hasRole("atendente")
                        .requestMatchers(HttpMethod.PUT, "/api/clientes/**").hasRole("atendente")
                        .requestMatchers("/clientes/**", "/api/clientes/**").denyAll()
                        .requestMatchers("/produtos/**", "/produtos", "/api/produtos/**", "/api/produtos")
                        .hasRole("gerente")
                        .requestMatchers("/configuracoes").hasRole("admin")
                        .requestMatchers(HttpMethod.GET, "/admin")
                        .hasAnyRole("admin", "gerente")
                        .requestMatchers(HttpMethod.GET, "/api/dashboard", "/api/dashboard/**")
                        .hasAnyRole("admin", "gerente")
                        .requestMatchers("/api/dashboard/**").denyAll()
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .permitAll()
                        .successHandler(authenticationSuccessHandler()))
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll());

        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return (request, response, authentication) -> {
            response.sendRedirect("/"); // Redireciona para a página inicial após login bem-sucedido
        };
    }
}