package entity;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VideoPlayer extends JPanel {


    public static boolean showVideoPlayer = false;

    public static boolean STOP;
    public static boolean PAUSE;
    public static boolean PLAY;
    public static boolean SetPOSITION;
    private static int position;



    static int speed = 50;

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
        this.setPreferredSize(new Dimension(1100, 550));
        this.setLayout(new BorderLayout());
        mainVideoPane = new JPanel(new FlowLayout());
        mainVideoPane.setPreferredSize(new Dimension(800, 510));
        boolean setMainPane=false;
        for (int j = 1; j < 5; j++) {
            File file = map.get(j);
            VideoPlayerPanel videoPlayer = new VideoPlayerPanel(file,j);
            if(!setMainPane){
                if(file!=null){
                    videoPlayer.setMainPanel(true);
                    setMainPane = true;
                }
            }
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
        slider.setValue(1);
        slider.addChangeListener(e ->{
            System.out.println("Значение на слайдере изменилось на: " + slider.getValue());
//            setSetPOSITION(true);
//            setPLAY(false);
//            position = slider.getValue();
            System.out.println("Установили SETPOSSITION - "+SetPOSITION);
        });

        buttonsPane = new JPanel(new FlowLayout());
        buttonsPane.setPreferredSize(new Dimension(1090, 35));

        JButton nextImage = new JButton("NEXT");
        JButton slowerButton = new JButton("<<");
        slowerButton.addActionListener((e)->{
            VideoPlayer.speed = (int) (VideoPlayer.speed*2);
            if(speed > 1000){
                speed = 1000;
            }
        });
        playButton = new JButton("PLAY");
        playButton.addActionListener(actionEvent -> {
            setPLAY(true);
            setSTOP(false);
            setPAUSE(false);
        });

        JButton pauseButton = new JButton("PAUSE");
        pauseButton.addActionListener((e)->{
            setPLAY(false);
            setSTOP(false);
            setPAUSE(true);
        });
        JButton stopButton = new JButton("STOP");
        stopButton.addActionListener(actionEvent -> {
            setPLAY(false);
            setSTOP(true);
            setPAUSE(false);
        });

        JButton fasterButton = new JButton(">>");
        fasterButton.addActionListener((e)->{
            VideoPlayer.speed = (int) (VideoPlayer.speed*0.5);
            if(speed < 1){
                speed = 1;
            }
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

    public static int getPosition() {
        return position;
    }

    public static boolean isPLAY() {
        return PLAY;
    }

    public static void setPLAY(boolean PLAY) {
        VideoPlayer.PLAY = PLAY;
    }

    public static boolean isSTOP() {
        return STOP;
    }

    public static boolean isPAUSE() {
        return PAUSE;
    }

    public static void setSTOP(boolean STOP) {
        VideoPlayer.STOP = STOP;
    }

    public static void setPAUSE(boolean PAUSE) {
        VideoPlayer.PAUSE = PAUSE;
    }

    public static void setSliderPosition(int position){
//        slider.setOrientation(position);
        slider.setValue(position);
        slider.repaint();
    }

    public static boolean isSetPOSITION() {
        return SetPOSITION;
    }

    public static void setSetPOSITION(boolean setPOSITION) {
        System.out.println("possition " + setPOSITION);
        SetPOSITION = setPOSITION;
    }

    public static int getSpeed() {
        return speed;
    }

    public static boolean isShowVideoPlayer() {
        return showVideoPlayer;
    }

    public static void setShowVideoPlayer(boolean showVideoPlayer) {
        VideoPlayer.showVideoPlayer = showVideoPlayer;
    }
}
