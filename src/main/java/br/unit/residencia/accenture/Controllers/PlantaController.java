package br.unit.residencia.accenture.Controllers;

import br.unit.residencia.accenture.Services.PersistenciaPlantaService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/plantas")
public class PlantaController {

    private final PersistenciaPlantaService persistenciaService;

    public PlantaController(PersistenciaPlantaService persistenciaService) {
        this.persistenciaService = persistenciaService;
    }

    /*
    * POST /plantas/processar — Realiza a leitura de uma planta enviada e salva no banco.
    *
    * Recebe uma string referente ao nome da sala e um file (planta da sala).
    *
    * Retorna uma string avisando se a planta foi salva ou se houve erro.
    */
    @PostMapping(value = "/processar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> processarPlanta(@RequestParam("nomeSala") String nomeSala, @RequestParam("file") MultipartFile file) {
        try {
            persistenciaService.processarESalvar(nomeSala, file);
            return ResponseEntity.ok("Planta '" + nomeSala + "' processada e salva com sucesso.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erro ao processar planta: " + e.getMessage());
        }
    }
}