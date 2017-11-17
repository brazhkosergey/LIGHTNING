package ui.camera;


import ui.main.MainFrame;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.LayerUI;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;


/**
 * panel to show images from camera
 */
public class CameraPanel extends JPanel {

    /**
     * opacity of background
     */
    private static float opacity = 0.3F;
    /**
     * camera number
     */
    private int cameraNumber = 0;
    /**
     * image, which will show on panel
     */
    private BufferedImage bufferedImage;
    /**
     * will read bytes from camera
     */
    private VideoCatcher videoCatcher;
    /**
     * will show data about FPS
     */
    private TitledBorder title;
    private JLayer<JPanel> cameraWindowLayer;
    private JLabel cameraDoesNotWorkLabel;
    private boolean fullSizeEnable = false;
    private CameraWindow cameraWindow;

    /**
     * constructor
     *
     * @param videoBytesSaver - creator for this panel
     * @param cameraNumber - number of camera
     */
    public CameraPanel(VideoBytesSaver videoBytesSaver, int cameraNumber) {
        this.setLayout(new BorderLayout());
        this.cameraNumber = cameraNumber;
        cameraDoesNotWorkLabel = new JLabel(MainFrame.getBundle().getString("cameradoesnotwork"));
        cameraDoesNotWorkLabel.setHorizontalAlignment(SwingConstants.CENTER);
        cameraDoesNotWorkLabel.setVerticalAlignment(SwingConstants.CENTER);
        cameraWindow = new CameraWindow();
        LayerUI<JPanel> layerUI = new BackgroundLayer();
        cameraWindowLayer = new JLayer<>(cameraWindow, layerUI);
        cameraWindowLayer.setAlignmentX(CENTER_ALIGNMENT);
        cameraWindowLayer.setAlignmentY(CENTER_ALIGNMENT);

        title = BorderFactory.createTitledBorder("FPS = 0");
        title.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        title.setTitleJustification(TitledBorder.CENTER);
        title.setTitleFont((new Font(null, Font.BOLD, 10)));
        title.setTitleColor(new Color(46, 139, 87));
        this.setBorder(title);
        videoCatcher = new VideoCatcher(this, videoBytesSaver);
        videoCatcher.start();
    }

    /**
     * class panel, for showing images
     */
    class CameraWindow extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (bufferedImage != null) {
                int x = 0;
                int imageWidth = bufferedImage.getWidth();
                int panelWidth = this.getWidth();
                if (panelWidth > imageWidth) {
                    x = (panelWidth - imageWidth) / 2;
                }
                g.drawImage(bufferedImage, x, 0, null);
            }
        }
    }

    /**
     * used for connect background to camera window
     */
    class BackgroundLayer extends LayerUI<JPanel> {
        @Override
        public void paint(Graphics g, JComponent c) {
            super.paint(g, c);
            if (videoCatcher.getVideoBytesSaver().getBackGroundImage() != null) {
                BufferedImage bufferedImage = changeOpacity(processImage(videoCatcher.getVideoBytesSaver().getBackGroundImage(), videoCatcher.getCameraPanel().getWidth(), videoCatcher.getCameraPanel().getHeight()));
                if (bufferedImage != null) {
                    int x = 0;
                    int imageWidth = bufferedImage.getWidth();
                    int panelWidth = videoCatcher.getCameraPanelWindow().getWidth();
                    if (panelWidth > imageWidth) {
                        x = (panelWidth - imageWidth) / 2;
                    }
                    g.drawImage(bufferedImage, x, 0, null);
                }
                g.dispose();
            }
        }
    }

    void startShowVideo() {
        this.removeAll();
        this.add(cameraWindowLayer);
        this.validate();
        this.repaint();
    }

    void stopShowVideo() {
        this.removeAll();
        this.add(cameraDoesNotWorkLabel);
        this.validate();
        this.repaint();
    }

    /**
     * changing image size, to make smaller
     *
     * @param bi        - image to change size
     * @param maxWidth  - max Width
     * @param maxHeight - maxHeight
     * @return - small image
     */
    public static BufferedImage processImage(BufferedImage bi, int maxWidth, int maxHeight) {//CORRECT
        int width;
        int height;

        if (maxWidth / 1.77 > maxHeight) {
            height = maxHeight;
            width = (int) (height * 1.77);
        } else {
            width = maxWidth;
            height = (int) (width / 1.77);
        }

        BufferedImage bi2 = null;
        double max;
        int size;
        int ww = width - bi.getWidth();
        int hh = height - bi.getHeight();

        if (ww < 0 || hh < 0) {
            if (ww < hh) {
                max = width;
                size = bi.getWidth();
            } else {
                max = height;
                size = bi.getHeight();
            }

            if (size > 0 && size > max) {
                double trans = 1.0 / (size / max);
                AffineTransform tr = new AffineTransform();
                tr.scale(trans, trans);
                AffineTransformOp op = new AffineTransformOp(tr, AffineTransformOp.TYPE_BICUBIC);
                Double w = bi.getWidth() * trans;
                Double h = bi.getHeight() * trans;
                bi2 = new BufferedImage(w.intValue(), h.intValue(), bi.getType());
                op.filter(bi, bi2);
            }
        }
        if (bi2 != null) {
            return bi2;
        } else {
            return bi;
        }
    }


    /**
     * change opacity of image
     *
     * @param originalImage - image to change opacity
     * @return - image
     */
    public static BufferedImage changeOpacity(BufferedImage originalImage) {
        BufferedImage resizedImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = resizedImage.createGraphics();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
        g.drawImage(originalImage, 0, 0, originalImage.getWidth(), originalImage.getHeight(), null);
        g.dispose();
        return resizedImage;
    }


    /**
     * used when delete background
     */
    public void repaintCameraWindow() {
        videoCatcher.getVideoBytesSaver().setBackGroundImage(null);
    }

    /**
     * used when change opacity setting
     */
    public void showCopyImage() {
        cameraWindowLayer.repaint();
    }

    CameraWindow getCameraWindow() {
        return cameraWindow;
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

    int getCameraNumber() {
        return cameraNumber;
    }

    public static float getOpacity() {
        return opacity;
    }

    public static void setOpacity(float opacity) {
        CameraPanel.opacity = opacity;
    }

    public boolean isFullSizeEnable() {
        return fullSizeEnable;
    }

    public void setFullSizeEnable(boolean fullSizeEnable) {
        if (fullSizeEnable) {
            this.setBackground(Color.LIGHT_GRAY);
            this.cameraWindow.setBackground(Color.LIGHT_GRAY);
        } else {
            Color color = new Color(238, 238, 238);
            this.setBackground(color);
            this.cameraWindow.setBackground(color);
        }
        this.fullSizeEnable = fullSizeEnable;
    }
}
