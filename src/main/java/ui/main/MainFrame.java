package ui.main;

import entity.AddressSaver;
import entity.MainVideoCreator;
import entity.VideoPlayer;
import entity.sound.SoundSaver;
import org.apache.log4j.Logger;
import ui.camera.CameraPanel;
import ui.camera.VideoCreator;
import ui.setting.CameraAddressSetting;
import ui.setting.Setting;
import ui.video.VideoFilesPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainFrame extends JFrame {
    private static Logger log = Logger.getLogger(MainFrame.class);

    private static JButton startButton;
    private static JButton startButtonProgrammingCatch;
    private static JLabel testModeLabel = new JLabel();
    public static JLabel audioPacketCount;

    private static MainFrame mainFrame;
    private static JPanel mainPanel;
    private static JPanel northPanel;
    private static JPanel centralPanel;
    private static JPanel southPanel;

    private static CameraAddressSetting cameraAddressSetting;
    private static Setting setting;
    private static VideoFilesPanel videoFilesPanel;

    private JPanel allCameraPanel;


    private static JLabel countSaveVideo;
    private static JLabel opacityLabel;
    private static JLabel showImagesLabel;
    private static JLabel lightSensitivityLabel;
    private static JLabel changeWhiteLabel;


    private JLabel usedMemoryLabel;
    private JLabel alarmServerLabel;
    private JLabel maxMemoryLabel;
    private static int maxMemory;
    private static JLabel informLabel;

    private static Map<Integer, CameraPanel> cameras;
    public static Map<Integer, List<String>> camerasAddress;
    private static Map<Integer, JPanel> cameraBlock;
    public static Map<Integer, BufferedImage> imagesForBlock;
    public static Map<Integer, VideoCreator> creatorMap;

    private static JLabel mainLabel = new JLabel("Головна");
    private JLabel recordLabel;
    private static JLabel recordSecondsLabel;

    public static AddressSaver addressSaver;

    private static int opacitySetting;
    private static int timeToSave = 30;
    private static int percentDiffWhite = 10;
    private static int colorLightNumber = 200;
    private static int colorRGBNumber = new Color(200, 200, 200).getRGB();
    private static boolean programLightCatchWork;
    private static int port;
    private static String path = "C:\\LIGHTNING_STABLE\\";
    private static String defaultPath = "C:\\LIGHTNING_STABLE\\";

    private static String profileName;

    private SoundSaver soundSaver;

    private static int showImagePerSecond = 1;

    private MainFrame() {
        super("LIGHTNING_STABLE");
        imagesForBlock = new HashMap<>();
        cameras = new HashMap<>();
        cameraBlock = new HashMap<>();
        camerasAddress = new HashMap<>();
        creatorMap = new HashMap<>();
//===================================================

        this.getContentPane().setBackground(Color.LIGHT_GRAY);
        this.getContentPane().setLayout(new FlowLayout(1));

        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        mainPanel.setMaximumSize(new Dimension(1120,650));

        northPanel = new JPanel(new FlowLayout());
        northPanel.setPreferredSize(new Dimension(1110, 54));

        centralPanel = new JPanel();
        centralPanel.setBorder(BorderFactory.createEtchedBorder());
        centralPanel.setPreferredSize(new Dimension(1110, 550));//new Dimension(1150, 720)580

        southPanel = new JPanel();
        southPanel.setPreferredSize(new Dimension(1110, 45));//new Dimension(1150, 720)

        mainPanel.add(northPanel,BorderLayout.NORTH);
        mainPanel.add(centralPanel,BorderLayout.CENTER);
        mainPanel.add(southPanel,BorderLayout.SOUTH);
        this.add(mainPanel);

        cameraAddressSetting = CameraAddressSetting.getCameraAddressSetting();
        videoFilesPanel = VideoFilesPanel.getVideoFilesPanel();

        informLabel = new JLabel();
        informLabel.setPreferredSize(new Dimension(170, 30));
        informLabel.setHorizontalAlignment(SwingConstants.CENTER);
        opacityLabel = new JLabel("Прозорість: 30%");
        opacityLabel.setPreferredSize(new Dimension(110, 30));
        showImagesLabel = new JLabel("Транслюемо 1 FPS");
        showImagesLabel.setPreferredSize(new Dimension(120,30));
        countSaveVideo = new JLabel("Зберігаемо " + timeToSave + "сек.");
        countSaveVideo.setPreferredSize(new Dimension(120, 30));
        lightSensitivityLabel = new JLabel("Чутливість: " + colorLightNumber);
        changeWhiteLabel = new JLabel("Збільшення світла: " + percentDiffWhite + "%");
        usedMemoryLabel = new JLabel();
        usedMemoryLabel.setPreferredSize(new Dimension(80, 30));
        maxMemoryLabel = new JLabel();
        maxMemoryLabel.setText("Total:" + maxMemory +" mb");
        alarmServerLabel = new JLabel();
        alarmServerLabel.setPreferredSize(new Dimension(65,30));



        addressSaver = AddressSaver.restorePasswords();
        buildMainWindow();
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(true);
        pack();
//        =============================================
        addressSaver.setPasswordsToFields();
        addressSaver.setSetting();
        cameraAddressSetting.saveAddressToMap();
        setting = Setting.getSetting();
        showAllCameras();

        Thread memoryUpdateThread = new Thread(() -> {
            int playInt = 0;
            int writeLogs = 0;
            boolean red = false;
            boolean startRec = true;

            while (true) {
                if (MainVideoCreator.isSaveVideo()) {
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

                long usedMemory = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576;
                usedMemoryLabel.setText(String.valueOf(usedMemory) + " mb");
                usedMemoryLabel.repaint();

                if(writeLogs>60){
                    log.info("Используем памяти "+usedMemory+ " mb");
                    writeLogs=0;
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

        Thread alarmThread = new Thread(() -> {
            ServerSocket ss = null;
            try {
                ss = new ServerSocket(port);
            } catch (IOException ignored) {}

            while (true) {
                try {
                    audioPacketCount.setForeground(new Color(29, 142, 27));
                    setAlarmServerLabelColor(port,new Color(29, 142, 27));
                    log.info("Ждем сигнал сработки на порт - "+port);
                    Socket socket = ss.accept();
                    log.info("Получили сигнал сработки на порт "+port);
                    MainVideoCreator.startCatchVideo(false);
                    socket.close();
                } catch (Exception e) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                    setAlarmServerLabelColor(port,Color.red);
                    audioPacketCount.setForeground(Color.red);
                }
            }
        });
        alarmThread.setName("Alarm Thread");
        alarmThread.start();

        File fileAddressSaver = new File(defaultPath+"\\data\\");
        fileAddressSaver.mkdirs();

        File fileBuffBytes = new File(path+"\\bytes\\");
        fileBuffBytes.mkdirs();

        for (int i = 1; i < 5; i++) {
            File folder = new File(defaultPath+"\\buff\\" + i + "\\");
            folder.mkdirs();
            File[] files = folder.listFiles();
            for (int j = 0; j < files.length; j++) {
                files[j].delete();
            }
        }

        File pathFolder = new File(path);
        pathFolder.mkdirs();
    }

    static List<String> getRunningThreads() {
        List<String> threads = new ArrayList<>();
        ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
        ThreadGroup parent;
        while ((parent = threadGroup.getParent()) != null) {
            if (threadGroup != null) {
                threadGroup = parent;
                Thread[] threadList = new Thread[threadGroup.activeCount()];
                threadGroup.enumerate(threadList);
                for (Thread thread : threadList)
                    threads.add(new StringBuilder().append(thread.getThreadGroup().getName())
                            .append("::").append(thread.getName()).append("::PRIORITY:-")
                            .append(thread.getPriority()).toString());
            }
        }
        return threads;
    }

    public static MainFrame getMainFrame() {
        if (mainFrame != null) {
            return mainFrame;
        } else {
            mainFrame = new MainFrame();
            return mainFrame;
        }
    }

    private void buildMainWindow() {
        buildNorthPanel();
        buildCentralPanel();
        buildSouthPanel();
    }

    private void buildNorthPanel() {
        audioPacketCount = new JLabel("AUDIO");

        JButton mainWindowButton = new JButton("Головна");
        mainWindowButton.setPreferredSize(new Dimension(120, 30));
        mainWindowButton.addActionListener((e) -> {
            VideoPlayer.setShowVideoPlayer(false);
            for (Integer cameraNumber : cameras.keySet()) {
                CameraPanel cameraPanel = cameras.get(cameraNumber);
                if (cameraPanel.getVideoCatcher().isFullSize()) {
                    cameraPanel.getVideoCatcher().setWidthAndHeight(270, 158);
                    cameraPanel.revalidate();

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
            setCentralPanel(allCameraPanel);
            mainLabel.setText("Головна");
        });

        JButton cameraButton = new JButton("Камери");
        cameraButton.setPreferredSize(new Dimension(120, 30));
        cameraButton.addActionListener((e) -> {
            VideoPlayer.setShowVideoPlayer(false);
            setCentralPanel(cameraAddressSetting);
            mainLabel.setText("Камери");
        });

        JButton videoButton = new JButton("Відео");
        videoButton.setPreferredSize(new Dimension(120, 30));
        videoButton.addActionListener((e) -> {
            showVideosWindow();
        });
        JButton settingButton = new JButton("Налаштування");
        settingButton.setPreferredSize(new Dimension(120, 30));
        settingButton.addActionListener((e) -> {
            VideoPlayer.setShowVideoPlayer(false);
            setting.saveButton.setForeground(Color.BLACK);
            setting.saveButton.setText("Зберегти");
            setting.setPassword();
            setCentralPanel(setting);
            mainLabel.setText("Налаштування");
        });

        mainLabel.setFont(new Font(null, Font.BOLD, 15));
        mainLabel.setPreferredSize(new Dimension(120, 30));

        JPanel informPane = new JPanel(new FlowLayout());
//        recordLabel = new JLabel(String.valueOf((char) 8226));
        recordLabel = new JLabel(String.valueOf((char) 8623));
        recordLabel.setFont(new Font(null, Font.BOLD, 23));
        recordLabel.setForeground(Color.DARK_GRAY);
        informPane.add(mainLabel);
        informPane.add(recordLabel);
        recordSecondsLabel = new JLabel();
        recordSecondsLabel.setPreferredSize(new Dimension(150,30));
        recordSecondsLabel.setHorizontalAlignment(SwingConstants.CENTER);

        startButton = new JButton("REC");
        startButton.setVisible(false);
        startButton.addActionListener((e -> {
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
        northPanel.add(startButton);
        northPanel.add(startButtonProgrammingCatch);

        northPanel.add(recordSecondsLabel);
        northPanel.add(informPane);
    }

    public void showVideosWindow(){
        VideoPlayer.setShowVideoPlayer(false);
        videoFilesPanel.showVideos();
        setCentralPanel(videoFilesPanel);
        mainLabel.setText("Відео");
    }

    private void buildCentralPanel() {

        centralPanel.setLayout(new FlowLayout());
        GridLayout gridLayout = new GridLayout(2, 2, 2, 2);
        allCameraPanel = new JPanel();
        allCameraPanel.setLayout(gridLayout);

        for (int i = 1; i < 5; i++) {
            JPanel blockPanel;
            VideoCreator videoCreator = new VideoCreator(i);
            videoCreator.setBufferedImageBack(imagesForBlock.get(i));
            creatorMap.put(i, videoCreator);
            CameraPanel cameraOne = new CameraPanel(videoCreator, (i * 2 - 1));
            cameraOne.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {

                        if (cameraOne.getVideoCatcher().isFullSize()) {
                            for (Integer cameraNumber : cameras.keySet()) {
                                CameraPanel cameraPanel = cameras.get(cameraNumber);
                                if (cameraPanel.getVideoCatcher().isFullSize()) {
                                    cameraPanel.getVideoCatcher().setWidthAndHeight(270, 158);

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
                            mainLabel.setText("Головна");
                        } else {
                            cameraOne.getVideoCatcher().setWidthAndHeight(918, 540);
                            setCentralPanel(cameraOne);
                        }
                    }
                }
            });
            cameras.put(i * 2 - 1, cameraOne);

            CameraPanel cameraTwo = new CameraPanel(videoCreator, (i * 2));
            cameraTwo.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {

                        if (cameraTwo.getVideoCatcher().isFullSize()) {
                            for (Integer cameraNumber : cameras.keySet()) {
                                CameraPanel cameraPanel = cameras.get(cameraNumber);
                                if (cameraPanel.getVideoCatcher().isFullSize()) {
                                    cameraPanel.getVideoCatcher().setWidthAndHeight(270, 158);
                                    cameraPanel.revalidate();
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
                            mainLabel.setText("Головна");
                        } else {
                            cameraTwo.getVideoCatcher().setWidthAndHeight(918, 540);
                            setCentralPanel(cameraTwo);
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
        southPanel.add(alarmServerLabel);
        southPanel.add(countSaveVideo);
        southPanel.add(opacityLabel);
        southPanel.add(showImagesLabel);
        southPanel.add(lightSensitivityLabel);
        southPanel.add(changeWhiteLabel);
        southPanel.add(informLabel);
        southPanel.add(new JLabel("Використано: "));
        southPanel.add(usedMemoryLabel);
        southPanel.add(maxMemoryLabel);
    }

    public static void removeImageForBlock(int number) {
        imagesForBlock.remove(number);
        CameraPanel cameraPanel = cameras.get(number * 2 - 1);
        cameraPanel.repaintCameraWindow();
        CameraPanel cameraPanel1 = cameras.get(number * 2);
        cameraPanel1.repaintCameraWindow();
    }

    public static void showInformMassage(String massage, Color color) {
        informLabel.setText(massage);
        informLabel.setForeground(color);
        informLabel.repaint();
    }
    public static void showSecondsAlreadySaved(String massage){
        recordSecondsLabel.setText(massage);
        recordSecondsLabel.repaint();
    }

    public static void addImage(BufferedImage image, int numberGroup) {
        imagesForBlock.put(numberGroup, image);
    }

    public void showAllCameras() {
        for (Integer addressNumber : camerasAddress.keySet()) {
            if (addressNumber == null) {
                continue;
            }
            List<String> list = camerasAddress.get(addressNumber);
            if (list != null) {
                log.info("Камера - " + addressNumber+ " будет запущена.");
                URL url = null;
                try {
                    url = new URL(list.get(0) + "&streamprofile="+profileName);//TODO fgfvdafxvarfd
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
                log.info("Камера - " + addressNumber+ " НЕ будет запущена.");
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

    public void setCentralPanel(JPanel panel) {
        centralPanel.removeAll();
        centralPanel.add(panel);
        centralPanel.validate();
        centralPanel.repaint();
    }

    public static void setOpacitySetting(int opacity) {
        MainFrame.opacitySetting = opacity;
        opacityLabel.setText("Прозорість: " + opacity + "%");
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

    public void setCountSaveVideo(int countSave) {
        timeToSave = countSave;
        countSaveVideo.setText("Зберігаемо " + countSave + " сек.");
        countSaveVideo.repaint();
    }

    public static int getPercentDiffWhite() {
        return percentDiffWhite;
    }

    public static void setPercentDiffWhite(int percentDiffWhite) {
        MainFrame.percentDiffWhite = percentDiffWhite;
        changeWhiteLabel.setText("Збільшення світла: " + percentDiffWhite + "%");
        changeWhiteLabel.repaint();
    }

    private void setAlarmServerLabelColor(int port, Color color){
        alarmServerLabel.setText("PORT:"+port);
        alarmServerLabel.setForeground(color);
        alarmServerLabel.repaint();
    }

    public SoundSaver getSoundSaver() {
        return soundSaver;
    }

    public static int getColorLightNumber() {
        return colorLightNumber;
    }

    public static void setColorLightNumber(int colorLightNumber) {
        MainFrame.colorLightNumber = colorLightNumber;
        lightSensitivityLabel.setText("Чутливість: " + colorLightNumber);
        lightSensitivityLabel.repaint();
        MainFrame.colorRGBNumber = new Color(colorLightNumber, colorLightNumber, colorLightNumber).getRGB();
    }

    public static int getColorRGBNumber() {
        return colorRGBNumber;
    }

    public static boolean isProgramLightCatchWork() {
        return programLightCatchWork;
    }

    public static void setProgramLightCatchWork(boolean programLightCatchWork) {
        MainFrame.programLightCatchWork = programLightCatchWork;
        if (programLightCatchWork) {
            lightSensitivityLabel.setForeground(new Color(23, 114, 26));
            changeWhiteLabel.setForeground(new Color(23, 114, 26));
        } else {
            lightSensitivityLabel.setForeground(Color.LIGHT_GRAY);
            changeWhiteLabel.setForeground(Color.LIGHT_GRAY);
        }

        lightSensitivityLabel.repaint();
        changeWhiteLabel.repaint();
    }

    public static int getTimeToSave() {
        return timeToSave;
    }

    public static void setTimeToSave(int timeToSave) {
        MainFrame.timeToSave = timeToSave;
        countSaveVideo.setText("Зберігаемо " + timeToSave + "сек.");
    }

    public static void setPort(int port) {
        MainFrame.port = port;
    }

    public static void setPath(String path) {
        MainFrame.path = path;
    }

    public static void setProfileName(String profileName) {
        MainFrame.profileName = profileName;
    }

    public static void setMaxMemory(int maxMemory) {
        MainFrame.maxMemory = maxMemory;
    }

    public static int getPort() {
        return port;
    }

    public static String getPath() {
        return path;
    }

    public static String getProfileName() {
        return profileName;
    }

    public static void setTestMode(boolean testMode) {
        testModeLabel.setVisible(!testMode);
        startButtonProgrammingCatch.setPreferredSize(new Dimension(80, 25));
        startButton.setPreferredSize(new Dimension(80, 25));
        startButton.setVisible(testMode);
        startButtonProgrammingCatch.setVisible(testMode);
    }

    public static String getDefaultPath() {
        return defaultPath;
    }

    public static int getShowImagePerSecond() {
        return showImagePerSecond;
    }

    public static void setShowImagePerSecond(int showImagePerSecond) {
        showImagesLabel.setText("Транслюемо "+showImagePerSecond+" FPS");
        showImagesLabel.repaint();
        MainFrame.showImagePerSecond = showImagePerSecond;
    }
}










