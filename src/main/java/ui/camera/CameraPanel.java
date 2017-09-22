package ui.camera;

import ui.main.MainFrame;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.LayerUI;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;


public class CameraPanel extends JPanel {
    private static float opacity = 0.3F;
    private int width;
    private int height;

    private int cameraNumber = 0;
    private BufferedImage bufferedImage;
    private VideoCatcher videoCatcher;
    private CameraWindow cameraWindow;
    private TitledBorder title;

    private JLayer<JPanel> cameraWindowLayer;
    private JLabel label;
    public CameraPanel(VideoCreator videoCreator) {
        label = new JLabel("Камера не працюе");
        label.setHorizontalTextPosition(SwingConstants.RIGHT);

        setPreferredSize(new Dimension(260,230));
        cameraWindow = new CameraWindow();
        JPanel cameraWindowPane = new JPanel();
        cameraWindowPane.add(cameraWindow);
        LayerUI<JPanel> layerUI = new MyLayer();
        cameraWindowLayer = new JLayer<>(cameraWindowPane, layerUI);

        this.setLayout(new BorderLayout());
        this.add(label);

        title = BorderFactory.createTitledBorder("FPS = 0");
        title.setTitleJustification(TitledBorder.CENTER);
        title.setTitleFont((new Font("Comic Sans MS", Font.BOLD, 10)));
        title.setTitleColor(new Color(46, 139, 87));
        title.setBorder(new LineBorder(new Color(46, 139, 87), 1, true));
        this.setBorder(title);

        videoCatcher = new VideoCatcher(this,videoCreator);
        Thread thread = new Thread(videoCatcher);
        thread.start();
    }

    public static BufferedImage animateCircle(BufferedImage originalImage, int type){
        BufferedImage resizedImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), type);
        Graphics2D g = resizedImage.createGraphics();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
        g.drawImage(originalImage, 0, 0, originalImage.getWidth(), originalImage.getHeight(), null);
        g.dispose();
        return resizedImage;
    }

    class MyLayer extends LayerUI<JPanel> {
        @Override
        public void paint(Graphics g, JComponent c) {
            super.paint(g, c);

            if (videoCatcher.getVideoCreator().getBufferedImageBack() != null) {
                g.drawImage(animateCircle(VideoCatcher.processImage(videoCatcher.getVideoCreator().getBufferedImageBack(),width,height),BufferedImage.TYPE_INT_ARGB), 0, 0, null);
                g.dispose();
            }
        }
    }

    class CameraWindow extends JPanel{
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (bufferedImage != null) {
                g.drawImage(bufferedImage, 0, 0, null);
            }
        }
    }

    void setWindowSize(int width, int height){
        this.width = width;
        this.height = height;
        cameraWindow.setPreferredSize(new Dimension(width,height));
    }

    void setBufferedImage(BufferedImage bufferedImage) {
        this.bufferedImage = bufferedImage;
    }

    public VideoCatcher getVideoCatcher() {
        return videoCatcher;
    }

    TitledBorder getTitle() {
        return title;
    }

    void startShowVideo(){
        this.removeAll();
        this.add(cameraWindowLayer);
    }

    void stopShowVideo(){
        this.removeAll();
        this.add(label);
    }

    int getCameraNumber() {
        return cameraNumber;
    }

    public void setCameraNumber(int cameraNumber) {
        this.cameraNumber = cameraNumber;
    }

    public static float getOpacity() {
        return opacity;
    }

    public static void setOpacity(float opacity) {
        CameraPanel.opacity = opacity;
    }
}
