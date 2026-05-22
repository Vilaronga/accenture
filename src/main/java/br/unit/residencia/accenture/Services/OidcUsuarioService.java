package br.unit.residencia.accenture.Services;

import br.unit.residencia.accenture.Models.Perfil;
import br.unit.residencia.accenture.Models.Usuario;
import br.unit.residencia.accenture.Repositorys.UsuarioRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class OidcUsuarioService extends OidcUserService {

    private final UsuarioRepository usuarioRepository;

    public OidcUsuarioService(@Lazy UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) {
        OidcUser oidcUser = super.loadUser(userRequest);

        String email = oidcUser.getEmail();
        String nome = oidcUser.getFullName();
        String microsoftId = oidcUser.getSubject();
        Perfil perfilPadrao = Perfil.Colaborador;
        LocalDate data = LocalDate.now();

        System.out.println("=== Dados do OIDC User ===");
        System.out.println("email: " + email);
        System.out.println("nome: " + nome);
        System.out.println("microsoftID:" + microsoftId);
        System.out.println(("perfil: " + perfilPadrao));
        System.out.println(("data: " + data));

        usuarioRepository.findByEmail(email)
                .orElseGet(() -> usuarioRepository.save(
                        Usuario.builder()
                                .email(email)
                                .nome(nome)
                                .microsoftId(microsoftId)
                                .perfil(perfilPadrao)
                                .dataCriacao(data)
                                .dataAtualizacao(data)
                                .build()
                ));

        return oidcUser;
    }
}