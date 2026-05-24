package br.unit.residencia.accenture.Services;

import br.unit.residencia.accenture.DTOs.ObjetoDetectadoDTO;
import br.unit.residencia.accenture.DTOs.ResultadoDeteccaoDTO;
import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.opencv.opencv_core.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgcodecs.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;

@Service
public class AnalisePlantaBaixaService {

    // Configurações para identificar as semelhanças

    private static final int DUPLICATE_DISTANCE =
            1;

    // "% de semelhança"

    private static final double CADEIRA_GERAL_THRESHOLD =
            0.66;

    private static final double CADEIRA_PC_THRESHOLD =
            0.465;

    private static final double TV_THRESHOLD =
            0.7;

    private static final double IMPRESSORA_THRESHOLD =
            0.65;

    private static final double PAINEL_THRESHOLD =
            0.78;

    // Análise do arquivo recebido

    public ResultadoDeteccaoDTO analyze(
            MultipartFile multipartFile
    ) throws Exception {

        File tempFile =
                createTempFile(multipartFile);

        Mat originalImage = imread(
                tempFile.getAbsolutePath()
        );

        validateImage(originalImage);

        Mat processedImage =
                preprocessImage(originalImage);

        List<ObjetoDetectadoDTO> objects =
                new ArrayList<>();

        // Detecções encontradas

        detectTemplatesFromFolder(
                objects,
                processedImage,
                "src/main/resources/templates/cadeiras_gerais",
                "CADEIRA_GERAL"
        );

        detectTemplatesFromFolder(
                objects,
                processedImage,
                "src/main/resources/templates/cadeiras_pc",
                "CADEIRA_PC"
        );

        detectTemplatesFromFolder(
                objects,
                processedImage,
                "src/main/resources/templates/tvs",
                "TV"
        );

        detectTemplatesFromFolder(
                objects,
                processedImage,
                "src/main/resources/templates/impressoras",
                "IMPRESSORA"
        );

        detectTemplatesFromFolder(
                objects,
                processedImage,
                "src/main/resources/templates/paineis",
                "PAINELLED"
        );

        // Só pra debug - basta descomentar pra testar
        /*
        imwrite(
                "/debugvisual/resultado.png",
                processedImage
        );*/

        printSummary(objects);

        return new ResultadoDeteccaoDTO(
                objects
        );
    }

    // Resumo de tudo encontrado

    private void printSummary(
            List<ObjetoDetectadoDTO> objects
    ) {

        long cadeirasGerais =
                objects.stream()
                        .filter(o ->
                                o.type()
                                        .equals("CADEIRA_GERAL"))
                        .count();

        long cadeirasPc =
                objects.stream()
                        .filter(o ->
                                o.type()
                                        .equals("CADEIRA_PC"))
                        .count();

        long tvs =
                objects.stream()
                        .filter(o ->
                                o.type()
                                        .equals("TV"))
                        .count();

        long impressoras =
                objects.stream()
                        .filter(o ->
                                o.type()
                                        .equals("IMPRESSORA"))
                        .count();

        long paineis =
                objects.stream()
                        .filter(o ->
                                o.type()
                                        .equals("PAINELLED"))
                        .count();

        System.out.println("\n========== RESUMO ==========");
        System.out.println("CADEIRA_GERAL: " + cadeirasGerais);
        System.out.println("CADEIRA_PC: " + cadeirasPc);
        System.out.println("TV: " + tvs);
        System.out.println("IMPRESSORA: " + impressoras);
        System.out.println("PAINELLED: " + paineis);
        System.out.println("TOTAL: " + objects.size());
        System.out.println("============================\n");
    }

    // Planta salva temporariamente

    private File createTempFile(
            MultipartFile multipartFile
    ) throws Exception {

        File tempFile =
                File.createTempFile(
                        "planta",
                        ".png"
                );

        multipartFile.transferTo(tempFile);

        return tempFile;
    }

    // Validação da imagem

    private void validateImage(
            Mat image
    ) {

        if (image.empty()) {

            throw new RuntimeException(
                    "Imagem inválida."
            );
        }
    }

    // Pré-processamento da imagem

    private Mat preprocessImage(
            Mat image
    ) {

        Mat gray =
                new Mat();

        cvtColor(
                image,
                gray,
                COLOR_BGR2GRAY
        );

        GaussianBlur(
                gray,
                gray,
                new Size(3, 3),
                0
        );

        Canny(
                gray,
                gray,
                50,
                150
        );

        return gray;
    }

    // Verifica os templates das pastas

    private void detectTemplatesFromFolder(
            List<ObjetoDetectadoDTO> objects,
            Mat image,
            String folderPath,
            String type
    ) {

        File folder =
                new File(folderPath);

        File[] files =
                folder.listFiles();

        if (files == null) {

            System.out.println(
                    "Pasta não encontrada: "
                            + folderPath
            );

            return;
        }

        for (File file : files) {

            String fileName =
                    file.getName()
                            .toLowerCase();

            if (
                    !fileName.endsWith(".png")
                            &&
                            !fileName.endsWith(".jpg")
                            &&
                            !fileName.endsWith(".jpeg")
            ) {
                continue;
            }

            System.out.println(
                    "Analisando template: "
                            + file.getName()
            );

            detectTemplate(
                    objects,
                    image,
                    file.getAbsolutePath(),
                    type
            );
        }
    }

