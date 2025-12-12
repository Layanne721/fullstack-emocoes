package com.cantinho_emocoes.controller;

import com.cantinho_emocoes.dto.DashboardStatsDTO;
import com.cantinho_emocoes.model.Atividade;
import com.cantinho_emocoes.model.Diario;
import com.cantinho_emocoes.model.Perfil;
import com.cantinho_emocoes.model.Usuario; 
import com.cantinho_emocoes.repository.AtividadeRepository;
import com.cantinho_emocoes.repository.DiarioRepository;
import com.cantinho_emocoes.repository.UsuarioRepository;
import com.cantinho_emocoes.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;
    private final DiarioRepository diarioRepository;
    private final AtividadeRepository atividadeRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminController(UsuarioService usuarioService, 
                           UsuarioRepository usuarioRepository,
                           DiarioRepository diarioRepository,
                           AtividadeRepository atividadeRepository,
                           PasswordEncoder passwordEncoder) {
        this.usuarioService = usuarioService;
        this.usuarioRepository = usuarioRepository;
        this.diarioRepository = diarioRepository;
        this.atividadeRepository = atividadeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/dashboard/stats")
    public ResponseEntity<DashboardStatsDTO> getDashboardStats() {
        long totalUsuarios = usuarioRepository.count();
        long totalDiarios = diarioRepository.count();
        long totalAtividades = atividadeRepository.count();
        return ResponseEntity.ok(new DashboardStatsDTO(totalUsuarios, totalDiarios, totalAtividades));
    }

    // --- USUÁRIOS (PAIS/ADMINS) ---
    @GetMapping("/usuarios")
    public ResponseEntity<List<Usuario>> listarPaisEAdmins() {
        // Retorna apenas quem NÃO tem responsável (Pais e Admins)
        List<Usuario> lista = usuarioRepository.findAll().stream()
                .filter(u -> u.getResponsavel() == null)
                .collect(Collectors.toList());
        return ResponseEntity.ok(lista);
    }

    // DTO Interno para evitar problemas com @JsonIgnore na senha
    public record UsuarioAdminRequest(Long id, String nome, String email, String senha, Perfil perfil) {}

    @PostMapping("/usuarios")
    public ResponseEntity<?> salvarUsuario(@RequestBody UsuarioAdminRequest request) {
        Usuario usuario;

        // Edição
        if (request.id() != null && usuarioRepository.existsById(request.id())) {
            usuario = usuarioRepository.findById(request.id()).get();
            usuario.setNome(request.nome());
            usuario.setEmail(request.email());
            usuario.setPerfil(request.perfil());
        } 
        // Novo Usuário
        else {
            if (usuarioRepository.findByEmail(request.email()).isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email já existe"));
            }
            usuario = new Usuario();
            usuario.setNome(request.nome());
            usuario.setEmail(request.email());
            usuario.setPerfil(request.perfil());
            usuario.setDataCadastro(LocalDate.now());
            // Avatar padrão se não tiver
            usuario.setAvatarUrl("https://ui-avatars.com/api/?name=" + request.nome().replace(" ", "+"));
        }

        // Atualiza a senha SE ela foi enviada no formulário
        if (request.senha() != null && !request.senha().isBlank()) {
            usuario.setSenha(passwordEncoder.encode(request.senha()));
        }

        usuarioRepository.save(usuario);
        return ResponseEntity.ok(Map.of("message", "Usuário salvo com sucesso!"));
    }

    // --- ALUNOS (DEPENDENTES) ---
    @GetMapping("/alunos")
    public ResponseEntity<List<Usuario>> listarAlunos() {
        List<Usuario> lista = usuarioRepository.findAll().stream()
                .filter(u -> u.getResponsavel() != null)
                .collect(Collectors.toList());
        return ResponseEntity.ok(lista);
    }

    // --- DELETAR ---
    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<?> excluirUsuario(@PathVariable Long id) {
        try {
            usuarioService.deletarUsuarioPeloAdmin(id);
            return ResponseEntity.ok(Map.of("message", "Excluído com sucesso!"));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    // --- ATIVIDADES ---
    @GetMapping("/atividades")
    public ResponseEntity<List<Atividade>> listarAtividades() {
        return ResponseEntity.ok(atividadeRepository.findAll());
    }

    @PostMapping("/atividades")
    public ResponseEntity<?> salvarAtividade(@RequestBody Atividade atividade) {
        if (atividade.getAluno() != null && atividade.getAluno().getId() != null) {
             Usuario aluno = usuarioRepository.findById(atividade.getAluno().getId()).orElse(null);
             atividade.setAluno(aluno);
        }
        return ResponseEntity.ok(atividadeRepository.save(atividade));
    }

    @DeleteMapping("/atividades/{id}")
    public ResponseEntity<?> excluirAtividade(@PathVariable Long id) {
        atividadeRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Atividade excluída"));
    }
}