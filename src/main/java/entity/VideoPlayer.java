package entity;

import ui.main.MainFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VideoPlayer extends JPanel {

    private static boolean showVideoPlayer;
    private static boolean STOP;
    private static boolean PAUSE;
    private static boolean PLAY;
    private static boolean SetPOSITION;
    private static boolean NextIMAGE;
    private static boolean PrewIMAGE;
    private static boolean fullSize;

    private static int nextImagesInt;
    private static int prewImagesInt;
    private static int position;

    private static int countDoNotShowImage;
    private static int speed = 0;

    public static JLabel informLabel = new JLabel("STOP");
    private static JLabel speedLabel;
    static JLabel FPSLabel = new JLabel();

    private static JLabel sliderLabel;
    private static JLabel currentFrameLabel;

    //    private static List<JLabel> list;
    private static List<JPanel> list;
    private static Map<Integer, Boolean> eventPercent;
    private static List<VideoPlayerPanel> videoPlayerPanels;

    private JPanel centralPane;
    private JPanel mainVideoPane;
    private Map<Integer, File> foldersWithTemporaryVideoFiles;

    public VideoPlayer(Map<Integer, File> foldersWithTemporaryVideoFiles, String date, int numberInt) {
        this.foldersWithTemporaryVideoFiles = foldersWithTemporaryVideoFiles;
        this.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        this.setPreferredSize(new Dimension(1100, 535));
        centralPane = new JPanel();
        mainVideoPane = new JPanel();
        GridLayout mainVideoPaneLayout = new GridLayout(2, 2, 3, 3);
        mainVideoPane.setLayout(mainVideoPaneLayout);

        JButton backButton = new JButton("BACK");
        backButton.setFocusable(false);
        backButton.addActionListener((e) -> {
           if(fullSize){
               showFourVideo();
               fullSize = false;
           } else {
               stop();
               MainFrame.getMainFrame().showVideosWindow();
           }
        });

        List<Thread> threadList = new ArrayList<>();

        eventPercent = new HashMap<>();
        videoPlayerPanels = new ArrayList<>();
        int mainPanelNumber = 0;
        long mainFileSize = 0L;
        for (int j = 1; j < 5; j++) {
            File folder = foldersWithTemporaryVideoFiles.get(j);
            if (folder != null) {
                File[] files = folder.listFiles();
                if (files != null) {
                    long l = 0L;
                    for (File file : files) {
                        l += file.length();
                    }
                    if (l > mainFileSize) {
                        mainFileSize = l;
                        mainPanelNumber = j;
                    }
                }
            }

            VideoPlayerPanel videoPlayer = new VideoPlayerPanel(folder, j);
            videoPlayer.setShowVideoNow(true);
            videoPlayer.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (videoPlayer.isBlockHaveVideo()) {
                        if (e.getClickCount() == 2) {
                            if(fullSize){
                                showFourVideo();
                                fullSize = false;
                            } else {
                                setPLAY(false);
                                setSTOP(true);
                                setPAUSE(false);
                                setNextIMAGE(false);
                                setPrewIMAGE(false);
                                informLabel.setText("STOP");
                                for (VideoPlayerPanel videoPlayerPanel : videoPlayerPanels) {
                                    videoPlayerPanel.setMainPanel(false);
                                    videoPlayerPanel.setShowVideoNow(false);
                                }
                                videoPlayer.setWidthAndHeight(770, 425);
                                String name = videoPlayer.getFileName();
                                int first = name.indexOf("[");
                                int second = name.indexOf("]");
                                String substring = name.substring(first + 1, second);
                                String[] split = substring.split(",");
                                eventPercent.clear();
                                for (String aSplit : split) {
                                    System.out.println(aSplit);
                                    boolean contains = aSplit.contains("(");
                                    if (contains) {
                                        String s = aSplit.substring(1, aSplit.length() - 1);
                                        try {
                                            int i1 = Integer.parseInt(s);
                                            eventPercent.put(i1, contains);
                                        } catch (Exception ex) {
                                            ex.printStackTrace();
                                        }
                                    } else {
                                        try {
                                            int i1 = Integer.parseInt(aSplit);
                                            eventPercent.put(i1, contains);
                                        } catch (Exception ex) {
                                            ex.printStackTrace();
                                        }
                                    }
                                }

                                videoPlayer.setMainPanel(true);
                                videoPlayer.setShowVideoNow(true);
                                centralPane.removeAll();
                                centralPane.add(videoPlayer);
                                centralPane.validate();
                                centralPane.repaint();
                                fullSize = true;
                            }
                        }
                    }
                }
            });

            threadList.add(videoPlayer.getShowVideoThread());
            mainVideoPane.add(videoPlayer);
            videoPlayerPanels.add(videoPlayer);
        }

        if (mainPanelNumber != 0) {
            VideoPlayerPanel videoPlayerPanel = videoPlayerPanels.get(mainPanelNumber - 1);
            String name = videoPlayerPanel.getFileName();
            int first = name.indexOf("[");
            int second = name.indexOf("]");
            String substring = name.substring(first + 1, second);
            String[] split = substring.split(",");
            for (String aSplit : split) {

                boolean contains = aSplit.contains("(");
                if (contains) {
                    String s = aSplit.substring(1, aSplit.length() - 1);
                    try {
                        int i1 = Integer.parseInt(s);
                        eventPercent.put(i1, contains);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else {
                    try {
                        int i1 = Integer.parseInt(aSplit);
                        eventPercent.put(i1, contains);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
            videoPlayerPanel.setMainPanel(true);
        }

        for (Thread thread : threadList) {
            if (thread != null) {
                thread.start();
            }
        }

        centralPane.add(mainVideoPane);
        this.add(centralPane, BorderLayout.CENTER);

        JButton nextImage = new JButton("NEXT");
        nextImage.setFocusable(false);
        nextImage.addActionListener((e) -> {
            nextFrame();
        });

        JButton previousImage = new JButton("PREV");
        previousImage.setFocusable(false);
        previousImage.addActionListener((e) -> {
            prewFrame();
        });

        JButton slowerButton = new JButton("<<");
        slowerButton.setFocusable(false);
        slowerButton.addActionListener((e) -> {
            slow();
        });

        JButton fasterButton = new JButton(">>");
        fasterButton.setFocusable(false);
        fasterButton.addActionListener((e) -> {
            fast();
        });

        JButton playButton = new JButton("PLAY");
        playButton.setFocusable(true);
        playButton.addActionListener(actionEvent -> {
            play();
        });

        JButton pauseButton = new JButton("PAUSE");
        pauseButton.setFocusable(false);
        pauseButton.addActionListener((e) -> {
            pause();
        });

        JButton stopButton = new JButton("STOP");
        stopButton.setFocusable(false);
        stopButton.addActionListener(actionEvent -> {
            stop();
        });

        JLabel numberLabel = new JLabel(String.valueOf(numberInt));
        numberLabel.setFont(new Font(null, Font.BOLD, 15));
        numberLabel.setPreferredSize(new Dimension(50,15));
        numberLabel.setHorizontalTextPosition(SwingConstants.CENTER);

        JLabel dateLabel = new JLabel(date);
        dateLabel.setHorizontalTextPosition(SwingConstants.CENTER);
        dateLabel.setFont(new Font(null, Font.BOLD, 15));
        dateLabel.setForeground(new Color(46, 139, 87));


        sliderLabel = new JLabel("0 %");
        sliderLabel.setPreferredSize(new Dimension(50, 15));
        sliderLabel.setHorizontalAlignment(SwingConstants.CENTER);
        currentFrameLabel = new JLabel();
        currentFrameLabel.setPreferredSize(new Dimension(120, 15));
        currentFrameLabel.setHorizontalTextPosition(SwingConstants.CENTER);
        informLabel = new JLabel("STOP");
        informLabel.setPreferredSize(new Dimension(50, 15));
        informLabel.setHorizontalAlignment(SwingConstants.CENTER);
        speedLabel = new JLabel("SPEED");
        speedLabel.setPreferredSize(new Dimension(50, 15));
        speedLabel.setHorizontalAlignment(SwingConstants.CENTER);
        FPSLabel.setPreferredSize(new Dimension(80, 15));
        FPSLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel buttonsPane = new JPanel(new FlowLayout());
//        buttonsPane.add(backButton);
//        buttonsPane.add(Box.createRigidArea(new Dimension(2,10)));
        buttonsPane.add(numberLabel);
        buttonsPane.add(currentFrameLabel);
        buttonsPane.add(dateLabel);
        buttonsPane.add(informLabel);
        buttonsPane.add(sliderLabel);
        buttonsPane.add(previousImage);
        buttonsPane.add(slowerButton);
        buttonsPane.add(playButton);
        buttonsPane.add(pauseButton);
        buttonsPane.add(stopButton);
        buttonsPane.add(fasterButton);
        buttonsPane.add(nextImage);
        buttonsPane.add(Box.createRigidArea(new Dimension(10, 10)));
        buttonsPane.add(speedLabel);
        buttonsPane.add(FPSLabel);

        JPanel sliderForVideo = new JPanel();
        sliderForVideo.setBorder(BorderFactory.createEtchedBorder());
        GridLayout layout = new GridLayout(1, 1000, 0, 0);
        sliderForVideo.setLayout(layout);
        list = new ArrayList<>();

        for (int i = 1; i < 1000; i++) {
            JPanel panel = new JPanel();
//            panel.setPreferredSize(new Dimension(1,20));
            int finalI = i;
            panel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    setSetPOSITION(true);
                    position = finalI;
                }
            });

            sliderForVideo.add(panel);
            list.add(panel);
        }

        JPanel sliderPanel = new JPanel(new BorderLayout());
        sliderPanel.setPreferredSize(new Dimension(1005,50));
        sliderPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));


        sliderPanel.add(sliderForVideo, BorderLayout.NORTH);//85 - 50
        sliderPanel.add(buttonsPane, BorderLayout.CENTER);

        JPanel southPane = new JPanel(new BorderLayout());
        southPane.add(backButton, BorderLayout.WEST);//85 - 50
        southPane.add(sliderPanel, BorderLayout.CENTER);
        this.add(southPane, BorderLayout.SOUTH);

        playButton.addKeyListener(new KeyAdapter() {//TODO ikjhnbgvfgbhnjmk,l.,kmjnhbg
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == 37) {
                    setPLAY(false);
                    setSTOP(false);
                    setPAUSE(true);
                    setNextIMAGE(false);
                    setPrewIMAGE(true);
                    prewImagesInt++;
                    nextImagesInt = 0;
                    informLabel.setText("-" + prewImagesInt);
                } else if (e.getKeyCode() == 39) {
                    setPLAY(false);
                    setSTOP(false);
                    setPAUSE(true);
                    setNextIMAGE(true);
                    setPrewIMAGE(false);
                    prewImagesInt = 0;
                    nextImagesInt++;
                    informLabel.setText("+" + nextImagesInt);
                }
            }
        });

        setSliderPosition(0);
        Thread stopPlayngWhileRecordingThread = new Thread(() -> {
            while (VideoPlayer.isShowVideoPlayer()) {
                if (MainVideoCreator.isSaveVideo()) {
                    stop();
                } else {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        stopPlayngWhileRecordingThread.start();
    }

    private void showFourVideo(){
        setPLAY(false);
        setSTOP(true);
        setPAUSE(false);
        setNextIMAGE(false);
        setPrewIMAGE(false);
        informLabel.setText("STOP");
        centralPane.removeAll();
        mainVideoPane.removeAll();
        long fileSize = 0;
        int fileNumber = 0;
        for (int i = 0; i < 4; i++) {
            VideoPlayerPanel playerPanel = videoPlayerPanels.get(i);
            playerPanel.setMainPanel(false);
            playerPanel.setShowVideoNow(true);
//                playerPanel.setWidthAndHeight(377, 222);
            playerPanel.setWidthAndHeight(364, 205);
            ;
            mainVideoPane.add(playerPanel);
            File folder = foldersWithTemporaryVideoFiles.get(i + 1);
            if (folder != null) {
                File[] files = folder.listFiles();
                if (files != null) {
                    long l = 0L;
                    for (File file : files) {
                        l += file.length();
                    }
                    if (l > fileSize) {
                        fileSize = l;
                        fileNumber = i;
                    }
                }
            }
        }

        if (fileNumber != 0) {
            VideoPlayerPanel videoPlayerPanel = videoPlayerPanels.get(fileNumber);
            String name = videoPlayerPanel.getFileName();
            int first = name.indexOf("[");
            int second = name.indexOf("]");
            String substring = name.substring(first + 1, second);
            String[] split = substring.split(",");
            eventPercent.clear();
            for (String aSplit : split) {
                System.out.println(aSplit);
                boolean contains = aSplit.contains("(");
                if (contains) {
                    String s = aSplit.substring(1, aSplit.length() - 1);
                    try {
                        int i1 = Integer.parseInt(s);
                        eventPercent.put(i1, contains);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else {
                    try {
                        int i1 = Integer.parseInt(aSplit);
                        eventPercent.put(i1, contains);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
            videoPlayerPanel.setMainPanel(true);
        }
        mainVideoPane.validate();
        centralPane.add(mainVideoPane);
        centralPane.validate();
        centralPane.repaint();
    }

    private void play() {
        setPLAY(true);
        setSTOP(false);
        setPAUSE(false);
        setNextIMAGE(false);
        setPrewIMAGE(false);
        informLabel.setText("PLAY");
        if (speed == 0) {
            speedLabel.setText((countDoNotShowImage * 2) + "X");
        } else {
            double s = (1000 - speed) / 1000;
            speedLabel.setText(s + "X");
        }
    }

    private void stop() {
        setPLAY(false);
        setSTOP(true);
        setPAUSE(false);
        setNextIMAGE(false);
        setPrewIMAGE(false);
        informLabel.setText("STOP");
    }

    private void pause() {
        setPLAY(false);
        setSTOP(false);
        setPAUSE(true);
        setNextIMAGE(false);
        setPrewIMAGE(false);
        informLabel.setText("PAUSE");
    }

    private void fast() {
        if (VideoPlayer.speed == 0) {
            countDoNotShowImage++;
            if (countDoNotShowImage > 9) {
                countDoNotShowImage = 10;
            }
            speedLabel.setText((countDoNotShowImage * 2) + "X");
        } else {
            VideoPlayer.speed = VideoPlayer.speed - 50;
            if (speed < 1) {
                speed = 0;
            }

            double s = (double) (1000 - speed) / 1000;
            speedLabel.setText((int) (s * 100) + "%");
        }

        if (countDoNotShowImage == 0 && speed == 0) {
            speedLabel.setText(100 + "%");
        }
    }

    private void slow() {
        if (countDoNotShowImage == 0) {
            VideoPlayer.speed = VideoPlayer.speed + 50;
            if (speed > 900) {
                speed = 900;
            }
            double s = (double) (1000 - speed) / 1000;
            speedLabel.setText((int) (s * 100) + "%");
        } else {
            countDoNotShowImage--;
            speedLabel.setText((countDoNotShowImage * 2) + "X");
            if (countDoNotShowImage < 1) {
                countDoNotShowImage = 0;
            }
        }
        if (countDoNotShowImage == 0 && speed == 0) {
            speedLabel.setText(100 + "%");
        }
    }

    private void nextFrame() {
        setPLAY(false);
        setSTOP(false);
        setPAUSE(true);
        setNextIMAGE(true);
        setPrewIMAGE(false);
        prewImagesInt = 0;
        nextImagesInt++;
        informLabel.setText("+" + nextImagesInt);
    }

    private void prewFrame() {
        setPLAY(false);
        setSTOP(false);
        setPAUSE(true);
        setNextIMAGE(false);
        setPrewIMAGE(true);
        prewImagesInt++;
        nextImagesInt = 0;
        informLabel.setText("-" + prewImagesInt);
    }

    public static boolean isFullSize() {
        return fullSize;
    }

    public static void setFullSize(boolean fullSize) {
        VideoPlayer.fullSize = fullSize;
    }

    static void setSetPOSITION(boolean setPOSITION) {
        SetPOSITION = setPOSITION;
    }

    static int getPosition() {
        return position;
    }

    static boolean isPLAY() {
        return PLAY;
    }

    static void setPLAY(boolean PLAY) {
        VideoPlayer.PLAY = PLAY;
    }

    static boolean isSTOP() {
        return STOP;
    }

    static boolean isPAUSE() {
        return PAUSE;
    }

    static void setSTOP(boolean STOP) {
        VideoPlayer.STOP = STOP;
    }

    static void setPAUSE(boolean PAUSE) {
        VideoPlayer.PAUSE = PAUSE;
    }

    static void setSliderPosition(int position) {

        if (position > 999) {
            position = 999;
        }

        for (int i = 0; i < position - 1; i++) {
            if (eventPercent.containsKey(i)) {
                if (eventPercent.get(i)) {
                    list.get(i).setBackground(new Color(23, 182, 42));
                } else {
                    list.get(i).setBackground(new Color(197, 99, 39));
                }
            } else {
                list.get(i).setBackground(new Color(4, 2, 133));
            }
        }

        for (int i = position; i < list.size(); i++) {
            if (eventPercent.containsKey(i)) {
                if (eventPercent.get(i)) {
                    list.get(i).setBackground(new Color(24, 227, 42));
                } else {
                    list.get(i).setBackground(new Color(255, 113, 44));
                }

            } else {
//                list.get(i).setForeground(new Color(98, 99, 110));
                list.get(i).setBackground(Color.LIGHT_GRAY);
            }
        }

        int i = position / 10;
        sliderLabel.setText(i + "%");
//        if(position>99){
//            position=99;
//        }
//
//        for (int i = 0; i < position - 1; i++) {
//            if (eventPercent.containsKey(i)) {
//                if (eventPercent.get(i)) {
//                    list.get(i).setForeground(new Color(23, 182, 42));
//                } else {
//                    list.get(i).setForeground(new Color(197, 99, 39));
//                }
//            } else {
//                list.get(i).setForeground(new Color(4, 2, 133));
//            }
//        }
//
//        for (int i = position; i < list.size(); i++) {
//            if (eventPercent.containsKey(i)) {
//                if (eventPercent.get(i)) {
//                    list.get(i).setForeground(new Color(24, 227, 42));
//                } else {
//                    list.get(i).setForeground(new Color(255, 113, 44));
//                }
//
//            } else {
////                list.get(i).setForeground(new Color(98, 99, 110));
//                list.get(i).setForeground(Color.LIGHT_GRAY);
//            }
//        }
//        sliderLabel.setText(position + "%");
    }

    static boolean isSetPOSITION() {
        return SetPOSITION;
    }

    static int getSpeed() {
        return speed;
    }

    static int getCountDoNotShowImage() {
        return countDoNotShowImage;
    }

    public static boolean isShowVideoPlayer() {
        return showVideoPlayer;
    }

    public static void setShowVideoPlayer(boolean showVideoPlayer) {
        VideoPlayer.showVideoPlayer = showVideoPlayer;
    }

    static boolean isPrewIMAGE() {
        return PrewIMAGE;
    }

    private static void setNextIMAGE(boolean nextIMAGE) {
        NextIMAGE = nextIMAGE;
    }

    private static void setPrewIMAGE(boolean prewIMAGE) {
        PrewIMAGE = prewIMAGE;
    }

    static boolean isNextIMAGE() {
        return NextIMAGE;
    }

    static int getNextImagesInt() {
        return nextImagesInt;
    }

    static int getPrewImagesInt() {
        return prewImagesInt;
    }

    static void setCountDoNotShowImageToZero() {
        VideoPlayer.countDoNotShowImage = 0;
    }

    static void setSpeedToZero() {
        VideoPlayer.speed = 0;
    }

    static void setCurrentFrameLabelText(int currentFrameLabelText, int buffSize) {
        currentFrameLabel.setText("Кадр " + currentFrameLabelText + " : " + buffSize);
    }
}
