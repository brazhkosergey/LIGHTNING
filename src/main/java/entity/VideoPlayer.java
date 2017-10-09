package entity;

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
    private static boolean SaveIMAGE;

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

    private static List<JLabel> list;
    private static Map<Integer, Boolean> eventPercent;
    private static List<VideoPlayerPanel> videoPlayerPanels;

    private static JButton saveImageButton;
    private JPanel centralPane;
    private JPanel mainVideoPane;

    public VideoPlayer(Map<Integer, File> foldersWithTemporaryVideoFiles, String date) {
        this.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        this.setPreferredSize(new Dimension(1100, 550));
        centralPane = new JPanel();
        mainVideoPane = new JPanel();
        GridLayout mainVideoPaneLayout = new GridLayout(2, 2, 5, 5);
        mainVideoPane.setLayout(mainVideoPaneLayout);

        JButton backButton = new JButton("BACK");
        backButton.addActionListener((e) -> {
            setPLAY(false);
            setSTOP(true);
            setPAUSE(false);
            setNextIMAGE(false);
            setPrewIMAGE(false);
            informLabel.setText("STOP");
            saveImageButton.setForeground(Color.LIGHT_GRAY);
            centralPane.removeAll();
            mainVideoPane.removeAll();
            long fileSize = 0;
            int fileNumber = 0;
            for (int i = 0; i < 4; i++) {
                VideoPlayerPanel playerPanel = videoPlayerPanels.get(i);
                playerPanel.setMainPanel(false);
                playerPanel.setShowVideoNow(true);
                playerPanel.setWidthAndHeight(535, 222);
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
            centralPane.add(mainVideoPane);
            centralPane.repaint();
            backButton.setForeground(Color.LIGHT_GRAY);
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
                    if (e.getClickCount() == 2) {
                        setPLAY(false);
                        setSTOP(true);
                        setPAUSE(false);
                        setNextIMAGE(false);
                        setPrewIMAGE(false);
                        informLabel.setText("STOP");
                        saveImageButton.setForeground(Color.LIGHT_GRAY);
                        for (VideoPlayerPanel videoPlayerPanel : videoPlayerPanels) {
                            videoPlayerPanel.setMainPanel(false);
                            videoPlayerPanel.setShowVideoNow(false);
                        }

                        videoPlayer.setWidthAndHeight(1050, 450);
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
                        centralPane.repaint();
                        backButton.setForeground(new Color(23, 114, 26));
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
        JPanel buttonsPane = new JPanel(new FlowLayout());
        buttonsPane.setPreferredSize(new Dimension(1090, 35));
        saveImageButton = new JButton("КАДР");
        saveImageButton.addActionListener((e) -> {
            if (isPAUSE()) {
                SaveIMAGE = true;
                saveImageButton.setForeground(Color.blue);
            }
        });

        JButton nextImage = new JButton("NEXT");
        nextImage.addActionListener((e) -> {
            setPLAY(false);
            setSTOP(false);
            setPAUSE(true);
            setNextIMAGE(true);
            setPrewIMAGE(false);
            prewImagesInt = 0;
            nextImagesInt++;
            informLabel.setText("+" + nextImagesInt);
            saveImageButton.setForeground(new Color(14, 172, 37));
        });

        JButton previousImage = new JButton("PREV");
        previousImage.addActionListener((e) -> {
            setPLAY(false);
            setSTOP(false);
            setPAUSE(true);
            setNextIMAGE(false);
            setPrewIMAGE(true);
            prewImagesInt++;
            nextImagesInt = 0;
            informLabel.setText("-" + prewImagesInt);

            saveImageButton.setForeground(new Color(14, 172, 37));
        });

        JButton slowerButton = new JButton("<<");
        slowerButton.addActionListener((e) -> {
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
            saveImageButton.setForeground(Color.LIGHT_GRAY);
        });

        JButton fasterButton = new JButton(">>");
        fasterButton.addActionListener((e) -> {
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

            saveImageButton.setForeground(Color.LIGHT_GRAY);
        });

        JButton playButton = new JButton("PLAY");
        playButton.addActionListener(actionEvent -> {
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

            saveImageButton.setForeground(Color.LIGHT_GRAY);
        });

        JButton pauseButton = new JButton("PAUSE");
        pauseButton.addActionListener((e) -> {
            setPLAY(false);
            setSTOP(false);
            setPAUSE(true);
            setNextIMAGE(false);
            setPrewIMAGE(false);
            informLabel.setText("PAUSE");
            saveImageButton.setForeground(new Color(14, 172, 37));
        });

        JButton stopButton = new JButton("STOP");
        stopButton.addActionListener(actionEvent -> {
            setPLAY(false);
            setSTOP(true);
            setPAUSE(false);
            setNextIMAGE(false);
            setPrewIMAGE(false);
            informLabel.setText("STOP");
            saveImageButton.setForeground(Color.LIGHT_GRAY);
        });

        JLabel label1 = new JLabel(date);
        sliderLabel = new JLabel("0 %");
        sliderLabel.setPreferredSize(new Dimension(50, 15));
        sliderLabel.setHorizontalAlignment(SwingConstants.CENTER);
        currentFrameLabel = new JLabel();
        currentFrameLabel.setPreferredSize(new Dimension(120,15));
        currentFrameLabel.setHorizontalAlignment(SwingConstants.CENTER);
        informLabel = new JLabel("STOP");
        informLabel.setPreferredSize(new Dimension(50, 15));
        informLabel.setHorizontalAlignment(SwingConstants.CENTER);
        speedLabel = new JLabel("SPEED");
        speedLabel.setPreferredSize(new Dimension(50, 15));
        speedLabel.setHorizontalAlignment(SwingConstants.CENTER);
        FPSLabel.setPreferredSize(new Dimension(80, 15));
        FPSLabel.setHorizontalAlignment(SwingConstants.CENTER);

        buttonsPane.add(backButton);
        buttonsPane.add(Box.createRigidArea(new Dimension(2,10)));
        buttonsPane.add(currentFrameLabel);
        buttonsPane.add(label1);
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
        buttonsPane.add(saveImageButton);
        JPanel southPane = new JPanel();

        JPanel sliderPanel = new JPanel();
        sliderPanel.setBackground(new Color(206, 247, 188));
        GridLayout layout = new GridLayout(1, 100, 0, 0);
        sliderPanel.setLayout(layout);
        sliderPanel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        sliderPanel.setPreferredSize(new Dimension(800, 30));

        list = new ArrayList<>();
        for (int i = 1; i < 100; i++) {
            JLabel label = new JLabel(String.valueOf((char) 8623));
            label.setForeground(Color.LIGHT_GRAY);
            label.setFont(new Font(null, Font.BOLD, 13));
            int finalI = i;
            label.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    setSetPOSITION(true);
                    position = finalI;
                }
            });
            sliderPanel.add(label);
            list.add(label);
        }

        southPane.setLayout(new BoxLayout(southPane, BoxLayout.Y_AXIS));
        southPane.add(sliderPanel);
        southPane.add(buttonsPane);

        this.add(southPane, BorderLayout.SOUTH);
        previousImage.addKeyListener(new KeyAdapter() {//TODO ikjhnbgvfgbhnjmk,l.,kmjnhbg
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
                    saveImageButton.setForeground(new Color(14, 172, 37));
                }
            }
        });

        nextImage.addKeyListener(new KeyAdapter() {//TODO ikjhnbgvfgbhnjmk,l.,kmjnhbg
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == 39) {
                    setPLAY(false);
                    setSTOP(false);
                    setPAUSE(true);
                    setNextIMAGE(true);
                    setPrewIMAGE(false);
                    prewImagesInt = 0;
                    nextImagesInt++;
                    informLabel.setText("+" + nextImagesInt);
                    saveImageButton.setForeground(new Color(14, 172, 37));
                }
            }
        });

        setSliderPosition(0);
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

        if(position>99){
            position=99;
        }

        for (int i = 0; i < position - 1; i++) {
            if (eventPercent.containsKey(i)) {
                if (eventPercent.get(i)) {
                    list.get(i).setForeground(new Color(23, 182, 42));
                } else {
                    list.get(i).setForeground(new Color(197, 99, 39));
                }
            } else {
                list.get(i).setForeground(new Color(4, 2, 133));
            }
        }

        for (int i = position; i < list.size(); i++) {
            if (eventPercent.containsKey(i)) {
                if (eventPercent.get(i)) {
                    list.get(i).setForeground(new Color(24, 227, 42));
                } else {
                    list.get(i).setForeground(new Color(255, 113, 44));
                }

            } else {
//                list.get(i).setForeground(new Color(98, 99, 110));
                list.get(i).setForeground(Color.LIGHT_GRAY);
            }
        }
        sliderLabel.setText(position + "%");
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

    static boolean isSaveIMAGE() {
        return SaveIMAGE;
    }

    static void setCurrentFrameLabelText(int currentFrameLabelText, int buffSize){
        currentFrameLabel.setText("Кадр "+currentFrameLabelText+" : "+buffSize);
    }

    static void ReSetSaveIMAGE() {
        saveImageButton.setForeground(Color.LIGHT_GRAY);
        SaveIMAGE = false;
    }
}
