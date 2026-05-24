package br.unit.residencia.accenture.Models;

public enum TipoRecurso {
    IMPRESSORA,
    TV,
    PAINELLED,
    PROJETOR;

    public static TipoRecurso fromString(String text) {
        if (text == null) return null;

        String formatted = text.trim().replace(" ", "_").toUpperCase();

        for (TipoRecurso b : TipoRecurso.values()) {
            if (b.name().equals(formatted)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Tipo de recurso não reconhecido: " + text);
    }
}
