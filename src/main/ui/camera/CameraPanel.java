package ui.camera;

import ui.main.MainFrame;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.LayerUI;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

public class CameraPanel extends JPanel {

    private static float opacity = 0.3F;

    private int cameraNumber = 0;
    private BufferedImage bufferedImage;
    private VideoCatcher videoCatcher;
    private TitledBorder title;

    private JLayer<JPanel> cameraWindowLayer;
    private JLabel informLabel;
    private boolean fullSize = false;

    public CameraPanel(VideoCreator videoCreator, int cameraNumber) {
        this.setLayout(new BorderLayout());
        this.cameraNumber = cameraNumber;

        informLabel = new JLabel(MainFrame.getBundle().getString("cameradoesnotwork"));
        informLabel.setHorizontalAlignment(SwingConstants.CENTER);
        informLabel.setVerticalAlignment(SwingConstants.CENTER);

        CameraWindow cameraWindow = new CameraWindow();
        LayerUI<JPanel> layerUI = new MyLayer();
        cameraWindowLayer = new JLayer<>(cameraWindow, layerUI);
        cameraWindowLayer.setAlignmentX(CENTER_ALIGNMENT);
        cameraWindowLayer.setAlignmentY(CENTER_ALIGNMENT);

        this.add(informLabel);

        title = BorderFactory.createTitledBorder("FPS = 0");
        title.setTitleJustification(TitledBorder.CENTER);
        title.setTitleFont((new Font(null, Font.BOLD, 10)));
        title.setTitleColor(new Color(46, 139, 87));

        this.setBorder(title);
        videoCatcher = new VideoCatcher(this, videoCreator);
        videoCatcher.start();
    }

    public void repaintCameraWindow() {
        videoCatcher.getVideoCreator().setBufferedImageBack(null);
    }

    public void showCopyImage() {
        cameraWindowLayer.repaint();
    }

    public static BufferedImage animateCircle(BufferedImage originalImage, int type) {
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
                g.drawImage(animateCircle(processImage(videoCatcher.getVideoCreator().getBufferedImageBack(), videoCatcher.getCameraPanel().getWidth(), videoCatcher.getCameraPanel().getWidth()), BufferedImage.TYPE_INT_ARGB), 0, 0, null);
                g.dispose();
            }
        }
    }

    private BufferedImage processImage(BufferedImage bi, int maxWidth, int maxHeight) {
        BufferedImage bi2 = null;
        double max;
        int size;
        int ww = maxWidth - bi.getWidth();
        int hh = maxHeight - bi.getHeight();

        if (ww < 0 || hh < 0) {
            if (ww < hh) {
                max = maxWidth;
                size = bi.getWidth();
            } else {
                max = maxHeight;
                size = bi.getHeight();
            }

            if (size > 0 && size > max) {
                double trans = 1.0 / (size / max);
                AffineTransform tr = new AffineTransform();
                tr.scale(trans, trans);
                AffineTransformOp op = new AffineTransformOp(tr, AffineTransformOp.TYPE_BILINEAR);
                Double w = bi.getWidth() * trans;
                Double h = bi.getHeight() * trans;
                bi2 = new BufferedImage(w.intValue(), h.intValue(), bi.getType());
                op.filter(bi, bi2);
            }
        } else {
            return bi;
        }
        return bi2;
    }

    class CameraWindow extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (bufferedImage != null) {
                g.drawImage(bufferedImage, 0, 0, null);
            }
        }
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

    void startShowVideo() {
        this.removeAll();
        this.add(cameraWindowLayer);
        System.out.println("Начинаем показывать видeo " + getCameraNumber());
    }

    void stopShowVideo() {
        this.removeAll();
        this.add(informLabel);
    }

    int getCameraNumber() {
        return cameraNumber;
    }

    public static float getOpacity() {
        return opacity;
    }

    public static void setOpacity(float opacity) {
        CameraPanel.opacity = opacity;
    }

    public boolean isFullSize() {
        return fullSize;
    }

    public void setFullSize(boolean fullSize) {
        this.fullSize = fullSize;
    }
}
