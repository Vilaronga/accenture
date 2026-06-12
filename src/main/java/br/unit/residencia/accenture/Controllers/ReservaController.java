package br.unit.residencia.accenture.Controllers;

import br.unit.residencia.accenture.DTOs.ReservaRequestDTO;
import br.unit.residencia.accenture.DTOs.ReservaResponseDTO;
import br.unit.residencia.accenture.Services.ReservaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reservas")
@RequiredArgsConstructor
@Tag(name = "Reservas", description = "Gerenciamento de reservas de salas")
public class ReservaController {

    private final ReservaService reservaService;

    /*
     * POST /reservas — Cria uma reserva
     *
     * Recebe uma ReservaRequestDTO
     *
     * Retorna todos os detalhes da reserva por ReservaResponseDTO
     */
    @Operation(summary = "Criar uma nova reserva",
            description = """
                       Para tipoReserva = INDIVIDUAL: informar idUsuario.\n
                       Para tipoReserva = EQUIPE: informar idEquipe.\n
                       idLocalDeTrabalho é opcional — informar para reservar uma cadeira específica.\n\n
                       
                       Exemplo:\n
                       {\n
                         "tipoReserva": "EQUIPE",\n
                         "idEquipe": 1,\n
                         "idSala": 2,\n
                         "idLocalDeTrabalho": 10,\n
                         "dataHoraInicio": "2025-07-10T09:00:00",\n
                         "dataHoraFim": "2025-07-10T18:00:00"\n
                       }\n
                       """)
    @PostMapping
    public ResponseEntity<ReservaResponseDTO> criar(@Valid @RequestBody ReservaRequestDTO dto) {
        ReservaResponseDTO resposta = reservaService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }


    /*
     * GET /reservas — Lista todas as reservas
     *
     * Não recebe nada
     *
     * Retorna Lista de Todas as reservas através da ReservaResponseDTO
     */
    @Operation(summary = "Listar todas as reservas")
    @GetMapping
    public ResponseEntity<List<ReservaResponseDTO>> listarTodas() {
        return ResponseEntity.ok(reservaService.listarTodas());
    }


    /*
     * GET /reservas/{id} — Busca reserva por ID
     *
     * Recebe um ID
     *
     * Retorna uam reserva específica por ReservaResponseDTO
     */
    @Operation(summary = "Buscar reserva por ID")
    @GetMapping("/{id}")
    public ResponseEntity<ReservaResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(reservaService.buscarPorId(id));
    }


    /*
     * GET /reservas/usuario/{idUsuario} — Lista reservas de um usuário
     *
     * Recebe um ID
     *
     * Retorna uma lista com todas as reservas as quais o usuário pertence por ReservaResponseDTO
     */
    @Operation(summary = "Listar todas as reservas de um usuário")
    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<ReservaResponseDTO>> listarPorUsuario(@PathVariable Long idUsuario) {
        return ResponseEntity.ok(reservaService.listarPorUsuario(idUsuario));
    }


    /*
     * GET /reservas/equipe/{idEquipe} — Lista reservas de uma equipe
     *
     * Recebe um ID
     *
     * Retorna lista de todas as reservas que aquela equipe possui por EquipeResponseDTO
     */
    @Operation(summary = "Listar todas as reservas de uma equipe")
    @GetMapping("/equipe/{idEquipe}")
    public ResponseEntity<List<ReservaResponseDTO>> listarPorEquipe(@PathVariable Long idEquipe) {
        return ResponseEntity.ok(reservaService.listarPorEquipe(idEquipe));
    }


    /*
     * GET /reservas/sala/{idSala} — Lista reservas de uma sala
     *
     * Recebe um ID
     *
     * Retorna lista de todas as reservas que possuem aquela sala por ReservaResponseDTO
     */
    @Operation(summary = "Listar todas as reservas de uma sala")
    @GetMapping("/sala/{idSala}")
    public ResponseEntity<List<ReservaResponseDTO>> listarPorSala(@PathVariable Long idSala) {
        return ResponseEntity.ok(reservaService.listarPorSala(idSala));
    }


    /*
     * PUT /reservas/{id} — Atualiza uma reserva
     *
     * Recebe um ID
     *
     * Retorna uma reserva atualizada por ReservaResponseDTO
     */
    @Operation(summary = "Atualizar uma reserva existente")
    @PutMapping("/{id}")
    public ResponseEntity<ReservaResponseDTO> atualizar(@PathVariable Long id, @Valid @RequestBody ReservaRequestDTO dto) {
        return ResponseEntity.ok(reservaService.atualizar(id, dto));
    }


    /*
     * DELETE /reservas/{id} — Cancela uma reserva
     *
     * Recebe um ID
     *
     * Retorna nada (Apenas deleta uma reserva)
     */
    @Operation(summary = "Cancelar (excluir) uma reserva")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelar(@PathVariable Long id) {
        reservaService.cancelar(id);
        return ResponseEntity.noContent().build();
    }
}