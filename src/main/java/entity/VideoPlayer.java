package entity;

import ui.main.MainFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;
import java.util.List;

public class VideoPlayer extends JPanel {

    private static boolean showVideoPlayer;
    private boolean STOP;
    private boolean PAUSE;
    private boolean PLAY;
    private boolean SetPOSITION;
    private boolean NextIMAGE;
    private boolean PrewIMAGE;
    private boolean fullSize;

    private static int nextImagesInt;
    private static int prewImagesInt;
    private static int position;

    private static int countDoNotShowImage;
    private double speed = 1;

    public static JLabel informLabel = new JLabel("STOP");
    private JLabel speedLabel;

    private JLabel sliderLabel;
    private JLabel currentFrameLabel;

    private static List<JPanel> sliderPanelsLst;
    private static Map<Integer, Boolean> eventFrameNumberMap = null;
    private static Map<Integer, Boolean> eventPercent = null;
    private List<Integer> eventFrameNumberList = null;
    private List<VideoPlayerPanel> videoPlayerPanelsList;

    private JPanel centralPane;
    private JPanel mainVideoPane;
    private Map<Integer, File> foldersWithTemporaryVideoFiles;

    private int FPS = 0;
    private int totalCountFrames = 0;
    private int frameNumber = 0;
    private int currentFrameNumber = 0;
    private int currentSliderPosition = 0;

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
            if (fullSize) {
                showFourVideo();
                fullSize = false;
            } else {
                stop();
                MainFrame.getMainFrame().showVideosWindow();
            }
        });

        List<Thread> threadList = new ArrayList<>();

        eventFrameNumberMap = new HashMap<>();
        eventPercent = new HashMap<>();
        eventFrameNumberList = new ArrayList<>();
        videoPlayerPanelsList = new ArrayList<>();

        for (int j = 1; j < 5; j++) {
            File folder = foldersWithTemporaryVideoFiles.get(j);
            if (folder != null) {
                String name = folder.getName();
                String[] split = name.split("-");
                String[] fpsSplit = split[1].split("\\.");
                int i = fpsSplit[0].indexOf(")");
                String totalFpsString = fpsSplit[0].substring(2, i);
                int totalFPS = Integer.parseInt(totalFpsString);
                if (FPS < totalFPS) {
                    FPS = totalFPS;
                    totalCountFrames = 0;
                    File[] files = folder.listFiles();
                    if (files != null) {
                        for (File file : files) {
                            try {
                                String fileName = file.getName();
                                String[] fileNameSplit = fileName.split("\\.");
                                String[] lastSplit = fileNameSplit[0].split("-");
                                String countFramesString = lastSplit[1];
                                int countFrames = Integer.parseInt(countFramesString);
                                totalCountFrames += countFrames;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    eventFrameNumberMap.clear();
                    eventFrameNumberList.clear();
                    int first = name.indexOf("[");
                    int second = name.indexOf("]");
                    String substring = name.substring(first + 1, second);
                    String[] eventsSplit = substring.split(",");
                    for (String aSplit : eventsSplit) {
                        boolean contains = aSplit.contains("(");
                        if (contains) {
                            String s = aSplit.substring(1, aSplit.length() - 1);
                            try {
                                int i1 = Integer.parseInt(s);
                                eventFrameNumberMap.put(i1, contains);
                                eventFrameNumberList.add(i1);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        } else {
                            try {
                                int i1 = Integer.parseInt(aSplit);
                                eventFrameNumberMap.put(i1, contains);
                                eventFrameNumberList.add(i1);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }

                    Collections.sort(eventFrameNumberList);
                }
            }

            VideoPlayerPanel videoPlayer = new VideoPlayerPanel(folder, j);
            videoPlayer.setShowVideoNow(true);
            videoPlayer.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (videoPlayer.isBlockHaveVideo()) {
                        if (e.getClickCount() == 2) {
                            if (fullSize) {
                                showFourVideo();
                                fullSize = false;
                            } else {
                                stop();
                                for (VideoPlayerPanel videoPlayerPanel : videoPlayerPanelsList) {
                                    videoPlayerPanel.setShowVideoNow(false);
                                }
                                videoPlayer.setWidthAndHeight(770, 425);
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
            videoPlayerPanelsList.add(videoPlayer);
        }

        System.out.println("Наибольший ФПС - " + FPS);
        System.out.println("Количество кадров - " + totalCountFrames);

        for (Integer integer : eventFrameNumberMap.keySet()) {
            int percent = integer * 1000 / totalCountFrames;
            System.out.println(integer + " - кадр, процентов -" + percent);
            eventPercent.put(percent, eventFrameNumberMap.get(integer));
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
        numberLabel.setPreferredSize(new Dimension(50, 15));
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
        speedLabel = new JLabel(speed + "X");
        speedLabel.setPreferredSize(new Dimension(50, 15));
        speedLabel.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel FPSLabel = new JLabel("FPS: "+ FPS);
        FPSLabel.setPreferredSize(new Dimension(80, 15));
        FPSLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel buttonsPane = new JPanel(new FlowLayout());
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
        sliderPanelsLst = new ArrayList<>();

        for (int i = 1; i < 1000; i++) {
            JPanel panel = new JPanel();
            int finalI = i;
            panel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    position = finalI;
                    double percent = (double) position / 1000;
                    frameNumber = (int) (totalCountFrames * percent);
                    setSetPOSITION(true);
                }
            });

            sliderForVideo.add(panel);
            sliderPanelsLst.add(panel);
        }

        JPanel sliderPanel = new JPanel(new BorderLayout());
        sliderPanel.setPreferredSize(new Dimension(1005, 50));
        sliderPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        sliderPanel.add(sliderForVideo, BorderLayout.NORTH);//85 - 50
        sliderPanel.add(buttonsPane, BorderLayout.CENTER);

        JPanel southPane = new JPanel(new BorderLayout());
        southPane.add(backButton, BorderLayout.WEST);//85 - 50
        southPane.add(sliderPanel, BorderLayout.CENTER);
        this.add(southPane, BorderLayout.SOUTH);

        playButton.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == 37) {
                    prewFrame();
                } else if (e.getKeyCode() == 39) {
                    nextFrame();
                }
            }
        });

        setSliderPosition(0);
        Thread stopPlayingWhileRecordingThread = new Thread(() -> {
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
        stopPlayingWhileRecordingThread.start();
        createPlayerThread();
    }

    private void createPlayerThread() {

        int frameRate = 1000 / FPS;
        Thread timer = new Thread(() -> {
            while (VideoPlayer.isShowVideoPlayer()) {
                if (PLAY) {
                    try {
                        Thread.sleep((long) (frameRate * speed));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }


                    frameNumber++;

                    if (frameNumber == totalCountFrames) {
                        stop();
                    }
                } else if (PAUSE) {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        timer.start();

        Thread videoShowThread = new Thread(() -> {
            while (VideoPlayer.isShowVideoPlayer()) {
                if (frameNumber != currentFrameNumber) {
                    int partNumber = 0;
                    int currentFramePositionPercent = 0;
                    for (int i = 0; i < eventFrameNumberList.size(); i++) {
                        Integer integer = eventFrameNumberList.get(i);
                        if (integer > frameNumber) {
                            partNumber = i;
                            currentFramePositionPercent = frameNumber * 100000 / integer;
                            break;
                        } else {
                            if (i == (eventFrameNumberList.size() - 1)) {
                                partNumber = i + 1;
                                currentFramePositionPercent = frameNumber * 100000 / totalCountFrames;
                            }
                        }
                    }

                    for (VideoPlayerPanel videoPlayerPanel : videoPlayerPanelsList) {
                        if (videoPlayerPanel.isShowVideoNow()) {
                            videoPlayerPanel.showFrameNumber(partNumber, currentFramePositionPercent);
                        }
                    }

                    int sliderPosition = frameNumber * 1000 / totalCountFrames;
                    if (currentSliderPosition != sliderPosition) {
                        currentSliderPosition = sliderPosition;
                        setSliderPosition(currentSliderPosition);
                    }

                    int i = frameNumber;
                    currentFrameNumber = i;
                    setCurrentFrameLabelText(i);
                }
                if (SetPOSITION) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    SetPOSITION = false;
                }
//                }
//                else if (PAUSE) {
//
//
//                } else if (STOP) {
//
//                }

                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            stop();
        });
        videoShowThread.start();
    }

    private void showFourVideo() {
        stop();
        centralPane.removeAll();
        mainVideoPane.removeAll();
        for (int i = 0; i < 4; i++) {
            VideoPlayerPanel playerPanel = videoPlayerPanelsList.get(i);
            playerPanel.setShowVideoNow(true);
            playerPanel.setWidthAndHeight(364, 205);
            mainVideoPane.add(playerPanel);
        }
        centralPane.add(mainVideoPane);
        centralPane.validate();
        centralPane.repaint();
    }

    private void play() {
        for (VideoPlayerPanel videoPlayerPanel : videoPlayerPanelsList) {
            if (videoPlayerPanel.isBlockHaveVideo()) {
                videoPlayerPanel.showVideo();
            }
        }
        setPLAY(true);
        setSTOP(false);
        setPAUSE(false);
        informLabel.setText("PLAY");
    }

    private void stop() {
        for (VideoPlayerPanel videoPlayerPanel : videoPlayerPanelsList) {
            if (videoPlayerPanel.isBlockHaveVideo()) {
                videoPlayerPanel.stopVideo();
            }
        }
        setPLAY(false);
        setSTOP(true);
        setPAUSE(false);
        setSliderPosition(0);
        frameNumber = 0;
        speed = 1;

        speedLabel.setText(speed + "X");
        informLabel.setText("STOP");
    }

    private void pause() {
        setPLAY(false);
        setPAUSE(true);
        informLabel.setText("PAUSE");
    }

    private void fast() {
        speed *= 0.5;
        double i = 0;
        String s = "";
        if (speed <= 1) {
            i = 1 / speed;
        } else {
            i = speed;
            s = "-";
        }
        speedLabel.setText(s + i + "X");
    }

    private void slow() {
        speed /= 0.5;
        double i = 0;
        String s = "";
        if (speed <= 1) {
            i = 1 / speed;
        } else {
            i = speed;
            s = "-";
        }
        speedLabel.setText(s + i + "X");
    }

    private void nextFrame() {
        pause();
        frameNumber++;
    }

    private void prewFrame() {
        pause();
        frameNumber--;
    }

    private void setSetPOSITION(boolean setPOSITION) {
        SetPOSITION = setPOSITION;
    }

    private void setPLAY(boolean PLAY) {
        this.PLAY = PLAY;
    }

    private void setSTOP(boolean STOP) {
        this.STOP = STOP;
    }

    private void setPAUSE(boolean PAUSE) {
        this.PAUSE = PAUSE;
    }

    private void setSliderPosition(int position) {

        if (position > 999) {
            position = 999;
        }

        for (int i = 0; i < position - 1; i++) {
            if (eventPercent.containsKey(i)) {
                if (eventPercent.get(i)) {
                    sliderPanelsLst.get(i).setBackground(new Color(23, 182, 42));
                } else {
                    sliderPanelsLst.get(i).setBackground(new Color(197, 99, 39));
                }
            } else {
                sliderPanelsLst.get(i).setBackground(new Color(4, 2, 133));
            }
        }

        for (int i = position; i < sliderPanelsLst.size(); i++) {
            if (eventPercent.containsKey(i)) {
                if (eventPercent.get(i)) {
                    sliderPanelsLst.get(i).setBackground(new Color(24, 227, 42));
                } else {
                    sliderPanelsLst.get(i).setBackground(new Color(255, 113, 44));
                }

            } else {
                sliderPanelsLst.get(i).setBackground(Color.LIGHT_GRAY);
            }
        }

        int i = position / 10;
        sliderLabel.setText(i + "%");
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

    void setCurrentFrameLabelText(int currentFrameLabelText) {
        currentFrameLabel.setText("Кадр " + currentFrameLabelText);
    }
}
