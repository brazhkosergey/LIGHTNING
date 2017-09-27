package ui.main;

import entity.AddressSaver;
import entity.MainVideoCreator;
import entity.VideoPlayer;
import ui.camera.CameraPanel;
import ui.camera.VideoCreator;
import ui.setting.CameraAddressSetting;
import ui.setting.Setting;
import ui.video.VideoFilesPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainFrame extends JFrame {
    private static MainFrame mainFrame;
    private static JPanel mainPanel;
    private static JPanel northPanel;
    private static JPanel centralPanel;
    private static JPanel southPanel;

    private static CameraAddressSetting cameraAddressSetting;
    private static Setting setting;
    private static VideoFilesPanel videoFilesPanel;

    private JPanel allCameraPanel;

    private static JLabel opacityLabel;
    private static JLabel qualityVideoLabel;
    private static JLabel countSaveVideo;
    private static JLabel lightSensitivityLabel;
    private static JLabel changeWhiteLabel;

    private static JLabel informLabel;
    private JLabel usedMemoryLabel;

    private static Map<Integer, CameraPanel> cameras;
    public static Map<Integer, List<String>> camerasAddress;
    private static Map<Integer, JPanel> cameraBlock;
    public static Map<Integer, BufferedImage> imagesForBlock;
    public static Map<Integer, VideoCreator> creatorMap;

    private static JLabel mainLabel = new JLabel("Головна");
    private JLabel recordLabel;

    public static AddressSaver addressSaver;

    private static int opacitySetting;
    private static int timeToSave = 30;
    private static int percentDiffWhite = 10;
    private static int colorLightNumber = 200;
    private static int colorRGBNumber = new Color(200,200,200).getRGB();
    private static int doNotShowImages = 5;
    private static boolean programLightCatchWork;

    private MainFrame() {
        super("LIGHTNING");

        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel,BoxLayout.Y_AXIS));
        northPanel = new JPanel();
        centralPanel = new JPanel();
        southPanel = new JPanel();

        imagesForBlock = new HashMap<>();
        cameras = new HashMap<>();
        cameraBlock = new HashMap<>();
        camerasAddress = new HashMap<>();
        creatorMap = new HashMap<>();

        cameraAddressSetting = CameraAddressSetting.getCameraAddressSetting();
        videoFilesPanel = VideoFilesPanel.getVideoFilesPanel();

        informLabel = new JLabel();
        informLabel.setPreferredSize(new Dimension(200, 30));

        opacityLabel = new JLabel("Прозорість: 30%");
        opacityLabel.setPreferredSize(new Dimension(110, 30));
        qualityVideoLabel = new JLabel("Траслюемо: 20%");
        qualityVideoLabel.setPreferredSize(new Dimension(120, 30));
        countSaveVideo = new JLabel("Зберігаемо "+timeToSave+"сек.");
        countSaveVideo.setPreferredSize(new Dimension(120,30));

        lightSensitivityLabel = new JLabel("Чутливість: "+colorLightNumber);
        changeWhiteLabel = new JLabel("Збільшення світла: "+percentDiffWhite+"%");

        usedMemoryLabel = new JLabel();
        usedMemoryLabel.setPreferredSize(new Dimension(100, 30));
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(1150, 720));
        addressSaver = AddressSaver.restorePasswords();
        buildMainWindow();

        getContentPane().add(mainPanel, BorderLayout.CENTER);
        setVisible(true);
        pack();

        addressSaver.setPasswordsToFields();
        addressSaver.setSetting();
        cameraAddressSetting.saveAddressToMap();
        setting = Setting.getSetting();
        showAllCameras();

        File fileTmp = new File("C:\\ipCamera\\bytes\\");
        fileTmp.mkdirs();
        File file = new File("C:\\ipCamera\\");
        file.mkdirs();
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
        JButton mainWindowButton = new JButton("Головна");
        mainWindowButton.setPreferredSize(new Dimension(120, 30));
        mainWindowButton.addActionListener((e) -> {
            VideoPlayer.setShowVideoPlayer(false);
            for (Integer cameraNumber : cameras.keySet()) {
                CameraPanel cameraPanel = cameras.get(cameraNumber);
                if (cameraPanel.getVideoCatcher().isFullSize()) {
                    cameraPanel.getVideoCatcher().setWidthAndHeight(270, 260);
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
                        blockPanel.repaint();
                    }
                }
            }
            centralPanel.removeAll();
            centralPanel.add(allCameraPanel);
            centralPanel.repaint();
            mainLabel.setText("Головна");
        });

        JButton cameraButton = new JButton("Камери");
        cameraButton.setPreferredSize(new Dimension(120, 30));
        cameraButton.addActionListener((e) -> {
            VideoPlayer.setShowVideoPlayer(false);
            centralPanel.removeAll();
            centralPanel.add(cameraAddressSetting);
            centralPanel.repaint();
            mainLabel.setText("Камери");
        });

        JButton videoButton = new JButton("Відео");
        videoButton.setPreferredSize(new Dimension(120, 30));
        videoButton.addActionListener((e) -> {
            VideoPlayer.setShowVideoPlayer(false);

            centralPanel.removeAll();
            videoFilesPanel.showVideos();
            centralPanel.add(videoFilesPanel);
            centralPanel.repaint();
            mainLabel.setText("Відео");
        });
        JButton settingButton = new JButton("Налаштування");
        settingButton.setPreferredSize(new Dimension(120, 30));
        settingButton.addActionListener((e) -> {
            VideoPlayer.setShowVideoPlayer(false);
            centralPanel.removeAll();
            setting.saveButton.setForeground(Color.BLACK);
            setting.saveButton.setText("Зберегти");
            centralPanel.add(setting);
            centralPanel.repaint();
            mainLabel.setText("Налаштування");
        });

        mainLabel.setFont(new Font("Comic Sans MS", Font.BOLD, 15));
        mainLabel.setPreferredSize(new Dimension(200, 30));

        JPanel informPane = new JPanel(new FlowLayout());
        recordLabel = new JLabel(String.valueOf((char) 8226));
        recordLabel.setFont(new Font("Comic Sans MS", Font.BOLD, 45));
        recordLabel.setForeground(Color.DARK_GRAY);
        informPane.add(mainLabel);
        informPane.add(recordLabel);

        JButton startButton = new JButton("REC");
        startButton.addActionListener((e -> {
            MainVideoCreator.startCatchVideo(false);
        }));

        JButton startButtonProgrammingCatch = new JButton("REC PR");
        startButtonProgrammingCatch.addActionListener((e -> {
            MainVideoCreator.startCatchVideo(true);
        }));

        northPanel.add(mainWindowButton);
        northPanel.add(cameraButton);
        northPanel.add(videoButton);
        northPanel.add(settingButton);
        northPanel.add(startButton);
        northPanel.add(startButtonProgrammingCatch);
        northPanel.add(Box.createHorizontalStrut(100));
        northPanel.add(informPane);
        mainPanel.add(northPanel);

        Thread thread = new Thread(() -> {
            int playInt = 0;
            boolean red = false;
            boolean startRec = true;
            while (true) {
                if (MainVideoCreator.isSaveVideo()) {
                    if (startRec) {
                        mainLabel.setText("Йде запис відео");
                        startRec = false;
                    }
                    if (red) {
                        recordLabel.setForeground(Color.DARK_GRAY);
                        mainLabel.setForeground(Color.DARK_GRAY);
                        mainLabel.repaint();
                        recordLabel.repaint();
                        red = false;
                    } else {
                        recordLabel.setForeground(Color.RED);
                        mainLabel.setForeground(Color.RED);
                        mainLabel.repaint();
                        recordLabel.repaint();
                        red = true;
                    }
                } else {
                    if (red || !startRec) {
                        mainLabel.setText("Запис закінчено");
                        mainLabel.setForeground(Color.DARK_GRAY);
                        mainLabel.repaint();
                        recordLabel.setForeground(Color.DARK_GRAY);
                        recordLabel.repaint();
                        red = false;
                        startRec = true;
                    }
                }


                long usedMemory = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576;



//                long heapSize = Runtime.getRuntime().totalMemory()/1048576;

//                usedMemoryLabel.setText(String.valueOf(heapSize) + " mb");
                usedMemoryLabel.setText(String.valueOf(usedMemory) + " mb");
                usedMemoryLabel.repaint();

                if (VideoPlayer.isShowVideoPlayer()) {
                    try{
                        if (playInt == 0) {
                            VideoPlayer.informLabel.setForeground(Color.RED);
                            VideoPlayer.informLabel.repaint();
                            playInt++;
                        } else {
                            playInt = 0;
                            VideoPlayer.informLabel.setForeground(Color.LIGHT_GRAY);
                            VideoPlayer.informLabel.repaint();
                        }
                    } catch (NullPointerException e){
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
        thread.start();
    }

    private void buildCentralPanel() {
        centralPanel.setLayout(new FlowLayout());
        GridLayout gridLayout = new GridLayout(2,2,2,2);
        allCameraPanel = new JPanel();
        allCameraPanel.setLayout(gridLayout);

        for (int i = 1; i < 5; i++) {
            JPanel blockPanel;
            VideoCreator videoCreator = new VideoCreator(i);
            videoCreator.setBufferedImageBack(imagesForBlock.get(i));
            creatorMap.put(i, videoCreator);
            CameraPanel cameraOne = new CameraPanel(videoCreator);
            cameraOne.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {

                        if(cameraOne.getVideoCatcher().isFullSize()){
                            for (Integer cameraNumber : cameras.keySet()) {
                                CameraPanel cameraPanel = cameras.get(cameraNumber);
                                if (cameraPanel.getVideoCatcher().isFullSize()) {
                                    cameraPanel.getVideoCatcher().setWidthAndHeight(270, 260);
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
                                        blockPanel.repaint();
                                    }
                                }
                            }
                            centralPanel.removeAll();
                            centralPanel.add(allCameraPanel);
                            centralPanel.repaint();
                            mainLabel.setText("Головна");
                        } else {
                            cameraOne.getVideoCatcher().setWidthAndHeight(1100, 540);
                            cameraOne.revalidate();
                            centralPanel.removeAll();
                            centralPanel.add(cameraOne);
                            centralPanel.repaint();
                        }
                    }
                }
            });

            cameraOne.setCameraNumber(i * 2 - 1);
            cameras.put(i * 2 - 1, cameraOne);
            CameraPanel cameraTwo = new CameraPanel(videoCreator);
            cameraTwo.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {

                        if(cameraTwo.getVideoCatcher().isFullSize()){
                            for (Integer cameraNumber : cameras.keySet()) {
                                CameraPanel cameraPanel = cameras.get(cameraNumber);
                                if (cameraPanel.getVideoCatcher().isFullSize()) {
                                    cameraPanel.getVideoCatcher().setWidthAndHeight(270, 260);
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
                                        blockPanel.repaint();
                                    }
                                }
                            }
                            centralPanel.removeAll();
                            centralPanel.add(allCameraPanel);
                            centralPanel.repaint();
                            mainLabel.setText("Головна");
                        } else {
                            cameraTwo.getVideoCatcher().setWidthAndHeight(1100, 540);
                            cameraTwo.revalidate();
                            centralPanel.removeAll();
                            centralPanel.add(cameraTwo);
                            centralPanel.repaint();
                        }
                    }
                }
            });

            cameraTwo.setCameraNumber(i * 2);
            cameras.put(i * 2, cameraTwo);
            blockPanel = new JPanel();
            GridLayout gridLayout1=new GridLayout(1,2);
            blockPanel.setLayout(gridLayout1);
            blockPanel.add(cameraOne);
            blockPanel.add(cameraTwo);
            blockPanel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
            cameraBlock.put(i, blockPanel);
            allCameraPanel.add(blockPanel);
        }
        centralPanel.add(allCameraPanel);
        mainPanel.add(centralPanel);
    }

    private void buildSouthPanel() {
        southPanel.setLayout(new FlowLayout());
        southPanel.add(countSaveVideo);
        southPanel.add(Box.createRigidArea(new Dimension(10, 10)));
        southPanel.add(qualityVideoLabel);
        southPanel.add(Box.createRigidArea(new Dimension(10, 10)));
        southPanel.add(opacityLabel);
        southPanel.add(Box.createRigidArea(new Dimension(10, 10)));
        southPanel.add(lightSensitivityLabel);
        southPanel.add(Box.createRigidArea(new Dimension(10, 10)));
        southPanel.add(changeWhiteLabel);
        southPanel.add(Box.createRigidArea(new Dimension(20, 10)));
        southPanel.add(informLabel);
        southPanel.add(Box.createRigidArea(new Dimension(10, 10)));
        southPanel.add(new JLabel("Використано: "));
        southPanel.add(usedMemoryLabel);
        mainPanel.add(southPanel);
    }

    public static Map<Integer, CameraPanel> getCameras() {
        return cameras;
    }

    public static void removeImageForBlock(int number){
        imagesForBlock.remove(number);
        CameraPanel cameraPanel = cameras.get(number * 2 - 1);
        cameraPanel.repaintCameraWindow();
        CameraPanel cameraPanel1 = cameras.get(number * 2);
        cameraPanel1.repaintCameraWindow();
    }

    public static void showInformMassage(String massage, boolean green) {
        informLabel.setText(massage);
        if (green) {
            informLabel.setForeground(new Color(29, 142, 27));
        } else {
            informLabel.setForeground(Color.DARK_GRAY);
        }
        informLabel.repaint();
    }

    public static void addImage(BufferedImage image, int numberGroup) {
        imagesForBlock.put(numberGroup, image);
    }

    public void showAllCameras() {
        for (Integer addressNumber : camerasAddress.keySet()) {
            List<String> list = camerasAddress.get(addressNumber);
            if (list != null) {
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
                if (url != null) {
                    cameras.get(addressNumber).getVideoCatcher().startCatchVideo(url);
                }
            } else {
                cameras.get(addressNumber).getVideoCatcher().stopCatchVideo();
            }
        }
    }

    public void setCentralPanel(JPanel panel) {
        centralPanel.removeAll();
        centralPanel.add(panel);
        centralPanel.revalidate();
        centralPanel.repaint();
    }

    public static void setOpacitySetting(int opacity){
        MainFrame.opacitySetting = opacity;
        opacityLabel.setText("Прозорість: "+opacity+"%");
        opacityLabel.repaint();
        Float f = (float) opacity / 100;
        System.out.println(" Прозрачность равна - " + f);
        CameraPanel.setOpacity(f);

        for(Integer integer: imagesForBlock.keySet()){
            CameraPanel cameraPanel = cameras.get(integer);
            if(cameraPanel.getVideoCatcher().isCatchVideo()){
                cameraPanel.showCopyImage();
            }
        }
    }

    public static int getOpacitySetting() {
        return opacitySetting;
    }

    public static void setQualityVideoLabel(int qualityVideo){
        doNotShowImages = qualityVideo;

        int s = 100;
        if (qualityVideo != 0) {
            s = 100 / qualityVideo;
        }
        qualityVideoLabel.setText("Траслюемо: "+s+"%");
        qualityVideoLabel.repaint();
    }

    public void setCountSaveVideo( int countSave){
        timeToSave = countSave;
        countSaveVideo.setText("Зберігаемо "+countSave+" сек.");
        countSaveVideo.repaint();
    }

    public static int getPercentDiffWhite() {
        return percentDiffWhite;
    }

    public static void setPercentDiffWhite(int percentDiffWhite) {
        MainFrame.percentDiffWhite = percentDiffWhite;
        changeWhiteLabel.setText("Збільшення світла: "+percentDiffWhite+"%");
        changeWhiteLabel.repaint();
    }

    public static int getColorLightNumber() {
        return colorLightNumber;
    }

    public static void setColorLightNumber(int colorLightNumber) {
        MainFrame.colorLightNumber = colorLightNumber;
        lightSensitivityLabel.setText("Чутливість: "+colorLightNumber);
        lightSensitivityLabel.repaint();
        MainFrame.colorRGBNumber = new Color(colorLightNumber,colorLightNumber,colorLightNumber).getRGB();
    }

    public static int getColorRGBNumber() {
        return colorRGBNumber;
    }

    public static boolean isProgramLightCatchWork() {
        return programLightCatchWork;
    }

    public static void setProgramLightCatchWork(boolean programLightCatchWork) {
        MainFrame.programLightCatchWork = programLightCatchWork;
        if(programLightCatchWork){
            lightSensitivityLabel.setForeground(new Color(23, 114, 26));
            changeWhiteLabel.setForeground(new Color(23, 114, 26));
        } else {
            lightSensitivityLabel.setForeground(Color.LIGHT_GRAY);
            changeWhiteLabel.setForeground(Color.LIGHT_GRAY);
        }

        lightSensitivityLabel.repaint();
        changeWhiteLabel.repaint();
    }

    public static int getDoNotShowImages() {
        return doNotShowImages;
    }

    public static int getTimeToSave() {
        return timeToSave;
    }

    public static void setTimeToSave(int timeToSave) {
        MainFrame.timeToSave = timeToSave;
        countSaveVideo.setText("Зберігаемо "+timeToSave+"сек.");
    }
}










