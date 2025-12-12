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

    // --- DASHBOARD ---
    @GetMapping("/dashboard/stats")
    public ResponseEntity<DashboardStatsDTO> getDashboardStats() {
        long totalUsuarios = usuarioRepository.count();
        long totalDiarios = diarioRepository.count();
        long totalAtividades = atividadeRepository.count();
        return ResponseEntity.ok(new DashboardStatsDTO(totalUsuarios, totalDiarios, totalAtividades));
    }

    // =======================================================
    // 1. GERENCIAMENTO DE PAIS E ADMINS (Sem Responsável)
    // =======================================================
    @GetMapping("/usuarios")
    public ResponseEntity<List<Usuario>> listarPaisEAdmins() {
        // Filtra apenas quem NÃO tem responsável (ou seja, é Pai ou Admin)
        List<Usuario> lista = usuarioRepository.findAll().stream()
                .filter(u -> u.getResponsavel() == null)
                .collect(Collectors.toList());
        return ResponseEntity.ok(lista);
    }

    @PostMapping("/usuarios")
    public ResponseEntity<?> salvarPaiOuAdmin(@RequestBody Usuario usuario) {
        if (usuario.getId() != null && usuarioRepository.existsById(usuario.getId())) {
            // Edição
            Usuario existente = usuarioRepository.findById(usuario.getId()).get();
            existente.setNome(usuario.getNome());
            existente.setEmail(usuario.getEmail());
            existente.setPerfil(usuario.getPerfil());
            
            // Verifica a senha usando getPassword()
            if (usuario.getPassword() != null && !usuario.getPassword().isEmpty()) {
                existente.setSenha(passwordEncoder.encode(usuario.getPassword()));
            }
            return ResponseEntity.ok(usuarioRepository.save(existente));
        } else {
            // Criação
            if (usuarioRepository.findByEmail(usuario.getEmail()).isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email já existe"));
            }
            if (usuario.getPassword() != null) {
                usuario.setSenha(passwordEncoder.encode(usuario.getPassword()));
            }
            usuario.setDataCadastro(LocalDate.now());
            return ResponseEntity.ok(usuarioRepository.save(usuario));
        }
    }

    // =======================================================
    // 2. GERENCIAMENTO DE ALUNOS (Com Responsável)
    // =======================================================
    @GetMapping("/alunos")
    public ResponseEntity<List<Usuario>> listarAlunos() {
        // Filtra apenas quem TEM responsável (Alunos)
        List<Usuario> lista = usuarioRepository.findAll().stream()
                .filter(u -> u.getResponsavel() != null)
                .collect(Collectors.toList());
        return ResponseEntity.ok(lista);
    }

    @PostMapping("/alunos")
    public ResponseEntity<?> salvarAluno(@RequestBody Usuario aluno) {
        // Validação: Aluno precisa de um Responsável
        if (aluno.getResponsavel() == null || aluno.getResponsavel().getId() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "É obrigatório selecionar um Responsável para o aluno."));
        }

        Usuario responsavel = usuarioRepository.findById(aluno.getResponsavel().getId())
                .orElseThrow(() -> new RuntimeException("Responsável não encontrado"));

        if (aluno.getId() != null && usuarioRepository.existsById(aluno.getId())) {
            // Edição
            Usuario existente = usuarioRepository.findById(aluno.getId()).get();
            existente.setNome(aluno.getNome());
            existente.setDataNascimento(aluno.getDataNascimento());
            existente.setResponsavel(responsavel);
            existente.setAvatarUrl(aluno.getAvatarUrl());
            return ResponseEntity.ok(usuarioRepository.save(existente));
        } else {
            // Criação
            aluno.setResponsavel(responsavel);
            aluno.setPerfil(Perfil.CRIANCA);
            aluno.setDataCadastro(LocalDate.now());
            // Define senha padrão criptografada
            aluno.setSenha(passwordEncoder.encode("123456")); 
            return ResponseEntity.ok(usuarioRepository.save(aluno));
        }
    }

    // Exclusão Genérica (Serve para Pai, Admin ou Aluno)
    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<?> excluirQualquerUsuario(@PathVariable Long id) {
        try {
            usuarioService.deletarUsuarioPeloAdmin(id);
            return ResponseEntity.ok(Map.of("message", "Usuário excluído com sucesso!"));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    // =======================================================
    // 3. GERENCIAMENTO DE ATIVIDADES
    // =======================================================
    @GetMapping("/atividades")
    public ResponseEntity<List<Atividade>> listarAtividades() {
        return ResponseEntity.ok(atividadeRepository.findAll());
    }

    @PostMapping("/atividades")
    public ResponseEntity<?> salvarAtividade(@RequestBody Atividade atividade) {
        if (atividade.getAluno() != null && atividade.getAluno().getId() != null) {
             Usuario aluno = usuarioRepository.findById(atividade.getAluno().getId())
                 .orElseThrow(() -> new RuntimeException("Aluno vinculado não existe"));
             atividade.setAluno(aluno);
        }
        return ResponseEntity.ok(atividadeRepository.save(atividade));
    }

    @DeleteMapping("/atividades/{id}")
    public ResponseEntity<?> excluirAtividade(@PathVariable Long id) {
        if (atividadeRepository.existsById(id)) {
            atividadeRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "Atividade excluída!"));
        }
        return ResponseEntity.status(404).body(Map.of("error", "Atividade não encontrada"));
    }

    // =======================================================
    // 4. GERENCIAMENTO DE DIÁRIOS
    // =======================================================
    @GetMapping("/diarios")
    public ResponseEntity<List<Diario>> listarDiarios() {
        return ResponseEntity.ok(diarioRepository.findAll());
    }

    @PostMapping("/diarios")
    public ResponseEntity<?> salvarDiario(@RequestBody Diario diario) {
        return ResponseEntity.ok(diarioRepository.save(diario));
    }

    @DeleteMapping("/diarios/{id}")
    public ResponseEntity<?> excluirDiario(@PathVariable Long id) {
        if (diarioRepository.existsById(id)) {
            diarioRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "Diário excluído!"));
        }
        return ResponseEntity.status(404).body(Map.of("error", "Diário não encontrado"));
    }
}