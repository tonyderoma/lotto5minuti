package it.lotto5.dto;

public enum TipoGiocata {

    AMPIEZZE_BASSE("Ampiezze Basse"), CADENZE("Cadenze"), TIPO_FREQUENZE("Tipo Frequenze"), EXTRA_RANDOM("Extra Random"), AMPIEZZE_TRA("Ampiezze Tra"), FREQUENZE_TRA("Frequenze Tra"), FREQUENZE_PUNTUALI("Frequenze Puntuali"), AMPIEZZE_PUNTUALI("Ampiezze Puntuali"), RESIDUI("Residui"), AMPIEZZE_ALTE("Ampiezze Alte"), AMPIEZZE_RANDOM("Ampiezze Random"), NUMERICO_RANDOM("Numerico Random "), VERTICALI("Verticali"), MAX_RIT("Max Rit"), CADENZE_ESTRATTE("Cadenze Estratte"), MANUALE("Manuale"), RIDUZIONE("Riduzione"), RIDUZIONE_RITARDI("Riduzione Ritardi"), RIDUZIONE_RITARDI_RESIDUI("Riduzione Ritardi Residui"), CASUALI("Casuali"), RIDUZIONE_STORICO_FREQS("Riduzione Storico Freqs"), COORDSFREQS("Coords Freqs"), CADENZE_MISTE("Cadenze Miste"), RIDUZIONE_PASSATO("Riduzione dal passato"), RIDUZIONE_RESIDUI("Riduzione residui"), OTHERS("Others"), FREQUENZE_ALTE("Frequenze alte"), CODA_CIRCOLARE("Coda circolare"), RIDUZIONE_OTHERS("Riduzione Prec. 3"), RIDUZIONE_OTHERS_RESIDUI("Riduzione Prec. 3 Residui"), RANDOM("Random"), STORICO_FREQUENZE("Storico Frequenze"), FREQUENZE_ESTRATTE("Frequenze estratte"), RIDUZIONE_SPLIT("Riduzione Split"), RIDUZIONE_DECINE("Riduzione Decine"), RIDUZIONE_DECINE_RESIDUI("Riduzione Decine Residui"), RIDUZIONE_INTERCETTATI("Riduzione Intercettati"), SOTTRAI_PRECEDENTI("Sottrai Precedenti"), SOTTRAI_PRECEDENTI_RESIDUI("Sottrai Precedenti Residui"), POSIZIONI_FREQUENZE("Posizioni Frequenze"), POSIZIONI_FREQUENZE_RESIDUI("Posizioni Frequenze Residui"), POSIZIONI_RITARDI("Posizioni Ritardi"), POSIZIONI_RITARDI_RESIDUI("Posizioni Ritardi Residui"), RIDUZIONE_PRECEDENTI("Riduzione Precedenti"), RIDUZIONE_PRECEDENTI_RESIDUI("Riduzione Precedenti Residui"), RIDUZIONE_EXTRA("Riduzione Extra"), RIDUZIONE_EXTRA_RESIDUI("Riduzione Extra Residui"), RIDUZIONE_ROULETTE("Riduzione Roulette"), RIDUZIONE_ROULETTE_RESIDUI("Riduzione Roulette Residui");

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
