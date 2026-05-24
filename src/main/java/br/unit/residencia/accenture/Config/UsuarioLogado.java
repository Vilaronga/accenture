package br.unit.residencia.accenture.Config;

import br.unit.residencia.accenture.Models.Usuario;
import br.unit.residencia.accenture.Repositories.UsuarioRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;

@Component
public class UsuarioLogado {

    private final UsuarioRepository usuarioRepository;

    public UsuarioLogado(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario get() {
        OidcUser oidcUser = (OidcUser) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        return usuarioRepository.findByEmail(oidcUser.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }
}