package br.unit.residencia.accenture.Controllers;

import br.unit.residencia.accenture.Config.UsuarioLogado;
import br.unit.residencia.accenture.Models.Usuario;
import br.unit.residencia.accenture.Repositorys.UsuarioRepository;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Operation(summary = "Página inicial pública")
    @GetMapping("/")
    public String home() {
        return "/perfil para fazer login com a Microsoft.";
    }

    @Operation(summary = "Perfil do usuário logado")
    @GetMapping("/perfil")
    public String perfil(UsuarioLogado usuario) {
        String nome = usuario.get().getNome();
        String email = usuario.get().getEmail();
        return "Olá, " + nome + "! (" + email + ")";
    }

    @GetMapping("/usuarios")
    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    @GetMapping("/usuarios/{id}")
    public ResponseEntity<Usuario> buscarPorId(@PathVariable Long id) {
        return usuarioRepository.findById(id)
                .map(usuario -> ResponseEntity.ok().body(usuario))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/usuarios/{email}")
    public ResponseEntity<Usuario> buscarPorEmail(@PathVariable String email) {
        return usuarioRepository.findByEmail(email)
                .map(usuario -> ResponseEntity.ok().body(usuario))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}