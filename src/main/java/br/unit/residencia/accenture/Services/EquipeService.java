package br.unit.residencia.accenture.Services;

import br.unit.residencia.accenture.DTOs.EquipeRequestDTO;
import br.unit.residencia.accenture.DTOs.EquipeResponseDTO;
import br.unit.residencia.accenture.Models.Equipe;
import br.unit.residencia.accenture.Models.Usuario;
import br.unit.residencia.accenture.Repositories.EquipeRepository;
import br.unit.residencia.accenture.Repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EquipeService {

    private final EquipeRepository equipeRepository;
    private final UsuarioRepository usuarioRepository;

    /*
     * Método para criar equipes
     */
    @Transactional
    public EquipeResponseDTO criar(EquipeRequestDTO dto) {
        Usuario lider = buscarUsuario(dto.idLider());

        List<Usuario> membros = resolverMembros(dto.idsMembros());

        if (membros.stream().noneMatch(m -> m.getIdUsuario().equals(lider.getIdUsuario()))) {
            membros.add(lider);
        }

        Equipe equipe = Equipe.builder()
                .nome(dto.nome())
                .lider(lider)
                .membros(membros)
                .dataCriacao(LocalDateTime.now())
                .build();

        return EquipeResponseDTO.from(equipeRepository.save(equipe));
    }

    /*
     * Método para listar todas as equipes
     */
    @Transactional(readOnly = true)
    public List<EquipeResponseDTO> listarTodas() {
        return equipeRepository.findAll()
                .stream()
                .map(EquipeResponseDTO::from)
                .toList();
    }

    /*
     * Método para buscar uma equipe pelo id
     */
    @Transactional(readOnly = true)
    public EquipeResponseDTO buscarPorId(Long id) {
        Equipe equipe = equipeRepository.findByIdComMembros(id)
                .orElseThrow(() -> new IllegalArgumentException("Equipe não encontrada: " + id));
        return EquipeResponseDTO.from(equipe);
    }

    /*
     * Método para atualizar uma equipe
     */
    @Transactional
    public EquipeResponseDTO atualizar(Long id, EquipeRequestDTO dto) {
        Equipe equipe = equipeRepository.findByIdComMembros(id)
                .orElseThrow(() -> new IllegalArgumentException("Equipe não encontrada: " + id));

        Usuario novoLider = buscarUsuario(dto.idLider());
        List<Usuario> novosMembros = resolverMembros(dto.idsMembros());

        if (novosMembros.stream().noneMatch(m -> m.getIdUsuario().equals(novoLider.getIdUsuario()))) {
            novosMembros.add(novoLider);
        }

        equipe.setNome(dto.nome());
        equipe.setLider(novoLider);
        equipe.getMembros().clear();
        equipe.getMembros().addAll(novosMembros);

        return EquipeResponseDTO.from(equipeRepository.save(equipe));
    }

    /*
     * Método para adicionar um membro a uma equipe
     */
    @Transactional
    public EquipeResponseDTO adicionarMembro(Long idEquipe, Long idUsuario) {
        Equipe equipe = equipeRepository.findByIdComMembros(idEquipe)
                .orElseThrow(() -> new IllegalArgumentException("Equipe não encontrada: " + idEquipe));

        Usuario usuario = buscarUsuario(idUsuario);

        boolean jaEMembro = equipe.getMembros().stream()
                .anyMatch(m -> m.getIdUsuario().equals(idUsuario));

        if (jaEMembro) {
            throw new IllegalStateException("Usuário já é membro da equipe.");
        }

        equipe.getMembros().add(usuario);
        return EquipeResponseDTO.from(equipeRepository.save(equipe));
    }

    /*
     * Método para remover um membro de uma equipe
     */
    @Transactional
    public EquipeResponseDTO removerMembro(Long idEquipe, Long idUsuario) {
        Equipe equipe = equipeRepository.findByIdComMembros(idEquipe)
                .orElseThrow(() -> new IllegalArgumentException("Equipe não encontrada: " + idEquipe));

        if (equipe.getLider() != null && equipe.getLider().getIdUsuario().equals(idUsuario)) {
            throw new IllegalStateException("Não é possível remover o líder da equipe. Troque o líder antes.");
        }

        equipe.getMembros().removeIf(m -> m.getIdUsuario().equals(idUsuario));
        return EquipeResponseDTO.from(equipeRepository.save(equipe));
    }

    /*
     * Método para deletar uma equipe
     */
    @Transactional
    public void excluir(Long id) {
        if (!equipeRepository.existsById(id)) {
            throw new IllegalArgumentException("Equipe não encontrada: " + id);
        }
        equipeRepository.deleteById(id);
    }

    /*
     * Método buscar um usuário pelo id
     */
    private Usuario buscarUsuario(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + id));
    }

    /*
     * Método para listar usuários
     */
    private List<Usuario> resolverMembros(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return new ArrayList<>();
        return new ArrayList<>(usuarioRepository.findAllById(ids));
    }
}