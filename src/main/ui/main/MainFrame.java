package ui.main;

import entity.AddressSaver;
import entity.MainVideoCreator;
import ui.video.VideoPlayer;
import entity.sound.SoundSaver;
import ui.camera.CameraPanel;
import ui.camera.VideoBytesSaver;
import ui.setting.CameraAddressSetting;
import ui.setting.Setting;
import ui.video.VideoFilesPanel;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;

/**
 * Main frame
 */
public class MainFrame extends JFrame {
    private static String password;

    private static Logger log = Logger.getLogger(MainFrame.class);

    /**
     * TEST MODE PANELS
     */
    private static JButton startEventButton;
    private static JButton startButtonProgrammingCatch;
    private static JLabel testModeLabel = new JLabel();
    /**
     * MAIN FRAME
     */
    private static MainFrame mainFrame;
    private static JPanel mainPanel;
    private static JPanel northPanel;
    private static JPanel centralPanel;
    private static JPanel southPanel;
    /**
     * show count of packets from audio module for one second,
     * and will be red if have exception during creating server socket on port, for waiting event from sensor
     */
    public static JLabel audioPacketCount;

    /**
     * NORTH PANEL
     */
    private static JLabel mainLabel;
    private JLabel recordLabel;
    private static JLabel recordSecondsLabel;

    /**
     * CENTRAL PANEL
     */
    private static CameraAddressSetting cameraAddressSetting;
    private static Setting setting;
    private static VideoFilesPanel videoFilesPanel;
    private JPanel allCameraPanel;

    /**
     * SOUTH PANEL
     */
    private JLabel eventServerPortLabel;
    private static JLabel countSecondsToSaveVideo;
    private static JLabel opacityLabel;
    private static JLabel showImagesLabel;
    private static JLabel photosensitivityLabel;
    private static JLabel changeWhiteLabel;
    private static JLabel informLabel;

    /**
     * cameras panel list, numbers (1-8)
     */
    private static Map<Integer, CameraPanel> cameras;
    /**
     * addresses for cameras
     */
    public static Map<Integer, List<String>> camerasAddress;
    /**
     * camera blocks (4)
     */
    private static Map<Integer, JPanel> cameraBlock;
    /**
     * backgrounds for each block
     */
    public static Map<Integer, BufferedImage> imagesForBlock;
    /**
     * video savers for each block
     */
    public static Map<Integer, VideoBytesSaver> videoSaversMap;


    /**
     * address saver to save and restore setting data
     */
    public static AddressSaver addressSaver;
    /**
     * opacity of background
     */
    private static int opacitySetting;
    /**
     * seconds to save before and after lightning
     */
    private static int secondsToSave = 30;

    /**
     * used for scanning frames for count of white pixels to catch lightning
     */
    private static int percentDiffWhite = 5;

    /**
     * the darkest color number, which will be set as "WHITE"
     * Color color= new Color(colorLightNumber,colorLightNumber,colorLightNumber);
     * program will search on frame pixels more light then this color
     */
    private static int colorLightNumber = 180;

    /**
     * set with RGB numbers of color, which will bet "WHITE"
     */
    private Set<Integer> colorRGBNumberSet;
    /**
     * enable/disable scanning frames to catch lightning
     */
    private static boolean programLightCatchEnable;
    /**
     * port for server socket, for waiting request from sensor about lightning
     */
    private static int port;

    /**
     * path to folder, for saved bytes, during saving video
     * can be changed in setting
     */
    private static String path = "C:\\LIGHTNING_STABLE\\";

    /**
     * default path for temporary files, log files and address saver files
     * can not be changed
     */
    private static String defaultPath = "C:\\LIGHTNING_STABLE\\";

    /**
     * class to save audio fro audio module
     */
    private SoundSaver soundSaver;

    /**
     * mark that some camera is full size mode
     */
    private static boolean fullSize = false;


    private static ResourceBundle bundle;
    /**
     * show the percent of frames from ip camera, witch will be showed on camera panel
     */
    private static int showFramesPercent = 15;


