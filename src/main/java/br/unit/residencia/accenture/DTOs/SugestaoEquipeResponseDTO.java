package br.unit.residencia.accenture.DTOs;

import java.util.List;

/**
 * Resposta da sugestão inteligente de alocação de uma equipe.
 *
 * @param idEquipe              id da equipe analisada
 * @param nomeEquipe            nome da equipe
 * @param totalMembros          quantidade de membros considerados
 * @param membros               membros no formato "Nome (ESPECIALIDADE)"
 * @param salaSugeridaProximidade sala com o agrupamento de cadeiras mais compacto (cálculo determinístico)
 * @param sugestaoInteligente   texto gerado pelo Gemini com a alocação por especialidade e proximidade
 */
public record SugestaoEquipeResponseDTO(
        Long idEquipe,
        String nomeEquipe,
        int totalMembros,
        List<String> membros,
        String salaSugeridaProximidade,
        String sugestaoInteligente
) {
}
