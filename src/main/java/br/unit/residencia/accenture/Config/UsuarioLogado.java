package br.unit.residencia.accenture.Config;

import br.unit.residencia.accenture.Models.Usuario;
import br.unit.residencia.accenture.Repositories.UsuarioRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;

/*
* Classe utilitária que serve para obter o usuário logado atualmente.
*
* Não está sendo utilizada, mas colocamos para facilitar a obtenção dos dados do usuário atual
* pensando em, caso realize uma reserva para si, já facilitar o resgate de seus dados.
*/
@Component
public class UsuarioLogado {

    private final UsuarioRepository usuarioRepository;

    public UsuarioLogado(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /* Este método acessa a memória e pega o usuário logado atualmente
    *
    * Através do email da autenticação é resgatado o usuário.
    *
    * Retorna Usuário
    */
    public Usuario get() {
        OidcUser oidcUser = (OidcUser) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        return usuarioRepository.findByEmail(oidcUser.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }
}