package ui.main;

import entity.MainVideoCreator;
import ui.camera.CameraPanel;
import ui.camera.VideoCatcher;
import ui.camera.VideoCreator;
import ui.setting.CameraSetting;
import ui.video.VideoPanel;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainFrame extends JFrame {
    private static MainFrame mainFrame;
    private static JPanel mainPanel = new JPanel();
    private static JPanel northPanel = new JPanel();
    private static JPanel centralPanel = new JPanel();
    private static JPanel southPanel = new JPanel();
    private static CameraSetting cameraSetting = CameraSetting.getCameraSetting();
    private static VideoPanel videoPanel = VideoPanel.getVideoPanel();

    private JPanel allCameraPanel;
//    JPanel fourthBlockPanel;
//    JPanel thirdBlockPanel;
//    JPanel firstBlockPanel;
//    JPanel secondBlockPanel;


    public static Map<Integer, CameraPanel> cameras;
    public static Map<Integer, String> camerasAddress;

    private static Map<Integer, JPanel> cameraBlock;

    public static JLabel mainLabel = new JLabel("Головна");

    private MainFrame() {
        super("LIGHTNING");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(1150, 720));
        cameras = new HashMap<>();
        cameraBlock=new HashMap<>();
        camerasAddress = new HashMap<>();
        mainPanel.setLayout(new BorderLayout());
        buildMainWindow();
        getContentPane().add(mainPanel, BorderLayout.CENTER);
        setVisible(true);
        pack();
        showAllCameras();
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
           centralPanel.removeAll();
           centralPanel.add(cameraSetting);
           centralPanel.repaint();
           mainLabel.setText("Камери");
        });

        JButton videoButton = new JButton("Відео");
        videoButton.setPreferredSize(new Dimension(120, 30));
        videoButton.addActionListener((e)->{
            centralPanel.removeAll();
            videoPanel.showVideos();
            centralPanel.add(videoPanel);
            centralPanel.repaint();
            mainLabel.setText("Відео");



//            URL url = null;
//            try {
//                url = new URL("http://71.86.78.236:8888/mjpg/video.mjpg?COUNTER");
//            } catch (MalformedURLException e1) {
//                e1.printStackTrace();
//            }
//
//            if (url != null) {
//                cameras.get(2).getVideoCatcher().startCatchVideo(url);
//            }
        });

        JButton settingButton = new JButton("Налаштування");
        settingButton.setPreferredSize(new Dimension(120, 30));
        settingButton.addActionListener((e)->{


//            URL url = null;
//            try {
//                url = new URL("http://192.65.213.241:80/mjpg/video.mjpg?COUNTER");
////                        url = new URL("http://root:PASS@192.168.3.223/axis-cgi/mjpg/video.cgi?camera=1");
////                url = new URL("http://195.235.198.107:3346/axis-cgi/mjpg/video.cgi");
//            } catch (MalformedURLException e1) {
//                e1.printStackTrace();
//            }
//
//            if (url != null) {
//                cameras.get(1).getVideoCatcher().startCatchVideo(url);
//            }
        });

        mainLabel.setFont(new Font("Comic Sans MS", Font.BOLD, 15));
        mainLabel.setPreferredSize(new Dimension(200, 30));

