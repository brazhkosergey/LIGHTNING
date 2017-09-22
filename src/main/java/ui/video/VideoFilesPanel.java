package ui.video;

import entity.MainVideoCreator;
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

    public static Map<Long, Map<Integer, File>> mapOfFiles;

    JPanel mainPanel;
    JScrollPane mainScrollPanel;
    JPanel exportSettingPanel;
    JSlider slider;

    boolean play = false;

    private VideoFilesPanel() {
//        map = new HashMap<>();
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
        mainScrollPanel.setPreferredSize(new Dimension(700, 530));//650+400 = 1100
        exportSettingPanel = new JPanel();
        exportSettingPanel.setLayout(new BoxLayout(exportSettingPanel,BoxLayout.Y_AXIS));
        exportSettingPanel.setPreferredSize(new Dimension(400,530));
        exportSettingPanel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));

        JLabel exportSettingLabel = new JLabel("Параметри відео файлів");
        exportSettingLabel.setAlignmentX(CENTER_ALIGNMENT);
        JLabel sliderTextLabel = new JLabel("FPS для відео, що експортуемо");
        sliderTextLabel.setAlignmentX(CENTER_ALIGNMENT);
        JLabel sliderLabel = new JLabel();
        sliderLabel.setAlignmentX(CENTER_ALIGNMENT);
        slider = new JSlider();
        slider.setPreferredSize(new Dimension(300,30));
        slider.setValue(50);
        slider.setMaximum(120);
        slider.setMinimum(5);
        slider.setMajorTickSpacing(10);
        slider.setMinorTickSpacing(1);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.addChangeListener((e)->{
            sliderLabel.setText("FPS = "+slider.getValue());
        });
        sliderLabel.setText("FPS = "+slider.getValue());
//        exportSettingPanel.add(exportSettingLabel);
//        exportSettingPanel.add(Box.createRigidArea(new Dimension(100,50)));
//        exportSettingPanel.add(sliderTextLabel);
//        exportSettingPanel.add(sliderLabel);
//        exportSettingPanel.add(slider);
        JPanel centralPanel= new JPanel(new FlowLayout());
        centralPanel.add(mainScrollPanel);
        centralPanel.add(exportSettingPanel);
        this.add(centralPanel);
    }

    public void showVideos() {
        mainPanel.removeAll();
        mapOfFiles.clear();
        dateFormat.applyPattern("dd.MM.yyyy HH:mm:ss");
        JPanel mainVideoPanel;
        JLabel mainVideoLabel;
        JButton showVideoButton;
        JButton exportButton;
        JButton deleteButton;
        File file = new File("C:\\ipCamera\\bytes\\");
        File[] files = file.listFiles();
        String fileName;

        if (files != null) {
            for (File fileFromFolder : files) {
                fileName = fileFromFolder.getName();
                if (fileName.contains(".tmp")) {
                    String[] split = fileName.split("-");
                    long dataLong = Long.parseLong(split[0]);
                    String[] splitInteger = split[1].split("\\.");
                    int cameraGroupNumber = Integer.parseInt(splitInteger[0].substring(0,1));
                    if (mapOfFiles.containsKey(dataLong)) {
                        mapOfFiles.get(dataLong).put(cameraGroupNumber, fileFromFolder);
                    } else {
                        Map<Integer, File> files1 = new HashMap<>();
                        files1.put(cameraGroupNumber, fileFromFolder);
                        mapOfFiles.put(dataLong, files1);
                    }
                }
            }
        }

        for (Long dataLong : mapOfFiles.keySet()) {
            Date date = new Date(dataLong);
            mainVideoLabel = new JLabel(dateFormat.format(date));
            showVideoButton = new JButton("PLAY");
            showVideoButton.addActionListener((ActionEvent e) -> {
                VideoPlayer.setShowVideoPlayer(true);
                VideoPlayer videoPlayer = new VideoPlayer(mapOfFiles.get(dataLong), dateFormat.format(new Date(dataLong)));
                MainFrame.getMainFrame().setCentralPanel(videoPlayer);
            });

            exportButton = new JButton("Експорт");
            exportButton.addActionListener((e) -> {
                Map<Integer, File> integerFileMap = mapOfFiles.get(dataLong);

                for (Integer integer : integerFileMap.keySet()) {
                    File file1 = integerFileMap.get(integer);
                    Thread thread = new Thread(() -> {
//                        MainVideoCreator.encodeVideo(file1);
                        MainVideoCreator.encodeVideoXuggle(file1);
                    });
                    thread.start();
                }
            });

            deleteButton = new JButton("Видалити");
            deleteButton.addActionListener((e) -> {
                Map<Integer, File> integerFileMap = mapOfFiles.get(dataLong);
                for (Integer integer : integerFileMap.keySet()) {
                    File file1 = integerFileMap.get(integer);
                    file1.delete();
                    showVideos();
                }
            });

            mainVideoPanel = new JPanel(new FlowLayout());
            mainVideoPanel.setMaximumSize(new Dimension(600, 40));

            mainVideoPanel.add(mainVideoLabel);
            mainVideoPanel.add(Box.createRigidArea(new Dimension(50, 30)));
            mainVideoPanel.add(showVideoButton);
            mainVideoPanel.add(Box.createRigidArea(new Dimension(30, 30)));
            mainVideoPanel.add(exportButton);
            mainVideoPanel.add(Box.createRigidArea(new Dimension(30, 30)));
            mainVideoPanel.add(deleteButton);
            mainVideoPanel.add(Box.createRigidArea(new Dimension(150, 30)));
            mainPanel.add(mainVideoPanel);
        }

        mainScrollPanel.repaint();
        MainFrame.getMainFrame().setCentralPanel(this);
    }
}
