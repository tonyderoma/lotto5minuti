package it.lotto5.dto;

public enum TipoGiocata {

    AMPIEZZE_BASSE("Ampiezze Basse "), CADENZE("Cadenze "), TIPO_FREQUENZE("Tipo Frequenze "), EXTRA_RANDOM("Extra Random "), AMPIEZZE_TRA("Ampiezze Tra "), FREQUENZE_TRA("Frequenze Tra "), FREQUENZE_PUNTUALI("Frequenze Puntuali "), AMPIEZZE_PUNTUALI("Ampiezze Puntuali "), RESIDUI("Residui "), AMPIEZZE_ALTE("Ampiezze Alte "), AMPIEZZE_RANDOM("Ampiezze Random "), NUMERICO_RANDOM("Numerico Random "), VERTICALI("Verticali            "), MAX_RIT("Max Rit             "),
    ;
    private String tipo;

    private TipoGiocata(String tipo) {
        this.tipo = tipo;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
}
