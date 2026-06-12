package br.unit.residencia.accenture.Services;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GeminiService {

    private static final String MODELO = "gemini-2.5-flash";

    /*
     * Instrução enviada antes de qualquer prompt para forçar o Gemini a responder com um JSON válido.
     * Impede que o Gemini coloque coisas extras na resposta como markdown, texto fora de lugar, etc.
     * Isso irá facilitar o parsing da resposta.
     */
    public static final String SYSTEM_INSTRUCTION =
            "Você é um sistema especializado em alocação de espaços corporativos. " +
                    "Responda SOMENTE com um objeto JSON válido e completo. " +
                    "Não inclua texto antes nem depois do JSON. " +
                    "Não use blocos de código markdown (``` ou ```json). " +
                    "Não adicione comentários dentro do JSON. " +
                    "Sua resposta inteira deve poder ser lida diretamente por JSON.parse().";

    private final String apiKey;
    private Client client;

    public GeminiService(@Value("${app.google.api-key:}") String apiKey) {
        this.apiKey = apiKey;
    }

    /*
     * Cria o client do Gemini sob demanda (lazy), para que a aplicação suba
     * mesmo sem a chave configurada. O erro só acontece ao usar o serviço.
     */
    private Client client() {
        if (client == null) {
            if (apiKey == null || apiKey.isBlank()) {
                throw new IllegalStateException(
                        "Chave da API do Gemini não configurada. " +
                                "Defina a variável de ambiente GOOGLE_API_KEY.");
            }
            client = Client.builder().apiKey(apiKey).build();
        }
        return client;
    }

    /*
     * Envia o prompt completo ao gemini e aguarda pela sua resposta.
     */
    public GenerateContentResponse gerarSugestao(String prompt) {
        return client().models.generateContent(MODELO, prompt, null);
    }

    /*
     * Transforma o prompt em string.
     */
    public String gerarTexto(String prompt) {
        return gerarSugestao(prompt).text();
    }

    /*
     * Monta o prompt
     */
    public String gerarTextoComSystemPrompt(String prompt) {
        String promptCompleto = SYSTEM_INSTRUCTION + "\n\n" + prompt;
        return gerarTexto(promptCompleto);
    }
}