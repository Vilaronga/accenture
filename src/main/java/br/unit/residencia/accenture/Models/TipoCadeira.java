package br.unit.residencia.accenture.Models;

public enum TipoCadeira {
    PC("Cadeira de Estação de Trabalho"),
    GERAL("Cadeira Comum ou de Reunião");

    private final String descricao;

    TipoCadeira(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}