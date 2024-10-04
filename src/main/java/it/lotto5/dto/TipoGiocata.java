package it.lotto5.dto;

public enum TipoGiocata {

    AMPIEZZE_BASSE("Ampiezze Basse"), CADENZE("Cadenze"), TIPO_FREQUENZE("Tipo Frequenze"), EXTRA_RANDOM("Extra Random"), AMPIEZZE_TRA("Ampiezze Tra"), FREQUENZE_TRA("Frequenze Tra"), FREQUENZE_PUNTUALI("Frequenze Puntuali"), AMPIEZZE_PUNTUALI("Ampiezze Puntuali"), RESIDUI("Residui");

    private String tipo;

    private TipoGiocata(String tipo) {
        this.tipo = tipo;
    }
}