//        ====================================
        northPanel.add(mainWindowButton);
        northPanel.add(cameraButton);
        northPanel.add(videoButton);
        northPanel.add(settingButton);
        northPanel.add(Box.createHorizontalStrut(350));
        northPanel.add(mainLabel);
        mainPanel.add(northPanel, BorderLayout.NORTH);
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
//        CameraPanel cameraOne = new CameraPanel();
//        cameraOne.addMouseListener(new MouseAdapter() {
//            @Override
//            public void mouseClicked(MouseEvent e) {
//                if (e.getClickCount() == 2) {
//
//                    cameraOne.getVideoCatcher().setWidthAndHeight(1000, 570);
//                    cameraOne.setPreferredSize(new Dimension(1050, 580));
//                    cameraOne.revalidate();
//                    centralPanel.removeAll();
//                    centralPanel.add(cameraOne);
//                    centralPanel.repaint();
//                }
//            }
//        });
//
//        cameras.put(1, cameraOne);
//
//        CameraPanel cameraTwo = new CameraPanel();
//        cameraTwo.addMouseListener(new MouseAdapter() {
//            @Override
//            public void mouseClicked(MouseEvent e) {
//                if (e.getClickCount() == 2) {
//                    cameraTwo.getVideoCatcher().setWidthAndHeight(1000, 600);
//                    cameraTwo.setPreferredSize(new Dimension(900, 600));
//                    cameraTwo.revalidate();
//                    centralPanel.removeAll();
//                    centralPanel.add(cameraTwo);
//                    centralPanel.repaint();
//                }
//            }
//        });
//
//
//        cameras.put(2, cameraTwo);
//
//        firstBlockPanel = new JPanel();
//        firstBlockPanel.setPreferredSize(new Dimension(550, 270));
//        firstBlockPanel.setLayout(new FlowLayout());
//        firstBlockPanel.add(cameraOne);
//        firstBlockPanel.add(cameraTwo);
//        TitledBorder titleMainSetting = BorderFactory.createTitledBorder("Перший блок камер");
//        titleMainSetting.setTitleJustification(TitledBorder.CENTER);
//        titleMainSetting.setTitleFont((new Font("Comic Sans MS", Font.BOLD, 17)));
//        titleMainSetting.setTitleColor(new Color(46, 139, 87));
//        titleMainSetting.setBorder(new LineBorder(new Color(46, 139, 87), 3, true));
//        firstBlockPanel.setBorder(titleMainSetting);
//
//        secondBlockPanel = new JPanel();
//        secondBlockPanel.setPreferredSize(new Dimension(550, 270));
//        secondBlockPanel.setLayout(new FlowLayout());
//        TitledBorder titleSecondBlock = BorderFactory.createTitledBorder("Другий блок камер");
//        titleSecondBlock.setTitleJustification(TitledBorder.CENTER);
//        titleSecondBlock.setTitleFont((new Font("Comic Sans MS", Font.BOLD, 17)));
//        titleSecondBlock.setTitleColor(new Color(46, 139, 87));
//        titleSecondBlock.setBorder(new LineBorder(new Color(46, 139, 87), 3, true));
//        secondBlockPanel.setBorder(titleSecondBlock);
//
//        thirdBlockPanel = new JPanel();
//        thirdBlockPanel.setPreferredSize(new Dimension(550, 270));
//        thirdBlockPanel.setLayout(new FlowLayout());
//
//        TitledBorder titleThirdBlock = BorderFactory.createTitledBorder("Третій блок камер");
//        titleThirdBlock.setTitleJustification(TitledBorder.CENTER);
//        titleThirdBlock.setTitleFont((new Font("Comic Sans MS", Font.BOLD, 17)));
//        titleThirdBlock.setTitleColor(new Color(46, 139, 87));
//        titleThirdBlock.setBorder(new LineBorder(new Color(46, 139, 87), 3, true));
//        thirdBlockPanel.setBorder(titleThirdBlock);
//
//        fourthBlockPanel = new JPanel();
//        fourthBlockPanel.setPreferredSize(new Dimension(550, 270));
//        fourthBlockPanel.setLayout(new FlowLayout());
//
//        TitledBorder titleFourthBlock = BorderFactory.createTitledBorder("Четвертий блок камер");
//        titleFourthBlock.setTitleJustification(TitledBorder.CENTER);
//        titleFourthBlock.setTitleFont((new Font("Comic Sans MS", Font.BOLD, 17)));
//        titleFourthBlock.setTitleColor(new Color(46, 139, 87));
//        titleFourthBlock.setBorder(new LineBorder(new Color(46, 139, 87), 3, true));
//        fourthBlockPanel.setBorder(titleFourthBlock);
//

//        allCameraPanel.add(firstBlockPanel);
//        allCameraPanel.add(secondBlockPanel);
//        allCameraPanel.add(thirdBlockPanel);
//        allCameraPanel.add(fourthBlockPanel);

        centralPanel.add(allCameraPanel);
        mainPanel.add(centralPanel, BorderLayout.CENTER);
    }

    private void buildSouthPanel() {

        JButton startButton = new JButton("почати запис");
        startButton.addActionListener((e -> {
            MainVideoCreator.startCatchVideo(new Date(System.currentTimeMillis()));

        }));

        JButton endButton = new JButton("закінчити запис");
        endButton.addActionListener((e -> {
            MainVideoCreator.stopCatchVideo();
        }));

        southPanel.setLayout(new FlowLayout());
        southPanel.add(startButton);
        southPanel.add(Box.createRigidArea(new Dimension(100,30)));
        southPanel.add(endButton);
        southPanel.add(Box.createRigidArea(new Dimension(20, 20)));
        mainPanel.add(southPanel, BorderLayout.SOUTH);
    }

    public void showAllCameras(){

        for(Integer addressNumber:camerasAddress.keySet()){
            if(!cameras.get(addressNumber).getVideoCatcher().isCatchVideo()){
                URL url = null;
                try {
                    url= new URL(camerasAddress.get(addressNumber));
                } catch (MalformedURLException e) {

                    e.printStackTrace();
                }
                if(url!=null){
                    cameras.get(addressNumber).getVideoCatcher().startCatchVideo(url);
                }
            }
        }
    }
}










