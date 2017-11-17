package ui.video;

import entity.MainVideoCreator;
import ui.main.MainFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;
import java.util.List;

/**
 * class to play four videos together, have four video panels (OneVideoPlayerPanel), one for each camera group
 * VideoPlayer parse the biggest frames count video of four videos, split the video by events frame number
 * (example - if video have one lightning(event), will be two parts, if two lightnings(events) - 3 parts),
 * and, depending on it, set the number of frame(percent - 100000%) in part of video,
 * which each OneVideoPlayerPanel should show during playing (play, stop or set position)/
 */
public class VideoPlayer extends JPanel {

    /**
     * mark when central panel show VideoPlayer
     */
    private static boolean showVideoPlayer;
    /**
     * show that one of OneVideoPlayerPanels is full size
     */
    private boolean fullSize;


    /**
     * PLAY, PAUSE
     */
    private boolean PAUSE;
    private boolean PLAY;

    /**
     * set position to show on slider
     */
    private boolean SetPOSITION;
    /**
     * position (0-1000)
     */
    private static int position;

    /**
     * speed of video
     */
    private double speed = 1;

    public static JLabel informLabel = new JLabel("STOP");
    private JLabel speedLabel;
    private JLabel FPSLabel;
    private JLabel sliderLabel;
    private JLabel currentFrameLabel;
    private JPanel centralPane;
    private JPanel mainVideoPane;
    private JButton playButton;


    /**
     * list op panels for slider, timestamp panel
     */
    private List<JPanel> sliderPanelsLst;

    /**
     * collection with number of frame when was lightning as key,
     * and type on detection(program or sensor) as value
     */
    private Map<Integer, Boolean> eventPercent = null;
    /**
     * numbers of frame when was lightning
     */
    private List<Integer> eventFrameNumberList = null;

    /**
     * after splitting video by events, save the parts here,
     * number of video part as key, count frames in part as value
     */
    private Map<Integer, Integer> tempEventsMapPartSize;


    /**
     * OneVideoPlayerPanel list
     */
    private List<OneVideoPlayerPanel> oneVideoPlayerPanelsList;


    /**
     * fps of the biggest video( by count frames)
     */
    private int FPS = 0;

    /**
     * frames count of the biggest video( by count frames)
     */
    private int totalCountFrames = 0;
    /**
     * frame number, which all oneVideoPlayerPanels should show
     */
    private int frameNumber = 0;
    /**
     * frame number, which all oneVideoPlayerPanels should show (to compare)
     */
    private int currentFrameNumber = 0;
    /**
     * position to jump to
     */
    private int currentSliderPosition = 0;

