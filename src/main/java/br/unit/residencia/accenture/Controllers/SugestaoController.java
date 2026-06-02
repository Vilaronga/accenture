package br.unit.residencia.accenture.Controllers;

import br.unit.residencia.accenture.DTOs.RequisicaoSugestaoDTO;
import br.unit.residencia.accenture.DTOs.RespostaSugestaoDTO;
import br.unit.residencia.accenture.Services.GeminiService;
import com.google.genai.types.GenerateContentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sugestoes")
@RequiredArgsConstructor
public class SugestaoController {

    private final GeminiService cliente;

    @PostMapping("/teste")
    public ResponseEntity<RespostaSugestaoDTO> testarGemini(@RequestBody RequisicaoSugestaoDTO requisicao) {
        System.out.println("Prompt fornecido: " + requisicao.prompt() + "\n\n");
        GenerateContentResponse response = cliente.gerarSugestao(requisicao.prompt());
        RespostaSugestaoDTO respostaDTO = new RespostaSugestaoDTO(response.text());
        System.out.println("Resposta do Gemini: " + respostaDTO.resposta());
        return ResponseEntity.ok().body(respostaDTO);
    }
}
