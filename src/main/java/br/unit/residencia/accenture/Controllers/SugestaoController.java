package br.unit.residencia.accenture.Controllers;

import br.unit.residencia.accenture.DTOs.SugestaoEquipeRequestDTO;
import br.unit.residencia.accenture.DTOs.SugestaoEquipeResponseDTO;
import br.unit.residencia.accenture.Services.SugestaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sugestoes")
@RequiredArgsConstructor
public class SugestaoController {

    private final SugestaoService sugestaoService;

    /*
     * POST /sugestoes/equipe — Solicita a sugestão de uma sala para uma equipe
     *
     * Recebe o ID da equipe e data da reserva por SugestaoEquipeRequestDTO
     *
     * Retorna a sugestão gerada pela API do Gemini. Token + sugestão por SugestaoEquipeResponseDTO
     */
    @PostMapping("/equipe")
    public ResponseEntity<SugestaoEquipeResponseDTO> sugerirParaEquipe(@Valid @RequestBody SugestaoEquipeRequestDTO request) {
        SugestaoEquipeResponseDTO resposta = sugestaoService.sugerirParaEquipe(request);
        return ResponseEntity.ok(resposta);
    }

    /*
     * POST /sugestoes/aceitar/{token} — Aceita a sugestão gerada pela I.A
     *
     * Recebe string Token referente à sugestão que está em cache.
     *
     * Retorna string com reserva realizada com sucesso.
     */
    @PostMapping("/aceitar/{token}")
    public ResponseEntity<String> aceitarSugestao(@PathVariable String token) {
        sugestaoService.aceitarSugestao(token);
        return ResponseEntity.ok("Reserva realizada com sucesso baseada na sugestão.");
    }

    /*
     * DELETE /sugestoes/recusar/{token} — Aceita a sugestão gerada pela I.A
     *
     * Recebe string Token referente à sugestão que está em cache.
     *
     * Retorna nada. Apenas exclui o token do cache.
     */
    @DeleteMapping("/recusar/{token}")
    public ResponseEntity<Void> recusarSugestao(@PathVariable String token) {
        sugestaoService.recusarSugestao(token);
        return ResponseEntity.noContent().build();
    }
}