package entity;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class VideoPlayerPanel extends JPanel {
    BufferedImage bufferedImage = null;
    VideoPlayer videoPlayer;
    JLabel label ;
    boolean videoPlay = false;
    public VideoPlayerPanel(){
        videoPlayer = new VideoPlayer();
        videoPlayer.setPreferredSize(new Dimension(390,260));
        label = new JLabel("Натистіть PLAY");
        this.add(label);
    }

    class VideoPlayer extends JPanel{
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (bufferedImage != null) {
                g.drawImage(bufferedImage, 0, 0, null);
            }
        }
    }

    public void setBufferedImage(BufferedImage bufferedImage){
        this.bufferedImage = bufferedImage;
    }

    public void showVideo(){
        videoPlay = true;
        this.removeAll();
        this.add(videoPlayer);
        this.repaint();
    }

    public void setLabelText(String text){
        videoPlay = false;
        label.setText(text);
        this.removeAll();
        this.add(label);
        this.repaint();
    }

    public boolean isVideoPlay() {
        return videoPlay;
    }

    public void setVideoPlay(boolean videoPlay) {
        this.videoPlay = videoPlay;
    }
}
