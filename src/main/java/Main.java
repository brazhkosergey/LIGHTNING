import ui.main.MainFrame;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

public class Main {
    public static void main(String[] args) {
        long maxMemory = Runtime.getRuntime().maxMemory() / (1024 * 1024);
        if (maxMemory < 25000) {
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
        MainFrame.setMaxMemory((int) maxMemory);
        MainFrame.getMainFrame();
    }
}
