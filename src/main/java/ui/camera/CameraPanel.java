package ui.camera;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.image.BufferedImage;

public class CameraPanel extends JPanel {

    private int cameraNumber = 0;
    private BufferedImage bufferedImage;
    private VideoCatcher videoCatcher;
    private CameraWindow cameraWindow;
    private TitledBorder title;

    public CameraPanel(VideoCreator videoCreator) {
        setPreferredSize(new Dimension(260,230));
        cameraWindow = new CameraWindow();
        this.setLayout(new FlowLayout());
        this.add(cameraWindow);
        title = BorderFactory.createTitledBorder("FPS = 0");
        title.setTitleJustification(TitledBorder.CENTER);
        title.setTitleFont((new Font("Comic Sans MS", Font.BOLD, 10)));
        title.setTitleColor(new Color(46, 139, 87));
        title.setBorder(new LineBorder(new Color(46, 139, 87), 1, true));
        this.setBorder(title);
//        this.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        videoCatcher = new VideoCatcher(this,videoCreator);
        Thread thread = new Thread(videoCatcher);
        thread.start();
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

    public void setWindowSize(int width, int height){
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
}
