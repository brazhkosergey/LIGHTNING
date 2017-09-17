package ui.video;

import entity.MainPlayerClass;
import entity.VideoPlayer;
import ui.main.MainFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class VideoFilesPanel extends JPanel {
    SimpleDateFormat dateFormat = new SimpleDateFormat();
    private static VideoFilesPanel videoFilesPanel;

    public static Map<Date, Map<Integer, List<BufferedImage>>> map;

    public static Map<Long, Map<Integer,File>> mapOfFiles;

    JPanel mainPanel;
    JScrollPane mainScrollPanel;

    boolean play = false;

    private VideoFilesPanel() {
        map = new HashMap<>();
        mapOfFiles = new HashMap<>();
        buildVideoPanel();
    }

    public static VideoFilesPanel getVideoFilesPanel() {
        if (videoFilesPanel != null) {
            return videoFilesPanel;
        } else {
            videoFilesPanel = new VideoFilesPanel();
            return videoFilesPanel;
        }
    }

    private void buildVideoPanel() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainScrollPanel = new JScrollPane(mainPanel);
        mainScrollPanel.setPreferredSize(new Dimension(1100, 580));
        this.add(mainScrollPanel);
    }

    public void showVideos() {
        mainPanel.removeAll();
        mapOfFiles.clear();
        dateFormat.applyPattern("dd.MM.yyyy HH:mm:ss");
        JPanel mainVideoPanel;
        JLabel mainVideoLabel;
        JButton showVideoButton;
        JButton exportButton;
        File file = new File("C:\\ipCamera\\");
        File[] files = file.listFiles();
        String fileName;

        if(files!=null){
            for (File fileFromFolder : files) {
                fileName = fileFromFolder.getName();
                if (fileName.contains(".tmp")) {
                    String[] split = fileName.split("-");//1505382999165-1.tmp
                    long dataLong = Long.parseLong(split[0]);
                    String[] splitInteger = split[1].split("\\.");
                    int cameraGroupNumber = Integer.parseInt(splitInteger[0]);
                    if (mapOfFiles.containsKey(dataLong)) {
                        mapOfFiles.get(dataLong).put(cameraGroupNumber,fileFromFolder);
                        System.out.println("Добавилил файл "+fileFromFolder.getName()+" : "+dataLong+" с номером "+cameraGroupNumber+" в уже существующую мапу.");
                    } else {
                        Map<Integer,File> files1 = new HashMap<>();
                        files1.put(cameraGroupNumber,fileFromFolder);
                        mapOfFiles.put(dataLong, files1);
                        System.out.println("Саздали мапу для файла "+fileFromFolder.getName()+" : "+dataLong+" с номером "+cameraGroupNumber);
                    }
                }
            }
        }
        System.out.println("Размер мапы: - "+mapOfFiles.size());
        for(Long dataLong:mapOfFiles.keySet()){

            Date date = new Date(dataLong);
            mainVideoLabel = new JLabel(dateFormat.format(date));
            showVideoButton = new JButton("PLAY");
            showVideoButton.addActionListener((ActionEvent e) -> {
                VideoPlayer.setShowVideoPlayer(true);
                VideoPlayer videoPlayer = new VideoPlayer(mapOfFiles.get(dataLong), dateFormat.format(new Date(dataLong)));
                MainFrame.getMainFrame().setCentralPanel(videoPlayer);
            });

            exportButton = new JButton("Експорт");

            mainVideoPanel = new JPanel(new FlowLayout());
            mainVideoPanel.setMaximumSize(new Dimension(900, 40));
            mainVideoPanel.add(mainVideoLabel);
            mainVideoPanel.add(Box.createRigidArea(new Dimension(50, 30)));
            mainVideoPanel.add(showVideoButton);
            mainVideoPanel.add(Box.createRigidArea(new Dimension(30, 30)));
            mainVideoPanel.add(exportButton);
            mainVideoPanel.add(Box.createRigidArea(new Dimension(400, 30)));
            mainPanel.add(mainVideoPanel);
        }
//        for (Date date : map.keySet()) {
//            Map<Integer, List<BufferedImage>> integerListMap = map.get(date);
//            mainVideoLabel = new JLabel(dateFormat.format(date));
//            showVideoButton = new JButton("PLAY");
//            showVideoButton.addActionListener((ActionEvent e) -> {
//                allVideosPanel.removeAll();
//
//                JFrame frame = new JFrame(dateFormat.format(date));
//                frame.setPreferredSize(new Dimension(1000, 700));
//                JPanel mainPaneJFrame = new JPanel();
//                mainPaneJFrame.setMaximumSize(new Dimension(900, 600));
//                mainPaneJFrame.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
//
//                for (int i = 1; i < 5; i++) {
//                    int listNumber = i;
//                    VideoPlayerPanel videoPlayer = new VideoPlayerPanel();
//                    videoPlayer.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
//                    videoPlayer.setPreferredSize(new Dimension(400, 270));
//                    Thread thread = new Thread(() -> {
//                        boolean work = true;
//                        while (work) {
//                            if(!frame.isEnabled()){
//                                work = false;
//                            }
//
//                            if (play) {
//                                if (!videoPlayer.isVideoPlay()) {
//                                    videoPlayer.showVideo();
//                                }
//
//                                for (int i1 = 0; i1 < integerListMap.get(listNumber).size(); i1++) {
//                                    System.out.println("Показываем кадр " + i1+"=======================================");
//                                    videoPlayer.setBufferedImage(VideoCatcher.processImage(integerListMap.get(listNumber).get(i1),389,259) );
//                                    videoPlayer.repaint();
//                                    try {
//                                        Thread.sleep(100);
//                                    } catch (InterruptedException e1) {
//                                        e1.printStackTrace();
//                                    }
//                                }
//                            } else {
//                                if (videoPlayer.isVideoPlay()) {
//                                    videoPlayer.setLabelText("ВІДЕО ВІДСУТНЕ");
//                                }
//                                try {
//                                    Thread.sleep(100);
//                                } catch (InterruptedException e1) {
//                                    e1.printStackTrace();
//                                }
//                            }
//                        }
//                    });
//
//                    if (integerListMap.containsKey(listNumber)) {
//                        System.out.println("Поток " + listNumber + " запущен.");
//                        thread.setDaemon(true);
//                        thread.start();
//                    }
//                    allVideosPanel.add(videoPlayer);
//                }
//
//                mainPaneJFrame.add(allVideosPanel);
////                Thread playerThread = new Thread(new Runnable() {
////                    @Override
////                    public void run() {
////                        while (true) {
////
////                            if (play) {
////                                int size = 0;
////                                for (Integer integer : integerListMap.keySet()) {
////                                    size = integerListMap.get(integer).size();
////                                    if (size != 0) {
////                                        break;
////                                    }
////                                }
////
////                                for (int i = 0; i < size; i++) {
////                                    for (int j = 0; j < 4; j++) {
////                                        if (integerListMap.get(j) != null) {
////                                            if (integerListMap.get(j).get(i) != null) {
////                                                playerPanelMap.get(j).setBufferedImage(integerListMap.get(j).get(i));
////                                            }
////                                        }
////                                    }
////                                }
////                            } else {
////                                try {
////                                    Thread.sleep(100);
////                                } catch (InterruptedException e1) {
////                                    e1.printStackTrace();
////                                }
////                            }
////                        }
////                    }
////                });
//                JPanel southPane = new JPanel();
//                southPane.setLayout(new BoxLayout(southPane, BoxLayout.Y_AXIS));
//                JSlider slider = new JSlider();
//                slider.setMinimumSize(new Dimension(800, 20));
//                slider.setMinorTickSpacing(1);
//                slider.setValue(1);
//                slider.addChangeListener(new ChangeListener() {
//                    @Override
//                    public void stateChanged(ChangeEvent e) {
//                        System.out.println("Значение на слайдере изменилось на: " + slider.getValue());
//                    }
//                });
//                southPane.add(slider);
//
//                JPanel buttonsPanel = new JPanel(new FlowLayout());
//                buttonsPanel.setPreferredSize(new Dimension(950, 70));
//                buttonsPanel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
//                JButton nextImage = new JButton("NEXT");
//                JButton slowerButton = new JButton("<<");
//                JButton startButton = new JButton("PLAY");
//                startButton.addActionListener(actionEvent -> {
//                    play = true;
//                });
//
//                JButton pauseButton = new JButton("PAUSE");
//                JButton stopButton = new JButton("STOP");
//                stopButton.addActionListener(actionEvent -> {
//                    play = false;
//                });
//                JButton fasterButton = new JButton(">>");
//                JButton previousImage = new JButton("PREV");
//
//                buttonsPanel.add(previousImage);
//                buttonsPanel.add(slowerButton);
//                buttonsPanel.add(startButton);
//                buttonsPanel.add(pauseButton);
//                buttonsPanel.add(stopButton);
//                buttonsPanel.add(fasterButton);
//                buttonsPanel.add(nextImage);
//
//                southPane.add(buttonsPanel);
//
//                frame.getContentPane().add(mainPaneJFrame, BorderLayout.CENTER);
//                frame.getContentPane().add(southPane, BorderLayout.SOUTH);
//                frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
//                frame.setVisible(true);
//                frame.pack();
//            });
//            exportButton = new JButton("Експорт");
//
//            mainVideoPanel = new JPanel(new FlowLayout());
//            mainVideoPanel.setMaximumSize(new Dimension(900, 40));
//            mainVideoPanel.add(mainVideoLabel);
//            mainVideoPanel.add(Box.createRigidArea(new Dimension(50, 30)));
//            mainVideoPanel.add(showVideoButton);
//            mainVideoPanel.add(Box.createRigidArea(new Dimension(30, 30)));
//            mainVideoPanel.add(exportButton);
//            mainVideoPanel.add(Box.createRigidArea(new Dimension(400, 30)));
//            mainPanel.add(mainVideoPanel);
//        }
        mainScrollPanel.repaint();
        MainFrame.getMainFrame().setCentralPanel(this);
    }
}
