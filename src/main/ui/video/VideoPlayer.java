package ui.video;

import entity.MainVideoCreator;
import ui.main.MainFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;
import java.util.List;

public class VideoPlayer extends JPanel {

    private static boolean showVideoPlayer;
    private boolean PAUSE;
    private boolean PLAY;
    private boolean SetPOSITION;
    private boolean fullSize;

    private static int position;

    private double speed = 1;

    public static JLabel informLabel = new JLabel("STOP");
    private JLabel speedLabel;
    private JLabel FPSLabel;

    private JLabel sliderLabel;
    private JLabel currentFrameLabel;

    private List<JPanel> sliderPanelsLst;
    private Map<Integer, Boolean> eventPercent = null;
    private List<Integer> eventFrameNumberList = null;
    private Map<Integer, Integer> tempEventsMapPartSize;
    private List<VideoPlayerPanel> videoPlayerPanelsList;

    private JPanel centralPane;
    private JPanel mainVideoPane;

    private int FPS = 0;
    private int totalCountFrames = 0;
    private int frameNumber = 0;
    private int currentFrameNumber = 0;
    private int currentSliderPosition = 0;
    private JButton playButton;


    VideoPlayer(Map<Integer, File> foldersWithTemporaryVideoFiles, String date, int numberInt) {
        this.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        this.setLayout(new BorderLayout());
        centralPane = new JPanel(new BorderLayout());
        mainVideoPane = new JPanel();
        GridLayout mainVideoPaneLayout = new GridLayout(2, 2, 3, 3);
        mainVideoPane.setLayout(mainVideoPaneLayout);

        JButton backButton = new JButton(MainFrame.getBundle().getString("backbutton"));
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

        Map<Integer, Boolean> eventFrameNumberMap = new HashMap<>();
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

                    tempEventsMapPartSize = new HashMap<>();
                    int lastFrame = 0;
                    for (int k = 0; k < eventFrameNumberList.size(); k++) {
                        Integer integer = eventFrameNumberList.get(k);
                        tempEventsMapPartSize.put(k, (integer - lastFrame));
                        lastFrame = integer;

                        if (k == eventFrameNumberList.size() - 1) {
                            tempEventsMapPartSize.put(k + 1, (totalCountFrames - lastFrame));
                        }
                    }

                    for (Integer o : tempEventsMapPartSize.keySet()) {
                        System.out.println("Часть номер - " + o + " равна = " + tempEventsMapPartSize.get(o));
                    }
                }
            }

            VideoPlayerPanel videoPlayer = new VideoPlayerPanel(folder, j);
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
                                videoPlayer.setShowVideoNow(true);
                                videoPlayer.setFullSize(true);
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

        for (Integer integer : eventFrameNumberMap.keySet()) {
            int percent = integer * 1000 / totalCountFrames;
            eventPercent.put(percent, eventFrameNumberMap.get(integer));
        }

        centralPane.add(mainVideoPane);
        this.add(centralPane, BorderLayout.CENTER);

        JButton nextImage = new JButton("+1");
        nextImage.setFont(new Font(null, Font.BOLD, 17));
        nextImage.setFocusable(false);
        nextImage.addActionListener((e) -> {
            nextFrame();
        });

        JButton previousImage = new JButton("-1");
        previousImage.setFont(new Font(null, Font.BOLD, 17));
        previousImage.setFocusable(false);
        previousImage.addActionListener((e) -> {
            prewFrame();
        });

        JButton slowerButton = new JButton(String.valueOf((char) 9194));//⏪
        slowerButton.setFont(new Font(null, Font.BOLD, 17));
        slowerButton.setFocusable(false);
        slowerButton.addActionListener((e) -> {
            slow();
        });

        JButton fasterButton = new JButton(String.valueOf((char) 9193));
        fasterButton.setFont(new Font(null, Font.BOLD, 17));
        fasterButton.setFocusable(false);
        fasterButton.addActionListener((e) -> {
            fast();
        });

        playButton = new JButton(String.valueOf((char) 9205));
        playButton.setFont(new Font(null, Font.BOLD, 17));
        playButton.setFocusable(true);
        playButton.addActionListener(actionEvent -> {
            play();
        });

        JButton pauseButton = new JButton(String.valueOf((char) 9208));
        pauseButton.setFont(new Font(null, Font.BOLD, 17));
        pauseButton.setFocusable(false);
        pauseButton.addActionListener((e) -> {
            pause();
        });

        JButton stopButton = new JButton(String.valueOf((char) 9209));
        stopButton.setFont(new Font(null, Font.BOLD, 17));
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
        FPSLabel = new JLabel("FPS: " + FPS);
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
                    setSetPOSITION();
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

        JPanel southPane = new JPanel(new BorderLayout(2, 2));
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
        stopPlayingWhileRecordingThread.setName("Stop Playing While Recording Thread VIDEO PLAYER ");
        stopPlayingWhileRecordingThread.start();
        for (Thread thread : threadList) {
            if (thread != null) {
                thread.start();
            }
        }
        createPlayerThread();
    }

    private void createPlayerThread() {
        int frameRate = 1000 / FPS;
        Thread timer = new Thread(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
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
        timer.setName("Video Player  VIDEO PLAYER  Timer Thread  VIDEO PLAYER ");
        timer.start();

        Thread videoShowThread = new Thread(() -> {
            while (VideoPlayer.isShowVideoPlayer()) {
                if (frameNumber != currentFrameNumber) {
                    int partNumber = 0;
                    int currentFramePositionPercent = 0;

//                    Map<Integer, Integer> tempEventsMapPartSize = new HashMap<>();
//                    int lastFrame = 0;
//                    for (int i = 0; i < eventFrameNumberList.size(); i++) {
//                        Integer integer = eventFrameNumberList.get(i);
//                        tempEventsMapPartSize.put(i, (integer - lastFrame));
//                        lastFrame = integer;
//
//                        if (i == eventFrameNumberList.size() - 1) {
//                            tempEventsMapPartSize.put(i + 1, (totalCountFrames - lastFrame));
//                        }
//                    }
                    for (int i = 0; i < eventFrameNumberList.size(); i++) {
                        Integer integer = eventFrameNumberList.get(i);
                        if (integer > frameNumber) {
                            partNumber = i;
                            int frameNumberInPart;
                            if(i==0){
                                frameNumberInPart = frameNumber;
                            } else {
                                frameNumberInPart = frameNumber - eventFrameNumberList.get(i-1);
                            }

                            currentFramePositionPercent = frameNumberInPart * 100000 / tempEventsMapPartSize.get(partNumber);
                            break;
                        } else {
                            if (i == (eventFrameNumberList.size() - 1)) {
                                partNumber = i + 1;
                                int frameNumberInPart = frameNumber - eventFrameNumberList.get(i);
                                currentFramePositionPercent = frameNumberInPart * 100000 / tempEventsMapPartSize.get(partNumber);
                            }
                        }
                    }


//                    for (int i = 0; i < eventFrameNumberList.size(); i++) {
//                        Integer integer = eventFrameNumberList.get(i);
//                        if (integer > frameNumber) {
//                            partNumber = i;
//                            currentFramePositionPercent = frameNumber * 100000 / integer;
//                            break;
//                        } else {
//                            if (i == (eventFrameNumberList.size() - 1)) {
//                                partNumber = i + 1;
//                                currentFramePositionPercent = frameNumber * 100000 / totalCountFrames;
//                            }
//                        }
//                    }

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

                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            stop();
        });
        videoShowThread.setName("Video Player MainShow Thread  VIDEO PLAYER ");
        videoShowThread.start();
    }

    private void showFourVideo() {
        stop();
        centralPane.removeAll();
        mainVideoPane.removeAll();
        for (int i = 0; i < 4; i++) {
            VideoPlayerPanel playerPanel = videoPlayerPanelsList.get(i);
            playerPanel.setShowVideoNow(true);
            playerPanel.setFullSize(false);
            mainVideoPane.add(playerPanel);
        }
        centralPane.add(mainVideoPane);
        centralPane.validate();
        centralPane.repaint();
    }

    private void play() {
        playButton.requestFocus();
        for (VideoPlayerPanel videoPlayerPanel : videoPlayerPanelsList) {
            if (videoPlayerPanel.isBlockHaveVideo()) {
                videoPlayerPanel.showVideo();
            }
        }
        setPLAY(true);
        setPAUSE(false);
        informLabel.setText("PLAY");
    }

    private void stop() {
        playButton.requestFocus();
        for (VideoPlayerPanel videoPlayerPanel : videoPlayerPanelsList) {
            if (videoPlayerPanel.isBlockHaveVideo()) {
                videoPlayerPanel.stopVideo();
            }
        }
        setPLAY(false);
        setPAUSE(false);
        setSliderPosition(0);
        frameNumber = 0;
        speed = 1;

        speedLabel.setText(speed + "X");
        informLabel.setText("STOP");
    }

    private void pause() {
        playButton.requestFocus();
        setPLAY(false);
        setPAUSE(true);
        informLabel.setText("PAUSE");
    }

    private void fast() {
        playButton.requestFocus();
        speed *= 0.5;
        double i = 0;
        String s = "";
        if (speed <= 1) {
            i = 1 / speed;
            FPSLabel.setText("FPS: " + (FPS * i));
        } else {
            i = speed;
            s = "-";
            FPSLabel.setText("FPS: " + (FPS / i));
        }

        speedLabel.setText(s + i + "X");
    }

    private void slow() {
        playButton.requestFocus();
        speed /= 0.5;
        double i = 0;
        String s = "";
        if (speed <= 1) {
            i = 1 / speed;
            FPSLabel.setText("FPS: " + (FPS * i));
        } else {
            i = speed;
            s = "-";
            FPSLabel.setText("FPS: " + (FPS / i));
        }

        speedLabel.setText(s + i + "X");
    }

    private void nextFrame() {
        playButton.requestFocus();
        pause();
        frameNumber++;
        if (frameNumber > totalCountFrames) {
            stop();
        }
    }

    private void prewFrame() {
        playButton.requestFocus();
        pause();
        frameNumber--;
        if (frameNumber < 1) {
            frameNumber = 1;
        }
    }

    private void setSetPOSITION() {
        SetPOSITION = true;
    }

    private void setPLAY(boolean PLAY) {
        this.PLAY = PLAY;
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

    public static boolean isShowVideoPlayer() {
        return showVideoPlayer;
    }

    public static void setShowVideoPlayer(boolean showVideoPlayer) {
        VideoPlayer.showVideoPlayer = showVideoPlayer;
    }

    private void setCurrentFrameLabelText(int currentFrameLabelText) {
        currentFrameLabel.setText(MainFrame.getBundle().getString("framenumberlabel") + currentFrameLabelText);
    }
}
