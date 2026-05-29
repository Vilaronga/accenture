package br.unit.residencia.accenture.Services;

import br.unit.residencia.accenture.DTOs.ObjetoDetectadoDTO;
import br.unit.residencia.accenture.DTOs.ResultadoDeteccaoDTO;
import br.unit.residencia.accenture.Models.*;
import br.unit.residencia.accenture.Repositories.SalaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class PersistenciaPlantaService {

    private final String UPLOAD_DIR = "uploads/plantas/";
    private final AnalisePlantaBaixaService analiseService;
    private final SalaRepository salaRepository;

    public PersistenciaPlantaService(AnalisePlantaBaixaService analiseService, SalaRepository salaRepository) {
        this.analiseService = analiseService;
        this.salaRepository = salaRepository;
        new File(UPLOAD_DIR).mkdirs(); // Garante que a pasta exista
    }

    @Transactional
    public void processarESalvar(String nomeSala, MultipartFile file) throws Exception {

        // salva o arquivo com o nome da sala
        String fileName = nomeSala + "_" + System.currentTimeMillis() + ".png";
        Path path = Paths.get(UPLOAD_DIR + fileName);
        Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

        // Verifica se já existe uma sala com mesmo nome
        Sala sala = salaRepository.findByNomeSala(nomeSala)
                .orElse(Sala.builder().nomeSala(nomeSala).build());

        sala.setCaminhoPlanta(path.toString()); // atualiza o caminho da sala já que o nome vai mudar

        // Limpa os recursos e cadeiras caso esteja reprocessando a mesma planta
        sala.getLocaisDeTrabalho().clear();
        sala.getRecursos().clear();

        // Faz a análise da planta
        ResultadoDeteccaoDTO resultado = analiseService.analyze(file);

        // Mapeia os resultados
        for (ObjetoDetectadoDTO dto : resultado.objetos()) {
            switch (dto.type()) {
                case "CADEIRA_GERAL", "CADEIRA_PC" -> {
                    LocalDeTrabalho local = LocalDeTrabalho.builder()
                            .sala(sala)
                            .tipoCadeira(dto.type().equals("CADEIRA_PC") ? TipoCadeira.PC : TipoCadeira.GERAL)
                            .posX(dto.x())
                            .posY(dto.y())
                            .build();
                    sala.adicionarLocal(local);
                }
                case "TV", "IMPRESSORA", "PAINELLED" -> {
                    Recurso recurso = Recurso.builder()
                            .sala(sala)
                            .tipoRecurso(TipoRecurso.valueOf(dto.type()))
                            .posX(dto.x())
                            .posY(dto.y())
                            .build();
                    sala.adicionarRecurso(recurso);
                }
            }
        }
        salaRepository.save(sala);
    }
}