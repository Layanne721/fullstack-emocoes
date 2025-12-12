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

    @PostMapping("/usuarios")
    public ResponseEntity<?> salvarUsuario(@RequestBody Usuario usuario) {
        if (usuario.getId() != null && usuarioRepository.existsById(usuario.getId())) {
            Usuario existente = usuarioRepository.findById(usuario.getId()).get();
            existente.setNome(usuario.getNome());
            existente.setEmail(usuario.getEmail());
            existente.setPerfil(usuario.getPerfil());
            if (usuario.getPassword() != null && !usuario.getPassword().isEmpty()) {
                existente.setSenha(passwordEncoder.encode(usuario.getPassword()));
            }
            return ResponseEntity.ok(usuarioRepository.save(existente));
        } else {
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

    // --- ALUNOS (DEPENDENTES) ---
    @GetMapping("/alunos")
    public ResponseEntity<List<Usuario>> listarAlunos() {
        // Retorna apenas quem TEM responsável (Alunos)
        List<Usuario> lista = usuarioRepository.findAll().stream()
                .filter(u -> u.getResponsavel() != null)
                .collect(Collectors.toList());
        return ResponseEntity.ok(lista);
    }

    @PostMapping("/alunos")
    public ResponseEntity<?> salvarAluno(@RequestBody Usuario aluno) {
        if (aluno.getResponsavel() == null || aluno.getResponsavel().getId() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Responsável obrigatório"));
        }
        Usuario responsavel = usuarioRepository.findById(aluno.getResponsavel().getId())
                .orElseThrow(() -> new RuntimeException("Responsável não encontrado"));

        if (aluno.getId() != null && usuarioRepository.existsById(aluno.getId())) {
            Usuario existente = usuarioRepository.findById(aluno.getId()).get();
            existente.setNome(aluno.getNome());
            existente.setDataNascimento(aluno.getDataNascimento());
            existente.setResponsavel(responsavel);
            existente.setAvatarUrl(aluno.getAvatarUrl());
            return ResponseEntity.ok(usuarioRepository.save(existente));
        } else {
            aluno.setResponsavel(responsavel);
            aluno.setPerfil(Perfil.CRIANCA);
            aluno.setDataCadastro(LocalDate.now());
            aluno.setSenha(passwordEncoder.encode("123456")); 
            return ResponseEntity.ok(usuarioRepository.save(aluno));
        }
    }

    // --- DELETAR QUALQUER UM ---
    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<?> excluirUsuario(@PathVariable Long id) {
        try {
            usuarioService.deletarUsuarioPeloAdmin(id);
            return ResponseEntity.ok(Map.of("message", "Excluído com sucesso!"));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    // --- ATIVIDADES E DIÁRIOS ---
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
        diarioRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Diário excluído"));
    }
}