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

    private JLabel opacityLabel;
    private JLabel qualityVideoLabel;
    private JLabel countSaveVideo;
    private JLabel usedMemoryLabel;

    private static JLabel informLabel;


    private static Map<Integer, CameraPanel> cameras;
    public static Map<Integer, List<String>> camerasAddress;
    private static Map<Integer, JPanel> cameraBlock;
    public static Map<Integer, BufferedImage> imagesForBlock;
    public static Map<Integer, VideoCreator> creatorMap;

    public static int opacity;

    private static JLabel mainLabel = new JLabel("Головна");
    JLabel recordLabel;

    public static AddressSaver addressSaver;
    public static int timeToSave = 30;
    public static boolean programWork = false;

    private MainFrame() {
        super("LIGHTNING");

        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel,BoxLayout.Y_AXIS));
        northPanel = new JPanel();
//        northPanel.setPreferredSize(new Dimension(1100,50));
        centralPanel = new JPanel();
        southPanel = new JPanel();

        opacity = 30;
        imagesForBlock = new HashMap<>();
        addressSaver = AddressSaver.restorePasswords();
        cameras = new HashMap<>();
        cameraBlock = new HashMap<>();
        camerasAddress = new HashMap<>();
        creatorMap = new HashMap<>();


        cameraAddressSetting = CameraAddressSetting.getCameraAddressSetting();
        setting = Setting.getSetting();
        videoFilesPanel = VideoFilesPanel.getVideoFilesPanel();

        informLabel = new JLabel("INFORM");
        informLabel.setPreferredSize(new Dimension(250, 30));
        opacityLabel = new JLabel("Прозорість: 30%");
        opacityLabel.setPreferredSize(new Dimension(100, 30));
        qualityVideoLabel = new JLabel("Траслюемо відео: 20%");
        qualityVideoLabel.setPreferredSize(new Dimension(150, 30));
        countSaveVideo = new JLabel("Зберігаемо "+timeToSave+" секунд");
        countSaveVideo.setPreferredSize(new Dimension(150,30));

        usedMemoryLabel = new JLabel();
        usedMemoryLabel.setPreferredSize(new Dimension(100, 30));
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(1150, 720));
        buildMainWindow();
        getContentPane().add(mainPanel, BorderLayout.CENTER);
        setVisible(true);
        pack();

        cameraAddressSetting.restoreAddresses();
        setting.setSetting();
        cameraAddressSetting.saveAddressToMap();
        showAllCameras();

        File fileTmp = new File("C:\\ipCamera\\bytes\\");
        fileTmp.mkdirs();
        File file = new File("C:\\ipCamera\\");
        file.mkdirs();
        setCountSaveVideo(timeToSave);
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
            cameraAddressSetting.restoreAddresses();
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
            setting.setSetting();
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
//        ====================================
        JButton startButton = new JButton("REC");
        startButton.addActionListener((e -> {
            VideoPlayer.setShowVideoPlayer(false);
            MainVideoCreator.startCatchVideo();
        }));

        northPanel.add(mainWindowButton);
        northPanel.add(cameraButton);
        northPanel.add(videoButton);
        northPanel.add(settingButton);
        northPanel.add(startButton);
        northPanel.add(Box.createHorizontalStrut(150));
        northPanel.add(informPane);

//        mainPanel.add(northPanel, BorderLayout.NORTH);
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

                usedMemoryLabel.setText(String.valueOf(usedMemory) + " mb");
                usedMemoryLabel.repaint();

                if (VideoPlayer.isShowVideoPlayer()) {
                    if (playInt == 0) {
                        VideoPlayer.informLabel.setForeground(Color.RED);
                        VideoPlayer.informLabel.repaint();
                        playInt++;
                    } else {
                        playInt = 0;
                        VideoPlayer.informLabel.setForeground(Color.LIGHT_GRAY);
                        VideoPlayer.informLabel.repaint();
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
//        allCameraPanel.setPreferredSize(new Dimension(1110, 650));

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
                        cameraOne.getVideoCatcher().setWidthAndHeight(1100, 540);
                        cameraOne.revalidate();
                        centralPanel.removeAll();
                        centralPanel.add(cameraOne);
                        centralPanel.repaint();
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
                        cameraTwo.getVideoCatcher().setWidthAndHeight(1100, 540);
                        cameraTwo.revalidate();
                        centralPanel.removeAll();
                        centralPanel.add(cameraTwo);
                        centralPanel.repaint();
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
        southPanel.add(Box.createRigidArea(new Dimension(50, 30)));
        southPanel.add(countSaveVideo);
        southPanel.add(Box.createRigidArea(new Dimension(10, 30)));
        southPanel.add(qualityVideoLabel);
        southPanel.add(Box.createRigidArea(new Dimension(10, 30)));
        southPanel.add(opacityLabel);
        southPanel.add(Box.createRigidArea(new Dimension(100, 30)));
        southPanel.add(new JLabel("Використано - "));
        southPanel.add(Box.createRigidArea(new Dimension(10, 20)));
        southPanel.add(usedMemoryLabel);
        southPanel.add(informLabel);
//        mainPanel.add(southPanel, BorderLayout.SOUTH);
        mainPanel.add(southPanel);
    }

    public static Map<Integer, CameraPanel> getCameras() {
        return cameras;
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

    public static JPanel getCentralPanel() {
        return centralPanel;
    }

    public void setOpacityLabel(int opacity){
        opacityLabel.setText("Прозорість: "+opacity+"%");
        opacityLabel.repaint();
    }

    public void setQualityVideoLabel( int qualityVideo){
        qualityVideoLabel.setText("Траслюемо відео: "+qualityVideo+"%");
        qualityVideoLabel.repaint();
    }

    public void setCountSaveVideo( int countSave){
        countSaveVideo.setText("Зберігаемо "+countSave+" секунд");
        countSaveVideo.repaint();
    }
}










