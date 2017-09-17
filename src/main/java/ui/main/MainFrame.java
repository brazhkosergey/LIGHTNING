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
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainFrame extends JFrame {
    private static MainFrame mainFrame;
    private static JPanel mainPanel = new JPanel();
    private static JPanel northPanel = new JPanel();
    private static JPanel centralPanel = new JPanel();
    private static JPanel southPanel = new JPanel();


    private static CameraAddressSetting cameraAddressSetting = CameraAddressSetting.getCameraAddressSetting();
    private static Setting setting = Setting.getSetting();

    private static VideoFilesPanel videoFilesPanel = VideoFilesPanel.getVideoFilesPanel();

    private JPanel allCameraPanel;
    private JLabel messageLabel;

    public static Map<Integer, CameraPanel> cameras;
    public static Map<Integer, List<String>> camerasAddress;
    private static Map<Integer, JPanel> cameraBlock;

    public static JLabel mainLabel = new JLabel("Головна");
    JLabel recordLabel;


    public static AddressSaver addressSaver;
    public static int timeToSave = 30;
    public static boolean programWork = false;

    private MainFrame() {
        super("LIGHTNING");
        addressSaver = AddressSaver.restorePasswords();
        cameras = new HashMap<>();
        cameraBlock=new HashMap<>();
        camerasAddress = new HashMap<>();
        mainPanel.setLayout(new BorderLayout());
        messageLabel = new JLabel();
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
                    cameraPanel.getVideoCatcher().setWidthAndHeight(245, 220);
                    cameraPanel.setPreferredSize(new Dimension(260, 230));
                    cameraPanel.revalidate();

                    int blockNumber = (cameraNumber + 1) / 2;
                    JPanel blockPanel = cameraBlock.get(blockNumber);
                    if(cameraNumber%2==0){
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
        videoButton.addActionListener((e)->{
            VideoPlayer.setShowVideoPlayer(false);

            centralPanel.removeAll();
            videoFilesPanel.showVideos();
            centralPanel.add(videoFilesPanel);
            centralPanel.repaint();
            mainLabel.setText("Відео");
        });
        JButton settingButton = new JButton("Налаштування");
        settingButton.setPreferredSize(new Dimension(120, 30));
        settingButton.addActionListener((e)->{
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

            MainVideoCreator.startCatchVideo(new Date(System.currentTimeMillis()));
        }));

        northPanel.add(mainWindowButton);
        northPanel.add(cameraButton);
        northPanel.add(videoButton);
        northPanel.add(settingButton);
        northPanel.add(startButton);
        northPanel.add(Box.createHorizontalStrut(150));

//        northPanel.add(mainLabel);
        northPanel.add(informPane);
        mainPanel.add(northPanel, BorderLayout.NORTH);

        Thread thread = new Thread(() -> {
            boolean red = false;
            boolean startRec = true;
            while (true){
                if(MainVideoCreator.isSaveVideo()){
                    if(startRec){
                        mainLabel.setText("Йде запис відео");
                        startRec = false;
                    }
                    if(red){
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
                    if(red||!startRec){
                        mainLabel.setText("Запис закінчено");
                        mainLabel.setForeground(Color.DARK_GRAY);
                        mainLabel.repaint();
                        recordLabel.setForeground(Color.DARK_GRAY);
                        recordLabel.repaint();
                        red = false;
                        startRec = true;
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
        allCameraPanel = new JPanel(new FlowLayout());
        allCameraPanel.setPreferredSize(new Dimension(1110, 650));

        for (int i = 1; i < 5; i++) {
            JPanel blockPanel;
            VideoCreator videoCreator = new VideoCreator(i);
            CameraPanel cameraOne = new CameraPanel(videoCreator);
            cameraOne.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        cameraOne.getVideoCatcher().setWidthAndHeight(1000, 570);
                        cameraOne.setPreferredSize(new Dimension(1050, 580));
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
                        cameraTwo.getVideoCatcher().setWidthAndHeight(1000, 600);
                        cameraTwo.setPreferredSize(new Dimension(900, 600));
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
            blockPanel.setPreferredSize(new Dimension(550, 270));
            blockPanel.setLayout(new FlowLayout());
            blockPanel.add(cameraOne);
            blockPanel.add(cameraTwo);
            TitledBorder titleMainSetting = BorderFactory.createTitledBorder("Блок камер - " + i);
            titleMainSetting.setTitleJustification(TitledBorder.CENTER);
            titleMainSetting.setTitleFont((new Font("Comic Sans MS", Font.BOLD, 17)));
            titleMainSetting.setTitleColor(new Color(46, 139, 87));
            titleMainSetting.setBorder(new LineBorder(new Color(46, 139, 87), 3, true));
            blockPanel.setBorder(titleMainSetting);

            cameraBlock.put(i,blockPanel);
            allCameraPanel.add(blockPanel);
        }
        centralPanel.add(allCameraPanel);
        mainPanel.add(centralPanel, BorderLayout.CENTER);
    }

    private void buildSouthPanel() {
//
//
//
//        JButton endButton = new JButton("закінчити запис");
//        endButton.addActionListener((e -> {
//            MainVideoCreator.stopCatchVideo();
//        }));
//
//        southPanel.setLayout(new FlowLayout());
////        southPanel.add(startButton);
//        southPanel.add(Box.createRigidArea(new Dimension(100,30)));
//        southPanel.add(endButton);
//        southPanel.add(Box.createRigidArea(new Dimension(20, 20)));
//        southPanel.add(messageLabel);
//        mainPanel.add(southPanel, BorderLayout.SOUTH);
    }

    public void showMessage(String text){
        messageLabel.setText(text);
        messageLabel.repaint();
    }

    public void showAllCameras(){
        for(Integer addressNumber:camerasAddress.keySet()){
            if(!cameras.get(addressNumber).getVideoCatcher().isCatchVideo()){
                List<String> list = camerasAddress.get(addressNumber);
                URL url = null;
                try {
                    url = new URL(list.get(0));
                    Authenticator.setDefault(new Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(list.get(1),list.get(2).toCharArray());
                        }
                    });
                } catch (MalformedURLException ex) {
                    ex.printStackTrace();
                }
                if(url!=null){
                    cameras.get(addressNumber).getVideoCatcher().startCatchVideo(url);
                }
            }
        }
    }

    public void setCentralPanel(JPanel panel){
        centralPanel.removeAll();
        centralPanel.add(panel);
        centralPanel.revalidate();
        centralPanel.repaint();
    }
}










