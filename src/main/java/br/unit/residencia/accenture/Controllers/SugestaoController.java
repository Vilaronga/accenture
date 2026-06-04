package br.unit.residencia.accenture.Controllers;

import br.unit.residencia.accenture.DTOs.RequisicaoSugestaoDTO;
import br.unit.residencia.accenture.DTOs.RespostaSugestaoDTO;
import br.unit.residencia.accenture.DTOs.SugestaoEquipeResponseDTO;
import br.unit.residencia.accenture.Services.GeminiService;
import br.unit.residencia.accenture.Services.SugestaoService;
import com.google.genai.types.GenerateContentResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sugestoes")
@RequiredArgsConstructor
public class SugestaoController {

    private final GeminiService cliente;
    private final SugestaoService sugestaoService;

    @Operation(summary = "Teste direto do Gemini com um prompt livre")
    @PostMapping("/teste")
    public ResponseEntity<RespostaSugestaoDTO> testarGemini(@RequestBody RequisicaoSugestaoDTO requisicao) {
        GenerateContentResponse response = cliente.gerarSugestao(requisicao.prompt());
        return ResponseEntity.ok().body(new RespostaSugestaoDTO(response.text()));
    }

    @Operation(summary = "Sugestão inteligente de alocação de cadeiras para uma equipe, "
            + "com base nas especialidades dos membros e na proximidade das estações em cada sala")
    @PostMapping("/equipe/{idEquipe}")
    public ResponseEntity<SugestaoEquipeResponseDTO> sugerirParaEquipe(@PathVariable Long idEquipe) {
        return ResponseEntity.ok(sugestaoService.sugerirParaEquipe(idEquipe));
    }
}
