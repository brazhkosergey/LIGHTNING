package entity;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VideoPlayer extends JPanel {


    public static boolean showVideoPlayer = false;
    public static boolean PLAY;
    static int speed = 100;


    Map<Integer, File> map;
    List<Thread> threadList = new ArrayList<>();

    JButton playButton;
    JLabel label;
    JPanel mainVideoPane;
    JPanel buttonsPane;
    JPanel southPane;
    public static JSlider slider;


    public VideoPlayer(Map<Integer, File> map, String date) {
        this.map = map;
        this.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        this.setPreferredSize(new Dimension(1100, 580));
        this.setLayout(new BorderLayout());
        mainVideoPane = new JPanel(new FlowLayout());
        mainVideoPane.setPreferredSize(new Dimension(800, 540));

        for (int j = 1; j < 5; j++) {
            File file = map.get(j);
            VideoPlayerPanel videoPlayer = new VideoPlayerPanel(file,this);
            threadList.add(videoPlayer.getThread());
            mainVideoPane.add(videoPlayer);
        }
        for(Thread thread:threadList){
            if(thread!=null){
                thread.start();
            }
        }
        this.add(mainVideoPane,BorderLayout.CENTER);

        slider = new JSlider();
        slider.setPreferredSize(new Dimension(1090, 25));
        slider.setMinorTickSpacing(1);
        slider.setValue(1);
        slider.addChangeListener(e ->{
            System.out.println("Значение на слайдере изменилось на: " + slider.getValue());
        });

        buttonsPane = new JPanel(new FlowLayout());
        buttonsPane.setPreferredSize(new Dimension(1090, 35));

        JButton nextImage = new JButton("NEXT");
        JButton slowerButton = new JButton("<<");
        slowerButton.addActionListener((e)->{
            VideoPlayer.speed = (int) (VideoPlayer.speed*2);
        });
        playButton = new JButton("PLAY");
        playButton.addActionListener(actionEvent -> {
            setPLAY(true);
        });

        JButton pauseButton = new JButton("PAUSE");
        JButton stopButton = new JButton("STOP");
        stopButton.addActionListener(actionEvent -> {
            setPLAY(false);
        });
        JButton fasterButton = new JButton(">>");
        fasterButton.addActionListener((e)->{
            VideoPlayer.speed = (int) (VideoPlayer.speed*0.5);
        });
        JButton previousImage = new JButton("PREV");
        label = new JLabel(date);
        buttonsPane.add(label);
        buttonsPane.add(previousImage);
        buttonsPane.add(slowerButton);
        buttonsPane.add(playButton);
        buttonsPane.add(pauseButton);
        buttonsPane.add(stopButton);
        buttonsPane.add(fasterButton);
        buttonsPane.add(nextImage);
        southPane = new JPanel();
        southPane.setLayout(new BoxLayout(southPane,BoxLayout.Y_AXIS));
        southPane.add(slider);
        southPane.add(buttonsPane);

        this.add(southPane,BorderLayout.SOUTH);
    }

    public static boolean isPLAY() {
        return PLAY;
    }

    public static void setPLAY(boolean PLAY) {
        System.out.println("Изменено на - "+PLAY);
        VideoPlayer.PLAY = PLAY;
    }


    public static boolean isShowVideoPlayer() {
        return showVideoPlayer;
    }

    public static void setShowVideoPlayer(boolean showVideoPlayer) {
        VideoPlayer.showVideoPlayer = showVideoPlayer;
    }
}
