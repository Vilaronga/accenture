package br.unit.residencia.accenture.Config;

import br.unit.residencia.accenture.Services.OidcUsuarioService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private final OidcUsuarioService oidcUsuarioService;

    /*
    * Faz com que o objeto oidcUsuarioService anteriormente criado seja inicializado apenas quando necessário. -> @Lazy
    */
    public SecurityConfig(@Lazy OidcUsuarioService oidcUsuarioService) {
        this.oidcUsuarioService = oidcUsuarioService;
    }


    /*
    * Método responsável por realizar o controle de segurança.
    * No nosso caso, as páginas /swagger-ui, /v3/api-docs e /salas estão acessíveis para qualquer usuário
    * mesmo que não esteja logado.
    *
    * Caso seja acessada uma página fora dessas, o usuário será redirecionado para login
    * Ao logar, será redirecionado para a página /perfil
    * Ao deslogar´, será redirecionado para a página padrão "/"
    */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/salas/**"
                        ).permitAll()
                        .anyRequest()
                        .authenticated()
                )
                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(userInfo -> userInfo
                                .oidcUserService(oidcUsuarioService)
                        )
                        .defaultSuccessUrl("/perfil", true)
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/")
                );

        return http.build();
    }
}