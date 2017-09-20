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
//    public static int opacity = 30;
    public static float opacity = 0.3F;
    int width;
    int height;

    private int cameraNumber = 0;
    private BufferedImage bufferedImage;
    private BufferedImage bufferedImageBack;
    private VideoCatcher videoCatcher;
    private CameraWindow cameraWindow;
    private TitledBorder title;

    public CameraPanel(VideoCreator videoCreator) {
        setPreferredSize(new Dimension(260,230));
        cameraWindow = new CameraWindow();

        try {
            bufferedImageBack = ImageIO.read(new File("C:\\ipCamera\\auto.jpg"));
            int transparency = bufferedImageBack.getTransparency();
            System.out.println("Прозрачность равна - " + transparency);
        } catch (IOException e) {
            e.printStackTrace();
        }
//
//        JPanel jPanel1 = new JPanel() {
//
//            @Override
//            protected void paintComponent(Graphics g) {
//                super.paintComponent(g);
//                if (bufferedImageBack != null) {
//                    g.drawImage(bufferedImageBack, 0, 0, null);
//                }
//            }
//
////            @Override
////            public void paintComponent( Graphics g ) {
////                super.paintComponent( g );
////
////                // Apply our own painting effect
////                Graphics2D g2d = (Graphics2D) g.create();
////                // 50% transparent Alpha
////                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
////
////                g2d.setColor(getBackground());
////                g2d.fillRect( 0, 0, getWidth(), getHeight() );
////
////                g2d.dispose();
////            }
//        };
//
//        jPanel1.add(new JLabel("TSTSSTa"));
//        jPanel1.setOpaque(true);
//        jPanel1.setBackground(new Color(255, 255, 255, 50));


        JPanel testPane = new JPanel();
        testPane.add(cameraWindow);
        LayerUI<JPanel> layerUI = new MyLayer();
        JLayer<JPanel> layer = new JLayer<JPanel>(testPane, layerUI);



        this.setLayout(new FlowLayout());
        this.add(layer);
//        this.add(cameraWindow);
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



    public BufferedImage animateCircle(BufferedImage originalImage, int type){

        //The opacity exponentially decreases
//        float opacity = 0.3F;

        BufferedImage resizedImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), type);
        Graphics2D g = resizedImage.createGraphics();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
//        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,(float)(opacity/100)));
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

    public int getCameraNumber() {
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
