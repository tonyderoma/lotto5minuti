package it.lotto5;

import it.eng.pilot.PList;
import it.eng.pilot.PilotSupport;
import org.apache.log4j.BasicConfigurator;

public class CalcoloBilancio extends PilotSupport {
    public static void main(String[] args) {
        BasicConfigurator.configure();
        CalcoloBilancio cb = new CalcoloBilancio();
        cb.run();
    }


    private void run() {
        Integer totale = 0;
        PList<String> report = readFile("REPORT.TXT");
        for (String row : report) {
            Integer bilancio = getInteger(substring(row, "BILANCIO:", false, false, comma(), false, true));
            totale += bilancio;
        }
        log("Bilancio finale=", totale);

    }


}
