package com.cantinho_emocoes.controller;

import com.cantinho_emocoes.dto.DashboardStatsDTO;
import com.cantinho_emocoes.model.*;
import com.cantinho_emocoes.repository.*;
import com.cantinho_emocoes.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UsuarioRepository usuarioRepository;
    private final DiarioRepository diarioRepository;
    private final AtividadeRepository atividadeRepository;
    private final UsuarioService usuarioService;
    private final PasswordEncoder passwordEncoder;

    public AdminController(UsuarioRepository usuarioRepository, 
                           DiarioRepository diarioRepository,
                           AtividadeRepository atividadeRepository,
                           UsuarioService usuarioService,
                           PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.diarioRepository = diarioRepository;
        this.atividadeRepository = atividadeRepository;
        this.usuarioService = usuarioService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/dashboard/stats")
    public ResponseEntity<DashboardStatsDTO> getDashboardStats() {
        return ResponseEntity.ok(new DashboardStatsDTO(
            usuarioRepository.count(),
            diarioRepository.count(),
            atividadeRepository.count()
        ));
    }

    // --- DTOs DE LEITURA ---
    public record UsuarioSimplesDTO(Long id, String nome, String email, String perfil, String responsavelNome) {}
    public record AtividadeDTO(Long id, String tipo, String conteudo, LocalDateTime dataRealizacao, Long alunoId, String alunoNome) {}
    public record DiarioDTO(Long id, String emocao, int intensidade, String relato, LocalDateTime dataRegistro, Long alunoId, String alunoNome) {}

    // --- USUÁRIOS ---
    @GetMapping("/usuarios")
    public ResponseEntity<List<UsuarioSimplesDTO>> listarUsuarios() {
        List<UsuarioSimplesDTO> lista = usuarioRepository.findAll().stream()
            .filter(u -> u.getResponsavel() == null)
            .map(u -> new UsuarioSimplesDTO(u.getId(), u.getNome(), u.getEmail(), u.getPerfil().name(), null))
            .collect(Collectors.toList());
        return ResponseEntity.ok(lista);
    }

    public record UsuarioRequest(Long id, String nome, String email, String senha, Perfil perfil) {}

    @PostMapping("/usuarios")
    public ResponseEntity<?> salvarUsuario(@RequestBody UsuarioRequest req) {
        Usuario u;
        if (req.id != null) {
            u = usuarioRepository.findById(req.id).orElseThrow();
            u.setNome(req.nome);
            u.setEmail(req.email);
            u.setPerfil(req.perfil);
        } else {
            if (usuarioRepository.findByEmail(req.email).isPresent()) return ResponseEntity.badRequest().body("Email já existe");
            u = new Usuario();
            u.setNome(req.nome);
            u.setEmail(req.email);
            u.setPerfil(req.perfil);
            u.setDataCadastro(LocalDate.now());
            u.setAvatarUrl("https://ui-avatars.com/api/?name=" + req.nome.replace(" ", "+"));
        }
        if (req.senha != null && !req.senha.isBlank()) {
            u.setSenha(passwordEncoder.encode(req.senha));
        }
        usuarioRepository.save(u);
        return ResponseEntity.ok(Map.of("message", "Salvo com sucesso!"));
    }

    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<?> excluirUsuario(@PathVariable Long id) {
        try {
            usuarioService.deletarUsuarioPeloAdmin(id);
            return ResponseEntity.ok(Map.of("message", "Excluído!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // --- ALUNOS ---
    @GetMapping("/alunos")
    public ResponseEntity<List<UsuarioSimplesDTO>> listarAlunos() {
        List<UsuarioSimplesDTO> lista = usuarioRepository.findAll().stream()
            .filter(u -> u.getResponsavel() != null)
            .map(u -> new UsuarioSimplesDTO(u.getId(), u.getNome(), null, "CRIANCA", u.getResponsavel().getNome()))
            .collect(Collectors.toList());
        return ResponseEntity.ok(lista);
    }

    // --- ATIVIDADES (CORRIGIDO COM DTO) ---
    
    // Novo Record para receber os dados corretamente
    public record AtividadeRequest(Long id, String tipo, String conteudo, LocalDateTime dataRealizacao, Usuario aluno) {}

    @GetMapping("/atividades")
    public ResponseEntity<List<AtividadeDTO>> listarAtividades() {
        List<AtividadeDTO> lista = atividadeRepository.findAll().stream()
            .map(a -> new AtividadeDTO(
                a.getId(), 
                a.getTipo(), 
                a.getConteudo(),
                a.getDataRealizacao(),
                a.getAluno() != null ? a.getAluno().getId() : null,
                a.getAluno() != null ? a.getAluno().getNome() : "Geral"))
            .collect(Collectors.toList());
        return ResponseEntity.ok(lista);
    }

    @PostMapping("/atividades")
    public ResponseEntity<?> salvarAtividade(@RequestBody AtividadeRequest req) {
        Atividade atividade;

        // Edição ou Criação
        if (req.id != null) {
            atividade = atividadeRepository.findById(req.id).orElse(new Atividade());
        } else {
            atividade = new Atividade();
        }

        // Atualiza campos
        atividade.setTipo(req.tipo);
        atividade.setConteudo(req.conteudo);
        
        // Garante a data
        if (req.dataRealizacao != null) {
            atividade.setDataRealizacao(req.dataRealizacao);
        } else if (atividade.getDataRealizacao() == null) {
            atividade.setDataRealizacao(LocalDateTime.now());
        }

        // Vincula o Aluno (Correção do erro 500)
        if (req.aluno != null && req.aluno.getId() != null) {
            Usuario aluno = usuarioRepository.findById(req.aluno.getId()).orElse(null);
            if (aluno == null) return ResponseEntity.badRequest().body("Aluno não encontrado");
            atividade.setAluno(aluno);
        } else if (atividade.getAluno() == null) {
             return ResponseEntity.badRequest().body("É obrigatório informar um aluno.");
        }

        atividadeRepository.save(atividade);
        return ResponseEntity.ok(Map.of("message", "Atividade salva!"));
    }

    @DeleteMapping("/atividades/{id}")
    public ResponseEntity<?> excluirAtividade(@PathVariable Long id) {
        atividadeRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Excluída!"));
    }

    // --- DIÁRIOS ---
    @GetMapping("/diarios")
    public ResponseEntity<List<DiarioDTO>> listarDiarios() {
        List<DiarioDTO> lista = diarioRepository.findAll().stream()
            .map(d -> new DiarioDTO(d.getId(), d.getEmocao(), d.getIntensidade(), d.getRelato(), 
                d.getDataRegistro(), d.getDependente().getId(), d.getDependente().getNome()))
            .collect(Collectors.toList());
        return ResponseEntity.ok(lista);
    }

    public record DiarioRequest(Long id, String emocao, Integer intensidade, String relato, LocalDateTime dataRegistro) {}

    @PostMapping("/diarios")
    public ResponseEntity<?> salvarDiario(@RequestBody DiarioRequest req) {
        Diario d = diarioRepository.findById(req.id)
            .orElseThrow(() -> new RuntimeException("Diário não encontrado"));
        
        if (req.emocao != null) d.setEmocao(req.emocao);
        if (req.intensidade != null) d.setIntensidade(req.intensidade);
        if (req.relato != null) d.setRelato(req.relato);
        if (req.dataRegistro != null) d.setDataRegistro(req.dataRegistro);

        diarioRepository.save(d);
        return ResponseEntity.ok(Map.of("message", "Diário atualizado!"));
    }

    @DeleteMapping("/diarios/{id}")
    public ResponseEntity<?> excluirDiario(@PathVariable Long id) {
        diarioRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Excluído!"));
    }
}