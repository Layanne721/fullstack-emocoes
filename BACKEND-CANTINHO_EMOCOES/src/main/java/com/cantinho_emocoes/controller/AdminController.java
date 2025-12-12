package com.cantinho_emocoes.controller;

import com.cantinho_emocoes.dto.AdminUsuarioDTO;
import com.cantinho_emocoes.dto.DashboardStatsDTO;
import com.cantinho_emocoes.model.Atividade;
import com.cantinho_emocoes.model.Diario;
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
    
    // Repositórios para contagem de estatísticas e manipulação direta
    private final UsuarioRepository usuarioRepository;
    private final DiarioRepository diarioRepository;
    private final AtividadeRepository atividadeRepository;

    // Construtor
    public AdminController(UsuarioService usuarioService, 
                           UsuarioRepository usuarioRepository,
                           DiarioRepository diarioRepository,
                           AtividadeRepository atividadeRepository) {
        this.usuarioService = usuarioService;
        this.usuarioRepository = usuarioRepository;
        this.diarioRepository = diarioRepository;
        this.atividadeRepository = atividadeRepository;
    }

    // =======================================================
    // DASHBOARD & ESTATÍSTICAS
    // =======================================================

    @GetMapping("/dashboard/stats")
    public ResponseEntity<DashboardStatsDTO> getDashboardStats() {
        long totalUsuarios = usuarioRepository.count();
        long totalDiarios = diarioRepository.count();
        long totalAtividades = atividadeRepository.count();

        DashboardStatsDTO stats = new DashboardStatsDTO(totalUsuarios, totalDiarios, totalAtividades);
        return ResponseEntity.ok(stats);
    }

    // =======================================================
    // GERENCIAMENTO DE USUÁRIOS
    // =======================================================

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

    // =======================================================
    // GERENCIAMENTO DE DIÁRIOS (Novo)
    // =======================================================

    @GetMapping("/diarios")
    public ResponseEntity<List<Diario>> listarTodosDiarios() {
        // Retorna todos os diários encontrados no banco
        return ResponseEntity.ok(diarioRepository.findAll());
    }

    @DeleteMapping("/diarios/{id}")
    public ResponseEntity<?> excluirDiario(@PathVariable Long id) {
        if (diarioRepository.existsById(id)) {
            diarioRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "Diário excluído com sucesso!"));
        }
        return ResponseEntity.status(404).body(Map.of("error", "Diário não encontrado"));
    }

    // =======================================================
    // GERENCIAMENTO DE ATIVIDADES (Novo)
    // =======================================================

    @GetMapping("/atividades")
    public ResponseEntity<List<Atividade>> listarTodasAtividades() {
        return ResponseEntity.ok(atividadeRepository.findAll());
    }

    @DeleteMapping("/atividades/{id}")
    public ResponseEntity<?> excluirAtividade(@PathVariable Long id) {
        if (atividadeRepository.existsById(id)) {
            atividadeRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "Atividade excluída com sucesso!"));
        }
        return ResponseEntity.status(404).body(Map.of("error", "Atividade não encontrada"));
    }
}