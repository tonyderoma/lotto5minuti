package it.lotto5;

import it.eng.pilot.PList;
import it.eng.pilot.PilotSupport;

public class CopiaFile extends PilotSupport {
    private Process tunnel;

    public static void main(String[] args) throws Exception {
        CopiaFile lb = new CopiaFile();
        lb.run();
    }


    private void run() throws Exception {
        try {
            apriTunnel();
            setPropertyFile("copiafile.properties");
            PList<String> files = getKeyList("files");
            PList<String> targetPaths = getKeyList("targetPaths");
            if (files.size() != targetPaths.size()) {
                System.out.println("ERRORE:numero di file da copiare diverso dal numero di path target in cui copiarli.");
                return;
            }
            for (int i = 0; i < files.size(); i++)
                copia(files.get(i), targetPaths.get(i));
        } finally {
            chiudiTunnel();
        }
    }


    private void copia(String file, String targetPath) throws Exception {
        String c2 = str("cpFile.bat ", file, space(), targetPath);
        Process p = Runtime.getRuntime().exec(c2);
        Integer exitCode = p.waitFor();
        System.out.println(str("EXIT CODE...", exitCode, tab(), file, arrow(), targetPath, tab(), sn(zero(exitCode))));
    }

    private void apriTunnel() throws Exception {
        String c1 = "cp1.bat";
        tunnel = Runtime.getRuntime().exec(c1);
        attendiSecondi(20);
    }

    private void chiudiTunnel() throws Exception {
        if (notNull(tunnel)) {
            tunnel.destroy();
        }
    }
}
