package br.unit.residencia.accenture.Controllers;

import br.unit.residencia.accenture.DTOs.EquipeRequestDTO;
import br.unit.residencia.accenture.DTOs.EquipeResponseDTO;
import br.unit.residencia.accenture.Services.EquipeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/equipes")
@RequiredArgsConstructor
@Tag(name = "Equipes", description = "Gerenciamento de equipes")
public class EquipeController {

    private final EquipeService equipeService;

    /*
    * POST /equipes — Cria uma equipe
    *
    * Recebe uma equipe através de EquipeRequestDTO
    *
    * Retorna a equipe criada através de EquipeResponseDTO
    */
    @Operation(summary = "Criar uma nova equipe")
    @PostMapping
    public ResponseEntity<EquipeResponseDTO> criar(@Valid @RequestBody EquipeRequestDTO dto) {
        EquipeResponseDTO resposta = equipeService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

    /*
     * GET /equipes — Lista todas as equipes
     *
     * Não recebe nada
     *
     * Retorna Lista de Todas as equipes com todos os Membros através do EquipeResponseDTO
     */
    @Operation(summary = "Listar todas as equipes")
    @GetMapping
    public ResponseEntity<List<EquipeResponseDTO>> listarTodas() {
        return ResponseEntity.ok(equipeService.listarTodas());
    }

    /*
     * GET /equipes/{id} — Busca equipe pelo ID
     *
     * Recebe um ID
     *
     * Retorna uma equipe por EquipeResponseDTO
     */
    @Operation(summary = "Buscar equipe por ID")
    @GetMapping("/{id}")
    public ResponseEntity<EquipeResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(equipeService.buscarPorId(id));
    }

    /*
     * PUT /equipes/{id} — Atualiza uma equipe
     *
     * Recebe um ID e um EquipeRequestDTO
     *
     * Retorna uma equipe atualizada por EquipeResponseDTO
     */
    @Operation(summary = "Atualizar equipe (substitui nome, líder e membros)")
    @PutMapping("/{id}")
    public ResponseEntity<EquipeResponseDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody EquipeRequestDTO dto
    ) {
        return ResponseEntity.ok(equipeService.atualizar(id, dto));
    }

    /*
     * POST /equipes/{id}/membros/{idUsuario} — Adiciona um usuário a uma equipe
     *
     * Recebe um ID (equipe) e outro ID (Usuário)
     *
     * Retorna EquipeResponseDTO com o novo usuário inserido
     */
    @Operation(summary = "Adicionar um membro à equipe")
    @PostMapping("/{id}/membros/{idUsuario}")
    public ResponseEntity<EquipeResponseDTO> adicionarMembro(
            @PathVariable Long id,
            @PathVariable Long idUsuario
    ) {
        return ResponseEntity.ok(equipeService.adicionarMembro(id, idUsuario));
    }

    /*
     * DELETE /equipes/{id}/membros/{idUsuario} — Remove um usuário de uma equipe
     *
     * Recebe um ID (equipe) e outro ID (Usuário)
     *
     * Retorna a equipe sem o usuário removido através do EquipeResponseDTO
     */
    @Operation(summary = "Remover um membro da equipe")
    @DeleteMapping("/{id}/membros/{idUsuario}")
    public ResponseEntity<EquipeResponseDTO> removerMembro(
            @PathVariable Long id,
            @PathVariable Long idUsuario
    ) {
        return ResponseEntity.ok(equipeService.removerMembro(id, idUsuario));
    }

    /*
     * DELETE /equipes/{id} — Excluir equipe
     *
     * Recebe um ID
     *
     * Retorna nada (Apenas deleta a equipe)
     */
    @Operation(summary = "Excluir uma equipe")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        equipeService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}