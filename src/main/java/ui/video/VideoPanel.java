package ui.video;

import entity.VideoPlayerPanel;
import ui.camera.CameraPanel;
import ui.camera.VideoCatcher;
import ui.camera.VideoCreator;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class VideoPanel extends JPanel {
    SimpleDateFormat dateFormat = new SimpleDateFormat();
    private static VideoPanel videoPanel;
    public static Map<Date, Map<Integer, List<BufferedImage>>> map;

    JPanel mainPanel;
    JScrollPane mainScrollPanel;
    JPanel allVideosPanel;


    boolean play = false;


    private VideoPanel() {
        allVideosPanel = new JPanel();
        allVideosPanel.setPreferredSize(new Dimension(900, 600));
        map = new HashMap<>();
        buildVideoPanel();
    }

    public static VideoPanel getVideoPanel() {
        if (videoPanel != null) {
            return videoPanel;
        } else {
            videoPanel = new VideoPanel();
            return videoPanel;
        }
    }

    private void buildVideoPanel() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainScrollPanel = new JScrollPane(mainPanel);
        mainScrollPanel.setPreferredSize(new Dimension(1100, 600));
        this.add(mainScrollPanel);
    }

    public void showVideos() {
//        test();

        mainPanel.removeAll();
        dateFormat.applyPattern("dd.MM.yyyy HH:mm:ss");
        JPanel mainVideoPanel;
        JLabel mainVideoLabel;
        JButton showVideoButton;
        JButton exportButton;

        for (Date date : map.keySet()) {
            Map<Integer, List<BufferedImage>> integerListMap = map.get(date);
            mainVideoLabel = new JLabel(dateFormat.format(date));
            showVideoButton = new JButton("PLAY");
            showVideoButton.addActionListener((ActionEvent e) -> {
                allVideosPanel.removeAll();

                JFrame frame = new JFrame(dateFormat.format(date));
                frame.setPreferredSize(new Dimension(1000, 700));
                JPanel mainPaneJFrame = new JPanel();
                mainPaneJFrame.setMaximumSize(new Dimension(900, 600));
                mainPaneJFrame.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));

                for (int i = 1; i < 5; i++) {
                    int listNumber = i;
                    VideoPlayerPanel videoPlayer = new VideoPlayerPanel();
                    videoPlayer.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
                    videoPlayer.setPreferredSize(new Dimension(400, 270));
                    Thread thread = new Thread(() -> {
                        boolean work = true;
                        while (work) {
                            if(!frame.isEnabled()){
                                work = false;
                            }

                            if (play) {
                                if (!videoPlayer.isVideoPlay()) {
                                    videoPlayer.showVideo();
                                }

                                for (int i1 = 0; i1 < integerListMap.get(listNumber).size(); i1++) {

                                    System.out.println("Показываем кадр " + i1+"=======================================");
                                    videoPlayer.setBufferedImage(VideoCatcher.processImage(integerListMap.get(listNumber).get(i1),389,259) );
                                    videoPlayer.repaint();
                                    try {
                                        Thread.sleep(100);
                                    } catch (InterruptedException e1) {
                                        e1.printStackTrace();
                                    }
                                }
                            } else {
                                if (videoPlayer.isVideoPlay()) {
                                    videoPlayer.setLabelText("ВІДЕО ВІДСУТНЕ");
                                }
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e1) {
                                    e1.printStackTrace();
                                }
                            }
                        }
                    });

                    if (integerListMap.containsKey(listNumber)) {
                        System.out.println("Поток " + listNumber + " запущен.");
                        thread.setDaemon(true);
                        thread.start();
                    }
                    allVideosPanel.add(videoPlayer);
                }

                mainPaneJFrame.add(allVideosPanel);
//                Thread playerThread = new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        while (true) {
//
//                            if (play) {
//                                int size = 0;
//                                for (Integer integer : integerListMap.keySet()) {
//                                    size = integerListMap.get(integer).size();
//                                    if (size != 0) {
//                                        break;
//                                    }
//                                }
//
//                                for (int i = 0; i < size; i++) {
//                                    for (int j = 0; j < 4; j++) {
//                                        if (integerListMap.get(j) != null) {
//                                            if (integerListMap.get(j).get(i) != null) {
//                                                playerPanelMap.get(j).setBufferedImage(integerListMap.get(j).get(i));
//                                            }
//                                        }
//                                    }
//                                }
//                            } else {
//                                try {
//                                    Thread.sleep(100);
//                                } catch (InterruptedException e1) {
//                                    e1.printStackTrace();
//                                }
//                            }
//                        }
//                    }
//                });
                JPanel southPane = new JPanel();
                southPane.setLayout(new BoxLayout(southPane, BoxLayout.Y_AXIS));
                JSlider slider = new JSlider();
                slider.setMinimumSize(new Dimension(800, 20));
                slider.setMinorTickSpacing(1);
                slider.setValue(1);
                slider.addChangeListener(new ChangeListener() {
                    @Override
                    public void stateChanged(ChangeEvent e) {
                        System.out.println("Значение на слайдере изменилось на: " + slider.getValue());
                    }
                });
                southPane.add(slider);

                JPanel buttonsPanel = new JPanel(new FlowLayout());
                buttonsPanel.setPreferredSize(new Dimension(950, 70));
                buttonsPanel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
                JButton nextImage = new JButton("NEXT");
                JButton slowerButton = new JButton("<<");
                JButton startButton = new JButton("PLAY");
                startButton.addActionListener(actionEvent -> {
                    play = true;
                });

                JButton pauseButton = new JButton("PAUSE");
                JButton stopButton = new JButton("STOP");
                stopButton.addActionListener(actionEvent -> {
                    play = false;
                });
                JButton fasterButton = new JButton(">>");
                JButton previousImage = new JButton("PREV");

                buttonsPanel.add(previousImage);
                buttonsPanel.add(slowerButton);
                buttonsPanel.add(startButton);
                buttonsPanel.add(pauseButton);
                buttonsPanel.add(stopButton);
                buttonsPanel.add(fasterButton);
                buttonsPanel.add(nextImage);

                southPane.add(buttonsPanel);

                frame.getContentPane().add(mainPaneJFrame, BorderLayout.CENTER);
                frame.getContentPane().add(southPane, BorderLayout.SOUTH);
                frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                frame.setVisible(true);
                frame.pack();
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
        mainScrollPanel.repaint();
    }


    public void test() {
        for (int i = 0; i < 50; i++) {
            map.put(new Date(System.currentTimeMillis() + i), new HashMap<>());
        }
    }


}
