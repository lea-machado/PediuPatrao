package com.umc.pediupatrao.controller;

import com.umc.pediupatrao.config.SecurityConfig;
import com.umc.pediupatrao.service.AuditoriaService;
import com.umc.pediupatrao.service.UsuarioDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuditoriaController.class)
@Import(SecurityConfig.class)
class AuditoriaControllerSecurityTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private AuditoriaService auditoriaService;
    @MockitoBean private UsuarioDetailsService usuarioDetailsService;

    @Test
    @WithMockUser(username = "admin01", roles = "admin")
    void administradorPodeConsultarAuditoria() throws Exception {
        mockMvc.perform(get("/api/auditoria")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "gerente01", roles = "gerente")
    void gerentePodeConsultarAuditoria() throws Exception {
        mockMvc.perform(get("/api/auditoria")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "atendente01", roles = "atendente")
    void atendenteNaoPodeConsultarAuditoria() throws Exception {
        mockMvc.perform(get("/api/auditoria")).andExpect(status().isForbidden());
    }
}