    /**
     * Thread set the socket server to wait request from sensor
     */
    private Thread alarmThread;

    private MainFrame() {
        super("LIGHTNING STABLE");
        imagesForBlock = new HashMap<>();
        cameras = new HashMap<>();
        cameraBlock = new HashMap<>();
        camerasAddress = new HashMap<>();
        videoSaversMap = new HashMap<>();
        colorRGBNumberSet = new HashSet<>();

        this.getContentPane().setLayout(new BorderLayout());
        this.setMinimumSize(new Dimension(1150, 700));
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.LIGHT_GRAY);
        mainPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        JPanel languageSelectPanel = new JPanel(new BorderLayout(5, 5));
        JLabel languageSelectLabel = new JLabel("Select Language");
        languageSelectLabel.setFont(new Font(null, Font.BOLD, 15));
        languageSelectLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel buttonPane = new JPanel(new FlowLayout());
        JButton englishLanguageButton = new JButton("ENGLISH");
        englishLanguageButton.setPreferredSize(new Dimension(100, 50));
        englishLanguageButton.addActionListener((e) -> {
            mainPanel.removeAll();
            Locale.setDefault(new Locale("en", "US"));
            startApplication();
        });

        JButton russianLanguageButton = new JButton("РУССКИЙ");
        russianLanguageButton.setPreferredSize(new Dimension(100, 50));
        russianLanguageButton.addActionListener((e) -> {
            mainPanel.removeAll();
            Locale.setDefault(new Locale("ru", "RU"));//ru_RU
            startApplication();
        });

        buttonPane.add(englishLanguageButton);
        buttonPane.add(russianLanguageButton);
        languageSelectPanel.add(languageSelectLabel, BorderLayout.NORTH);
        languageSelectPanel.add(buttonPane, BorderLayout.CENTER);

        mainPanel.add(languageSelectPanel);