    /**
     * @param foldersWithTemporaryVideoFiles folder to search files
     * @param dateToShow                     - date, to show on pane;
     * @param videoNumberInList              - number to show on panel
     */
    VideoPlayer(Map<Integer, File> foldersWithTemporaryVideoFiles, String dateToShow, int videoNumberInList) {
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
                MainFrame.getMainFrame().showVideoFilesPanel();
            }
        });

        List<Thread> threadList = new ArrayList<>();

        Map<Integer, Boolean> eventFrameNumberMap = new HashMap<>();
        eventPercent = new HashMap<>();
        eventFrameNumberList = new ArrayList<>();
        oneVideoPlayerPanelsList = new ArrayList<>();

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
                }
            }

            OneVideoPlayerPanel videoPlayer = new OneVideoPlayerPanel(folder, j);
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
                                for (OneVideoPlayerPanel oneVideoPlayerPanel : oneVideoPlayerPanelsList) {
                                    oneVideoPlayerPanel.setShowVideoNow(false);
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
            oneVideoPlayerPanelsList.add(videoPlayer);
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
            previousFrame();
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

        JLabel numberLabel = new JLabel(String.valueOf(videoNumberInList));
        numberLabel.setFont(new Font(null, Font.BOLD, 15));
        numberLabel.setPreferredSize(new Dimension(50, 15));
        numberLabel.setHorizontalTextPosition(SwingConstants.CENTER);

        JLabel dateLabel = new JLabel(dateToShow);
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
                    previousFrame();
                } else if (e.getKeyCode() == 39) {
                    nextFrame();
                }
            }
        });

        setSliderPosition(0);
        Thread stopPlayingWhileRecordingThread = new Thread(() -> {
            while (VideoPlayer.isShowVideoPlayer()) {
                if (MainVideoCreator.isSaveVideoEnable()) {
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

    /**
     * creating Threads to control all OneVideoPlayerPanels
     * <p>
     * 'timer' thread - depend on fps set number of "frameNumber"
     * <p>
     * <p>
     * 'videoShowThread' thread - if "currentFrameNumber" != "frameNumber",
     * this thread calculate the percent(currentFramePositionPercent) of file part for "frameNumber",
     * ask all OneVideoPlayerPanels to show this "currentFramePositionPercent",
     * and set currentFrameNumber = frameNumber
     */
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
                    for (int i = 0; i < eventFrameNumberList.size(); i++) {
                        Integer integer = eventFrameNumberList.get(i);
                        if (integer > frameNumber) {
                            partNumber = i;
                            int frameNumberInPart;
                            if (i == 0) {
                                frameNumberInPart = frameNumber;
                            } else {
                                frameNumberInPart = frameNumber - eventFrameNumberList.get(i - 1);
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

                    for (OneVideoPlayerPanel videoPlayerPanel : oneVideoPlayerPanelsList) {
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

    /**
     * used to show four One VideoPlayerPanel again after FULL SIZE mode
     */
    private void showFourVideo() {
        stop();
        centralPane.removeAll();
        mainVideoPane.removeAll();
        for (int i = 0; i < 4; i++) {
            OneVideoPlayerPanel playerPanel = oneVideoPlayerPanelsList.get(i);
            playerPanel.setShowVideoNow(true);
            playerPanel.setFullSize(false);
            mainVideoPane.add(playerPanel);
        }
        centralPane.add(mainVideoPane);
        centralPane.validate();
        centralPane.repaint();
    }

    /**
     * used to play video
     */
    private void play() {
        playButton.requestFocus();
        for (OneVideoPlayerPanel oneVideoPlayerPanel : oneVideoPlayerPanelsList) {
            if (oneVideoPlayerPanel.isBlockHaveVideo()) {
                oneVideoPlayerPanel.showVideo();
            }
        }
        setPLAY(true);
        setPAUSE(false);
        informLabel.setText("PLAY");
    }

    /**
     * used to stop video
     */
    private void stop() {
        playButton.requestFocus();
        for (OneVideoPlayerPanel oneVideoPlayerPanel : oneVideoPlayerPanelsList) {
            if (oneVideoPlayerPanel.isBlockHaveVideo()) {
                oneVideoPlayerPanel.stopVideo();
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

    /**
     * used to pause video
     */
    private void pause() {
        playButton.requestFocus();
        setPLAY(false);
        setPAUSE(true);
        informLabel.setText("PAUSE");
    }

    /**
     * to show video twice faster
     */
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

    /**
     * to show video twice slower
     */
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

    /**
     * to set PAUSE and show next frame
     */
    private void nextFrame() {
        playButton.requestFocus();
        pause();
        frameNumber++;
        if (frameNumber > totalCountFrames) {
            stop();
        }
    }

    /**
     * to set PAUSE and show previous frame
     */
    private void previousFrame() {
        playButton.requestFocus();
        pause();
        frameNumber--;
        if (frameNumber < 1) {
            frameNumber = 1;
        }
    }

    /**
     * to draw slider
     *
     * @param position - current position(0-999)
     */
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

    private void setSetPOSITION() {
        SetPOSITION = true;
    }

    private void setPLAY(boolean PLAY) {
        this.PLAY = PLAY;
    }

    private void setPAUSE(boolean PAUSE) {
        this.PAUSE = PAUSE;
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