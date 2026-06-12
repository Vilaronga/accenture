package br.unit.residencia.accenture.Controllers;

import br.unit.residencia.accenture.DTOs.ResultadoDeteccaoDTO;
import br.unit.residencia.accenture.Services.AnalisePlantaBaixaService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/salas")
@RequiredArgsConstructor
public class SalaController {

    private final AnalisePlantaBaixaService analiseService;

    // Esse endpoint existe apenas para fazer o teste de upload de plana, o endpoint correto para enviar a planta é /plantas/processar
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResultadoDeteccaoDTO uploadPlanta(@RequestPart("planta") MultipartFile planta) throws Exception {
        return analiseService.analyze(planta);
    }
}