    // Compara os templates com a imagem

    private void detectTemplate(
            List<ObjetoDetectadoDTO> detected,
            Mat image,
            String templatePath,
            String type
    ) {

        Mat template = imread(
                templatePath,
                IMREAD_GRAYSCALE
        );

        if (template.empty()) {

            System.out.println(
                    "Template inválido: "
                            + templatePath
            );

            return;
        }

        // Canny no template

        Canny(
                template,
                template,
                50,
                150
        );

        // Escalas para verificação

        double[] scales =
                getScalesByType(type);

        for (double scale : scales) {

            Mat resizedTemplate =
                    new Mat();

            resize(
                    template,
                    resizedTemplate,
                    new Size(),
                    scale,
                    scale,
                    INTER_LINEAR
            );

            if (
                    resizedTemplate.cols()
                            >= image.cols()
                            ||
                            resizedTemplate.rows()
                                    >= image.rows()
            ) {
                continue;
            }

            Mat result =
                    new Mat();

            matchTemplate(
                    image,
                    resizedTemplate,
                    result,
                    TM_CCOEFF_NORMED
            );

            while (true) {

                MatchResult matchResult =
                        getBestMatch(result);

                double confidence =
                        matchResult.confidence();

                double threshold =
                        getThresholdByType(type);

                if (confidence < threshold) {
                    break;
                }

                Point point =
                        matchResult.point();

                // Tenta evitar pixels duplos

                if (
                        alreadyDetected(
                                detected,
                                point
                        )
                ) {

                    removeDetectedArea(
                            result,
                            point,
                            resizedTemplate
                    );

                    continue;
                }

                // detecção de objetos

                detected.add(
                        new ObjetoDetectadoDTO(
                                type,
                                (double) point.x(),
                                (double) point.y(),
                                (double) resizedTemplate.cols(),
                                (double) resizedTemplate.rows()
                        )
                );

                System.out.println(
                        "DETECTADO -> "
                                + type
                                + " X="
                                + point.x()
                                + " Y="
                                + point.y()
                                + " SCALE="
                                + scale
                                + " CONF="
                                + confidence
                );

                // debug visual

                rectangle(
                        image,
                        new Rect(
                                point.x(),
                                point.y(),
                                resizedTemplate.cols(),
                                resizedTemplate.rows()
                        ),
                        new Scalar(
                                255,
                                255,
                                255,
                                0
                        ),
                        2,
                        LINE_8,
                        0
                );

                // remove comparação

                removeDetectedArea(
                        result,
                        point,
                        resizedTemplate
                );
            }
        }
    }

    // threshold estabelecido em cada tipo

    private double getThresholdByType(
            String type
    ) {

        return switch (type) {

            case "CADEIRA_PC" ->
                    CADEIRA_PC_THRESHOLD;

            case "TV" ->
                    TV_THRESHOLD;

            case "IMPRESSORA" ->
                    IMPRESSORA_THRESHOLD;

            case "PAINELLED" ->
                    PAINEL_THRESHOLD;

            default ->
                    CADEIRA_GERAL_THRESHOLD;
        };
    }

    // Escalas pra cada tipo

    private double[] getScalesByType(
            String type
    ) {

        return switch (type) {

            case "PAINELLED" -> new double[]{
                    1.0,
                    1.5,
                    2.0,
                    2.5,
                    3.0
            };

            case "TV" -> new double[]{
                    0.7,
                    1.0,
                    1.3,
                    1.5,
                    2.0
            };

            default -> new double[]{
                    0.5,
                    0.75,
                    1.0,
                    1.25,
                    1.5
            };
        };
    }

    // Escola da melhor comparação

    private MatchResult getBestMatch(
            Mat result
    ) {

        DoublePointer minVal =
                new DoublePointer(1);

        DoublePointer maxVal =
                new DoublePointer(1);

        Point minLoc =
                new Point();

        Point maxLoc =
                new Point();

        minMaxLoc(
                result,
                minVal,
                maxVal,
                minLoc,
                maxLoc,
                null
        );

        return new MatchResult(
                maxVal.get(),
                maxLoc
        );
    }

    // Tenta evitar se já foi detectado

    private boolean alreadyDetected(
            List<ObjetoDetectadoDTO> detected,
            Point point
    ) {

        for (
                ObjetoDetectadoDTO obj
                : detected
        ) {

            double distance =
                    Math.sqrt(

                            Math.pow(
                                    obj.x()
                                            - point.x(),
                                    2
                            )

                                    +

                                    Math.pow(
                                            obj.y()
                                                    - point.y(),
                                            2
                                    )
                    );

            if (
                    distance
                            < DUPLICATE_DISTANCE
            ) {
                return true;
            }
        }

        return false;
    }

    // Remover comparação

    private void removeDetectedArea(
            Mat result,
            Point point,
            Mat template
    ) {

        rectangle(
                result,
                new Rect(
                        point.x(),
                        point.y(),
                        template.cols(),
                        template.rows()
                ),
                Scalar.BLACK,
                FILLED,
                LINE_8,
                0
        );
    }

    // Record auxiliar

    private record MatchResult(
            double confidence,
            Point point
    ) {
    }
}