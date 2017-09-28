import java.awt.*;
import java.awt.image.*;
import javax.swing.*;
import java.util.Timer;
import java.util.TimerTask;

public class Converge extends JFrame {

    ImageIcon saloonIcon = new ImageIcon("saloon.gif");
    ImageIcon coachIcon = new ImageIcon("oldstage.gif");
    Image saloon = saloonIcon.getImage();
    Image coach = coachIcon.getImage();

    BufferedImage dest;

    float sourcePercentage = 1, destinationPercentage = 0;
    private static int STEPS = 100;
    private static float STEP_CHANGE = 1.0f / STEPS;
    private static int SLEEP_DELAY = 100;

    Insets insets;

    public Converge() {
        super("Image Blending");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        dest = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
        setSize(200, 200);
        TimerTask task = new TimerTask() {
            public void run() {
                repaint();
                sourcePercentage -= STEP_CHANGE;
                destinationPercentage += STEP_CHANGE;
                if (sourcePercentage < 0) {
                    sourcePercentage = 0;
                    destinationPercentage = 1;
                    cancel();
                }
            }
        };
        Timer timer = new Timer();
        timer.schedule(task, 0, SLEEP_DELAY);
    }

    public void paint(Graphics g) {
        if (insets == null) {
            insets = getInsets();
        }
        g.translate(insets.left, insets.top);
        Graphics2D g2d = (Graphics2D) g;
        Graphics2D destG = dest.createGraphics();

        destG.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC,
                sourcePercentage));
        destG.drawImage(coach, 0, 0, this);
        destG.setComposite(AlphaComposite.getInstance(AlphaComposite.XOR,
                destinationPercentage));
        destG.drawImage(saloon, 0, 0, this);
        g2d.drawImage(dest, 0, 0, this);
    }

    public static void main(String args[]) {
        new Converge().show();
    }
}