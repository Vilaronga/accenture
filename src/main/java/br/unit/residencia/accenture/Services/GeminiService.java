package br.unit.residencia.accenture.Services;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import org.springframework.stereotype.Service;

@Service
public class GeminiService {

    private final Client client = new Client();

    public GenerateContentResponse gerarSugestao(String prompt) {
        return client.models.generateContent("gemini-2.5-flash", prompt, null);
    }

}
