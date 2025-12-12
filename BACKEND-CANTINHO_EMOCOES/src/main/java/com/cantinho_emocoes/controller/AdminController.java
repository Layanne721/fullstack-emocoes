package com.cantinho_emocoes.controller;

import com.cantinho_emocoes.dto.AdminUsuarioDTO;
import com.cantinho_emocoes.dto.DashboardStatsDTO;
import com.cantinho_emocoes.repository.AtividadeRepository;
import com.cantinho_emocoes.repository.DiarioRepository;
import com.cantinho_emocoes.repository.UsuarioRepository;
import com.cantinho_emocoes.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UsuarioService usuarioService;
    
    // Repositórios para contagem de estatísticas
    private final UsuarioRepository usuarioRepository;
    private final DiarioRepository diarioRepository;
    private final AtividadeRepository atividadeRepository;

    // O BackupService foi removido daqui pois agora existe o BackupController
    public AdminController(UsuarioService usuarioService, 
                           UsuarioRepository usuarioRepository,
                           DiarioRepository diarioRepository,
                           AtividadeRepository atividadeRepository) {
        this.usuarioService = usuarioService;
        this.usuarioRepository = usuarioRepository;
        this.diarioRepository = diarioRepository;
        this.atividadeRepository = atividadeRepository;
    }

    // --- Endpoints de Estatísticas (Dashboard) ---

    @GetMapping("/dashboard/stats")
    public ResponseEntity<DashboardStatsDTO> getDashboardStats() {
        long totalUsuarios = usuarioRepository.count();
        long totalDiarios = diarioRepository.count();
        long totalAtividades = atividadeRepository.count();

        DashboardStatsDTO stats = new DashboardStatsDTO(totalUsuarios, totalDiarios, totalAtividades);
        return ResponseEntity.ok(stats);
    }

    // --- Endpoints de Usuários ---

    @GetMapping("/usuarios")
    public ResponseEntity<List<AdminUsuarioDTO>> listarTodos() {
        List<AdminUsuarioDTO> usuarios = usuarioService.listarTodosUsuarios();
        return ResponseEntity.ok(usuarios);
    }

    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<?> excluirUsuario(@PathVariable Long id) {
        try {
            usuarioService.deletarUsuarioPeloAdmin(id);
            return ResponseEntity.ok(Map.of("message", "Usuário excluído com sucesso!"));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }
}