        this.add(mainPanel);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(true);
        pack();
    }

    /**
     * starting application after choosing language
     */
    private void startApplication() {
        bundle = ResourceBundle.getBundle("Labels");
        mainLabel = new JLabel(bundle.getString("mainpage"));
        northPanel = new JPanel(new FlowLayout());
        northPanel.setBackground(Color.LIGHT_GRAY);
        northPanel.setPreferredSize(new Dimension(1110, 54));

        centralPanel = new JPanel();
        centralPanel.setBackground(Color.LIGHT_GRAY);

        southPanel = new JPanel();
        southPanel.setBackground(Color.LIGHT_GRAY);
        southPanel.setPreferredSize(new Dimension(1110, 45));

        mainPanel.add(northPanel, BorderLayout.NORTH);
        mainPanel.add(centralPanel, BorderLayout.CENTER);
        mainPanel.add(southPanel, BorderLayout.SOUTH);

        cameraAddressSetting = CameraAddressSetting.getCameraAddressSetting();
        videoFilesPanel = VideoFilesPanel.getVideoFilesPanel();

        informLabel = new JLabel();
        informLabel.setHorizontalAlignment(SwingConstants.CENTER);
        informLabel.setPreferredSize(new Dimension(270, 30));
        informLabel.setHorizontalAlignment(SwingConstants.CENTER);

        opacityLabel = new JLabel(bundle.getString("opacitycount") + "30%");
        opacityLabel.setHorizontalAlignment(SwingConstants.CENTER);
        opacityLabel.setPreferredSize(new Dimension(150, 30));

        showImagesLabel = new JLabel(bundle.getString("showframescountlabel") + showFramesPercent);
        showImagesLabel.setHorizontalAlignment(SwingConstants.CENTER);
        showImagesLabel.setPreferredSize(new Dimension(150, 30));

        countSecondsToSaveVideo = new JLabel(bundle.getString("savesecondcount") + secondsToSave + bundle.getString("seconds"));
        countSecondsToSaveVideo.setHorizontalAlignment(SwingConstants.CENTER);
        countSecondsToSaveVideo.setPreferredSize(new Dimension(120, 30));

        photosensitivityLabel = new JLabel(bundle.getString("photosensitivity") + colorLightNumber);

        photosensitivityLabel.setHorizontalAlignment(SwingConstants.CENTER);
        photosensitivityLabel.setPreferredSize(new Dimension(180, 30));
        changeWhiteLabel = new JLabel(bundle.getString("lightening") + percentDiffWhite + "%");
        changeWhiteLabel.setHorizontalAlignment(SwingConstants.CENTER);

        eventServerPortLabel = new JLabel();
        eventServerPortLabel.setHorizontalAlignment(SwingConstants.CENTER);
        eventServerPortLabel.setPreferredSize(new Dimension(100, 30));
//        ===============================================================
        addressSaver = AddressSaver.restorePasswords();
        buildMainWindow();
        addressSaver.setPasswordsToFields();
        addressSaver.setSetting();
        cameraAddressSetting.saveAddressToMap();
        setting = Setting.getSetting();

        startAllCameras();

        Thread memoryUpdateThread = new Thread(() -> {
            int playInt = 0;
            int writeLogs = 0;
            boolean red = false;
            boolean startRec = true;

            while (true) {
                if (MainVideoCreator.isSaveVideoEnable()) {
                    if (startRec) {
                        startRec = false;
                    }
                    if (red) {
                        recordLabel.setForeground(Color.DARK_GRAY);
                        recordSecondsLabel.setForeground(Color.DARK_GRAY);
                        red = false;
                    } else {
                        recordLabel.setForeground(Color.RED);
                        recordSecondsLabel.setForeground(Color.RED);
                        red = true;
                    }
                } else {
                    if (red || !startRec) {
                        recordLabel.setForeground(new Color(46, 139, 87));
                        recordSecondsLabel.setForeground(new Color(46, 139, 87));
                        red = false;
                        startRec = true;
                    }
                }

                if (writeLogs > 60) {
                    Runtime.getRuntime().gc();
                    long usedMemory = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576;
                    log.info("Используем памяти " + usedMemory + " mb");
                    writeLogs = 0;
                } else {
                    writeLogs++;
                }

                if (VideoPlayer.isShowVideoPlayer()) {
                    try {
                        if (playInt == 0) {
                            VideoPlayer.informLabel.setForeground(Color.RED);
                            VideoPlayer.informLabel.repaint();
                            playInt++;
                        } else {
                            playInt = 0;
                            VideoPlayer.informLabel.setForeground(Color.LIGHT_GRAY);
                            VideoPlayer.informLabel.repaint();
                        }
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        memoryUpdateThread.setName("Memory Update Main Thread");
        memoryUpdateThread.start();

        alarmThread = new Thread(() -> {
            ServerSocket ss = null;
            try {
                ss = new ServerSocket(port);
            } catch (IOException ignored) {
            }

            while (true) {
                try {
                    audioPacketCount.setForeground(new Color(29, 142, 27));
                    setAlarmServerLabelColor(port, new Color(29, 142, 27));
                    log.info("Ждем сигнал сработки на порт - " + port);
                    Socket socket = ss.accept();
                    log.info("Получили сигнал сработки на порт " + port);
                    MainVideoCreator.startCatchVideo(false);
                    socket.close();
                } catch (Exception e) {
                    log.error(e.getLocalizedMessage());
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                    setAlarmServerLabelColor(port, Color.red);
                    audioPacketCount.setForeground(Color.red);
                }
            }
        });
        alarmThread.setName("Alarm Thread");
        alarmThread.start();

        File fileAddressSaver = new File(defaultPath + "\\data\\");
        fileAddressSaver.mkdirs();

        File fileBuffBytes = new File(path + "\\bytes\\");
        fileBuffBytes.mkdirs();

        for (int i = 1; i < 5; i++) {
            File folder = new File(defaultPath + "\\buff\\" + i + "\\");
            folder.mkdirs();
            File[] files = folder.listFiles();
            for (int j = 0; j < files.length; j++) {
                files[j].delete();
            }
        }

        File pathFolder = new File(path);
        pathFolder.mkdirs();
    }

    private void buildMainWindow() {
        buildNorthPanel();
        buildCentralPanel();
        buildSouthPanel();
    }

    private void buildNorthPanel() {
        audioPacketCount = new JLabel("AUDIO");
        JButton mainWindowButton = new JButton(bundle.getString("mainpage"));
        mainWindowButton.setPreferredSize(new Dimension(120, 30));
        mainWindowButton.addActionListener((e) -> {
            VideoPlayer.setShowVideoPlayer(false);
            if (fullSize) {
                for (Integer cameraNumber : cameras.keySet()) {
                    CameraPanel cameraPanel = cameras.get(cameraNumber);
                    if (cameraPanel.isFullSizeEnable()) {
                        cameraPanel.setFullSizeEnable(false);
                        int blockNumber = (cameraNumber + 1) / 2;
                        JPanel blockPanel = cameraBlock.get(blockNumber);
                        if (cameraNumber % 2 == 0) {
                            blockPanel.remove(cameraPanel);
                            blockPanel.add(cameraPanel);
                            blockPanel.repaint();
                        } else {
                            Component firstCamera = blockPanel.getComponent(0);
                            blockPanel.removeAll();
                            blockPanel.add(cameraPanel);
                            blockPanel.add(firstCamera);
                            blockPanel.validate();
                            blockPanel.repaint();
                        }
                    }
                }
                fullSize = false;
            }
            setCentralPanel(allCameraPanel);
            mainLabel.setText(bundle.getString("mainpage"));
        });

        JButton cameraButton = new JButton(bundle.getString("camerassettingpage"));
        cameraButton.setPreferredSize(new Dimension(120, 30));
        cameraButton.addActionListener((e) -> {
            VideoPlayer.setShowVideoPlayer(false);
            setCentralPanel(cameraAddressSetting);
            mainLabel.setText(bundle.getString("camerassettingpage"));
        });

        JButton videoButton = new JButton(bundle.getString("videospage"));
        videoButton.setPreferredSize(new Dimension(120, 30));
        videoButton.addActionListener((e) -> {
            showVideoFilesPanel();
        });

        JButton settingButton = new JButton(bundle.getString("settingpage"));
        settingButton.setPreferredSize(new Dimension(120, 30));
        settingButton.addActionListener((e) -> {
            VideoPlayer.setShowVideoPlayer(false);
            setting.saveButton.setForeground(Color.BLACK);
            setting.saveButton.setText(bundle.getString("savebutton"));
            setting.reSetPassword();
            setCentralPanel(setting);
            mainLabel.setText(bundle.getString("settingpage"));
        });

        mainLabel.setFont(new Font(null, Font.BOLD, 15));
        mainLabel.setPreferredSize(new Dimension(120, 30));

        JPanel informPane = new JPanel(new FlowLayout());
        informPane.setBackground(Color.LIGHT_GRAY);
        recordLabel = new JLabel(String.valueOf((char) 8623));
        recordLabel.setFont(new Font(null, Font.BOLD, 23));
        recordLabel.setForeground(Color.DARK_GRAY);
        informPane.add(mainLabel);
        informPane.add(recordLabel);
        recordSecondsLabel = new JLabel();
        recordSecondsLabel.setPreferredSize(new Dimension(150, 30));
        recordSecondsLabel.setHorizontalAlignment(SwingConstants.CENTER);

        startEventButton = new JButton("REC");
        startEventButton.setVisible(false);
        startEventButton.addActionListener((e -> {
            MainVideoCreator.startCatchVideo(false);
        }));

        startButtonProgrammingCatch = new JButton("REC PR");
        startButtonProgrammingCatch.setVisible(false);
        startButtonProgrammingCatch.addActionListener((e -> {
            MainVideoCreator.startCatchVideo(true);
        }));

        northPanel.add(audioPacketCount);
        northPanel.add(Box.createRigidArea(new Dimension(15, 10)));
        northPanel.add(mainWindowButton);
        northPanel.add(cameraButton);
        northPanel.add(videoButton);
        northPanel.add(settingButton);

        testModeLabel.setPreferredSize(new Dimension(165, 25));
        northPanel.add(testModeLabel);
        northPanel.add(startEventButton);
        northPanel.add(startButtonProgrammingCatch);

        northPanel.add(recordSecondsLabel);
        northPanel.add(informPane);
    }

    private void buildCentralPanel() {
        centralPanel.setLayout(new BorderLayout());
        GridLayout gridLayout = new GridLayout(2, 2, 2, 2);
        allCameraPanel = new JPanel();
        allCameraPanel.setLayout(gridLayout);

        for (int i = 1; i < 5; i++) {
            JPanel blockPanel;
            VideoBytesSaver videoBytesSaver = new VideoBytesSaver(i);
            videoBytesSaver.setBackGroundImage(imagesForBlock.get(i));
            videoSaversMap.put(i, videoBytesSaver);
            CameraPanel cameraOne = new CameraPanel(videoBytesSaver, (i * 2 - 1));
            cameraOne.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        if (fullSize) {
                            for (Integer cameraNumber : cameras.keySet()) {
                                CameraPanel cameraPanel = cameras.get(cameraNumber);
                                if (cameraPanel.isFullSizeEnable()) {
                                    cameraPanel.setFullSizeEnable(false);
                                    int blockNumber = (cameraNumber + 1) / 2;
                                    JPanel blockPanel = cameraBlock.get(blockNumber);
                                    if (cameraNumber % 2 == 0) {
                                        blockPanel.remove(cameraPanel);
                                        blockPanel.add(cameraPanel);
                                    } else {
                                        Component firstCamera = blockPanel.getComponent(0);
                                        blockPanel.removeAll();
                                        blockPanel.add(cameraPanel);
                                        blockPanel.add(firstCamera);
                                        blockPanel.validate();
                                    }
                                }
                            }
                            setCentralPanel(allCameraPanel);
                            fullSize = false;
                            mainLabel.setText(bundle.getString("mainpage"));
                        } else {
                            cameraOne.setFullSizeEnable(true);
                            setCentralPanel(cameraOne);
                            fullSize = true;
                        }
                    }
                }
            });
            cameras.put(i * 2 - 1, cameraOne);

            CameraPanel cameraTwo = new CameraPanel(videoBytesSaver, (i * 2));
            cameraTwo.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        if (fullSize) {
                            for (Integer cameraNumber : cameras.keySet()) {
                                CameraPanel cameraPanel = cameras.get(cameraNumber);
                                if (cameraPanel.isFullSizeEnable()) {
                                    cameraPanel.setFullSizeEnable(false);
                                    int blockNumber = (cameraNumber + 1) / 2;
                                    JPanel blockPanel = cameraBlock.get(blockNumber);
                                    if (cameraNumber % 2 == 0) {
                                        blockPanel.remove(cameraPanel);
                                        blockPanel.add(cameraPanel);
                                    } else {
                                        Component firstCamera = blockPanel.getComponent(0);
                                        blockPanel.removeAll();
                                        blockPanel.add(cameraPanel);
                                        blockPanel.add(firstCamera);
                                        blockPanel.validate();
                                    }
                                }
                            }
                            setCentralPanel(allCameraPanel);
                            fullSize = false;
                            mainLabel.setText(bundle.getString("mainpage"));
                        } else {
                            cameraTwo.setFullSizeEnable(true);
                            setCentralPanel(cameraTwo);
                            fullSize = true;
                        }
                    }
                }
            });
            cameras.put(i * 2, cameraTwo);
            blockPanel = new JPanel();
            GridLayout gridLayout1 = new GridLayout(1, 2);
            blockPanel.setLayout(gridLayout1);
            blockPanel.add(cameraOne);
            blockPanel.add(cameraTwo);
            blockPanel.setBorder(BorderFactory.createEtchedBorder());
            cameraBlock.put(i, blockPanel);
            allCameraPanel.add(blockPanel);
        }
        centralPanel.add(allCameraPanel);
    }

    private void buildSouthPanel() {
        southPanel.setLayout(new FlowLayout());
        southPanel.add(eventServerPortLabel);
        southPanel.add(countSecondsToSaveVideo);
        southPanel.add(opacityLabel);
        southPanel.add(showImagesLabel);
        southPanel.add(photosensitivityLabel);
        southPanel.add(changeWhiteLabel);
        southPanel.add(informLabel);
    }

    public void setCentralPanel(JPanel panel) {
        centralPanel.removeAll();
        centralPanel.add(panel);
        centralPanel.validate();
        centralPanel.repaint();
    }

    public void showVideoFilesPanel() {
        VideoPlayer.setShowVideoPlayer(false);
        videoFilesPanel.showVideos();
        setCentralPanel(videoFilesPanel);
        mainLabel.setText(bundle.getString("videospage"));
    }

    public static void showInformMassage(String massage, Color color) {
        informLabel.setText(massage);
        informLabel.setForeground(color);
        informLabel.repaint();
    }

    public static void showSecondsAlreadySaved(String massage) {
        recordSecondsLabel.setText(massage);
        recordSecondsLabel.repaint();
    }

    public static void removeBackgroundForBlock(int number) {
        imagesForBlock.remove(number);
        CameraPanel cameraPanel = cameras.get(number * 2 - 1);
        cameraPanel.repaintCameraWindow();
        CameraPanel cameraPanel1 = cameras.get(number * 2);
        cameraPanel1.repaintCameraWindow();
    }

    public static void addBackgroundForBlock(BufferedImage image, int numberGroup) {
        imagesForBlock.put(numberGroup, image);
    }

    /**
     * start/restart to read data from camera stream
     * used each time when will be changed ip address or cameras, and when program start
     */
    public void startAllCameras() {
        for (Integer addressNumber : camerasAddress.keySet()) {
            if (addressNumber == null) {
                continue;
            }
            List<String> list = camerasAddress.get(addressNumber);
            if (list != null) {
                log.info("Камера - " + addressNumber + " будет запущена.");
                URL url = null;
                try {
                    url = new URL(list.get(0));
                    Authenticator.setDefault(new Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(list.get(1), list.get(2).toCharArray());
                        }
                    });
                } catch (MalformedURLException ex) {
                    ex.printStackTrace();
                }
                cameras.get(addressNumber).getVideoCatcher().startCatchVideo(url);
            } else {
                log.info("Камера - " + addressNumber + " НЕ будет запущена.");
                cameras.get(addressNumber).getVideoCatcher().stopCatchVideo();
            }
        }

        if (soundSaver == null) {
            Thread gh = new Thread(() -> {
                List<String> list = camerasAddress.get(null);
                if (list != null && list.size() != 0) {
                    String string = list.get(0);
                    soundSaver = new SoundSaver(string);
                    soundSaver.SETUP();
                    soundSaver.PLAY();
                }
            });
            gh.start();
        } else {
            soundSaver.TEARDOWN();
            soundSaver = null;
        }
    }

    public static MainFrame getMainFrame() {
        if (mainFrame != null) {
            return mainFrame;
        } else {
            mainFrame = new MainFrame();
            return mainFrame;
        }
    }

    public static void setOpacitySetting(int opacity) {
        MainFrame.opacitySetting = opacity;
        opacityLabel.setText(bundle.getString("opacitycount") + opacity + "%");
        opacityLabel.repaint();
        Float f = (float) opacity / 100;
        CameraPanel.setOpacity(f);

        for (Integer integer : imagesForBlock.keySet()) {
            CameraPanel cameraPanel = cameras.get(integer);
            if (cameraPanel.getVideoCatcher().isCatchVideo()) {
                cameraPanel.showCopyImage();
            }
        }
    }

    public static int getOpacitySetting() {
        return opacitySetting;
    }

    public static void setCountSecondsToSaveVideo(int countSave) {
        secondsToSave = countSave;
        countSecondsToSaveVideo.setText(bundle.getString("savesecondcount") + secondsToSave + bundle.getString("seconds"));
        countSecondsToSaveVideo.repaint();
    }

    public static int getPercentDiffWhite() {
        return percentDiffWhite;
    }

    public static void setPercentDiffWhite(int percentDiffWhite) {
        MainFrame.percentDiffWhite = percentDiffWhite;
        changeWhiteLabel.setText(bundle.getString("lightening") + percentDiffWhite + "%");
        changeWhiteLabel.repaint();
    }

    private void setAlarmServerLabelColor(int port, Color color) {
        eventServerPortLabel.setText(bundle.getString("portstring") + port);
        eventServerPortLabel.setForeground(color);
        eventServerPortLabel.repaint();
    }

    public SoundSaver getSoundSaver() {
        return soundSaver;
    }

    public static int getColorLightNumber() {
        return colorLightNumber;
    }

    public void setColorLightNumber(int colorLightNumber) {
        MainFrame.colorLightNumber = colorLightNumber;
        photosensitivityLabel.setText(bundle.getString("photosensitivity") + colorLightNumber);
        photosensitivityLabel.repaint();

        Thread thread = new Thread(() -> {
            setColorNumbersSet(colorLightNumber, colorRGBNumberSet);
        });
        thread.start();
    }

    private void setColorNumbersSet(int number, Set<Integer> set) {
        set.clear();
        for (int i = number; i < 250; i++) {
            for (int k = number; k < 250; k++) {
                for (int g = number; g < 250; g++) {//238
                    if (i == 238 & k == 238 & g == 238) {
                        continue;
                    }
                    Color color = new Color(i, k, g);
                    int rgb = color.getRGB();
                    set.add(rgb);
                }
            }
        }
    }

    public Set<Integer> getColorRGBNumberSet() {
        return colorRGBNumberSet;
    }

    public static boolean isProgramLightCatchEnable() {
        return programLightCatchEnable;
    }

    public static void setProgramLightCatchEnable(boolean programLightCatchEnable) {
        MainFrame.programLightCatchEnable = programLightCatchEnable;
        if (programLightCatchEnable) {
            photosensitivityLabel.setForeground(new Color(32, 175, 33));
            changeWhiteLabel.setForeground(new Color(32, 175, 33));
        } else {
            photosensitivityLabel.setForeground(Color.LIGHT_GRAY);
            changeWhiteLabel.setForeground(Color.LIGHT_GRAY);
        }

        photosensitivityLabel.repaint();
        changeWhiteLabel.repaint();
    }

    public static int getSecondsToSave() {
        return secondsToSave;
    }

    public static void setPort(int port) {
        MainFrame.port = port;
    }

    public static void setPath(String path) {
        MainFrame.path = path;
    }

    public static int getPort() {
        return port;
    }

    public static String getPath() {
        return path;
    }

    public static void setTestMode(boolean testMode) {
        testModeLabel.setVisible(!testMode);
        startButtonProgrammingCatch.setPreferredSize(new Dimension(80, 25));
        startEventButton.setPreferredSize(new Dimension(80, 25));
        startEventButton.setVisible(testMode);
        startButtonProgrammingCatch.setVisible(testMode);
    }

    public static String getDefaultPath() {
        return defaultPath;
    }

    public static String getPassword() {
        return password;
    }

    public static void setPassword(String password) {
        MainFrame.password = password;
    }

    public static int getShowFramesPercent() {
        return showFramesPercent;
    }

    public static ResourceBundle getBundle() {
        return bundle;
    }

    public static void setShowFramesPercent(int showFramesPercent) {
        MainFrame.showFramesPercent = showFramesPercent;
        showImagesLabel.setText(bundle.getString("showframescountlabel") + showFramesPercent);
        showImagesLabel.repaint();
    }

    public static Map<Integer, CameraPanel> getCameras() {
        return cameras;
    }
}










