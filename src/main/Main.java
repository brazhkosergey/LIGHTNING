import org.apache.log4j.Logger;
import ui.main.MainFrame;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

public class Main {
    private static Logger log = Logger.getLogger(Main.class);
    public static void main(String[] args) {
        long maxMemory = Runtime.getRuntime().maxMemory() / (1024 * 1024);
        log.info("Выделенная память для приложения - " + maxMemory);
        if (maxMemory < 25000) {
            log.info("Памяти не достаточно, перегружаем приложени, с указанием большего количества памяти.");
            String currentPath = null;
            try {
                currentPath = Main.class
                        .getProtectionDomain()
                        .getCodeSource().getLocation()
                        .toURI().getPath()
                        .replace('/', File.separator.charAt(0)).substring(1);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }

            try {
                Runtime.getRuntime().exec("java -jar -Xms5000m -Xmx29000m " + currentPath + " restart");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        log.info("Памяти достаточно.");
        try {
            MainFrame.getMainFrame();
        } catch (Exception e) {
            log.info(e.getLocalizedMessage());
        } catch (Error error) {
            log.error(error.getLocalizedMessage());
        }

    }
}
