package it.lotto5;

import it.eng.pilot.PilotSupport;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

public class Lottobotto extends PilotSupport {

    private static final String fileURL = "https://www.igt.it/STORICO_ESTRAZIONI_LOTTO/storico.zip";
    private static final int BUFFER_SIZE = 4096;

    private static final String FILE = "storico.zip";

    private Process tunnel;


    public static void main(String[] args) throws Exception {
        Lottobotto lb = new Lottobotto();
        lb.run();
    }


    private void run() throws Exception {
        try {
            apriTunnel();
            setPropertyFile("storico.properties");
            Integer oraDownload = getKeyInt("oraDownload");
            Integer minutiDownload = getKeyInt("minutiDownload");
            Timer timer = new Timer();
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    try {
                        download();
                        extract();
                        copiaBatch();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                }
            };
            timer.scheduleAtFixedRate(task, pd().ora(oraDownload).minuti(minutiDownload), 86400000); // Esegui ogni giorno
        } finally {
            chiudiTunnel();
        }
    }


    private void extract() throws Exception {
        p.unzip(FILE, "./");
    }

    private void download() throws Exception {
        URL url = new URL(fileURL);
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setRequestProperty("User-Agent",
                "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
        int responseCode = httpConn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {

            // opens input stream from the HTTP connection
            InputStream inputStream = httpConn.getInputStream();

            // opens an output stream to save into file
            FileOutputStream outputStream = new FileOutputStream(FILE);

            int bytesRead = -1;
            byte[] buffer = new byte[BUFFER_SIZE];
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.close();
            inputStream.close();
            System.out.println("File downloaded");
        } else {
            System.out.println("No file to download. Server replied HTTP code: " + responseCode);
        }
        httpConn.disconnect();
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

    private void copiaBatch() throws Exception {
        //runas /profile /user:Administrator ;
        String c2 = "cp2.bat";
        Process p = Runtime.getRuntime().exec(c2);
        Integer exitCode = p.waitFor();
        System.out.println("EXIT CODE..." + exitCode + "... STORICO.TXT  copiato: " + sn(zero(exitCode)));
    }
}
