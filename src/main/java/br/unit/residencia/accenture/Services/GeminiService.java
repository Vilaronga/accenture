package br.unit.residencia.accenture.Services;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GeminiService {

    private static final String MODELO = "gemini-2.5-flash";

    private final String apiKey;

    private Client client;

    public GeminiService(@Value("${app.google.api-key:}") String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * Cria o client do Gemini sob demanda (lazy), para que a aplicação suba
     * mesmo sem a chave configurada. O erro só acontece ao usar o serviço.
     */
    private Client client() {
        if (client == null) {
            if (apiKey == null || apiKey.isBlank()) {
                throw new IllegalStateException(
                        "Chave da API do Gemini não configurada. Defina a variável de ambiente GOOGLE_API_KEY.");
            }
            client = Client.builder().apiKey(apiKey).build();
        }
        return client;
    }

    public GenerateContentResponse gerarSugestao(String prompt) {
        return client().models.generateContent(MODELO, prompt, null);
    }

    /**
     * Atalho que já devolve o texto da resposta do Gemini.
     */
    public String gerarTexto(String prompt) {
        return gerarSugestao(prompt).text();
    }
}
