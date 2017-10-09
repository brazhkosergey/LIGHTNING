package ui.video;

import entity.MainVideoCreator;
import entity.VideoPlayer;
import ui.main.MainFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class VideoFilesPanel extends JPanel {
    private SimpleDateFormat dateFormat = new SimpleDateFormat();
    private static VideoFilesPanel videoFilesPanel;

    private static Map<Long, Map<Integer, File>> mapOfFiles;
    private static List<Long> listOfFilesNames;

    private JPanel mainPanel;
    private JScrollPane mainScrollPanel;
    private JPanel exportSettingPanel;
    private JSlider slider;

    private VideoFilesPanel() {
        mapOfFiles = new HashMap<>();
        listOfFilesNames = new ArrayList<>();
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
        exportSettingPanel.setLayout(new BoxLayout(exportSettingPanel, BoxLayout.Y_AXIS));
        exportSettingPanel.setPreferredSize(new Dimension(400, 530));
        exportSettingPanel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));

        JLabel exportSettingLabel = new JLabel("Параметри відео файлів");
        exportSettingLabel.setAlignmentX(CENTER_ALIGNMENT);
        JLabel sliderTextLabel = new JLabel("FPS для відео, що експортуемо");
        sliderTextLabel.setAlignmentX(CENTER_ALIGNMENT);
        JLabel sliderLabel = new JLabel();
        sliderLabel.setAlignmentX(CENTER_ALIGNMENT);
        slider = new JSlider();
        slider.setPreferredSize(new Dimension(300, 30));
        slider.setValue(50);
        slider.setMaximum(120);
        slider.setMinimum(5);
        slider.setMajorTickSpacing(10);
        slider.setMinorTickSpacing(1);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.addChangeListener((e) -> {
            sliderLabel.setText("FPS = " + slider.getValue());
        });

        sliderLabel.setText("FPS = " + slider.getValue());
        JPanel centralPanel = new JPanel(new FlowLayout());
        centralPanel.add(mainScrollPanel);
        centralPanel.add(exportSettingPanel);
        this.add(centralPanel);
    }

    public void showVideos() {
        mainPanel.removeAll();
        mapOfFiles.clear();
        listOfFilesNames.clear();
//        dateFormat.applyPattern("dd MMMM yyyy HH:mm:ss");


        JPanel mainVideoPanel;
        JLabel numberLabel;
        JLabel countFilesLabel;


        JLabel dateVideoLabel;
        JLabel timeVideoLabel;
        JButton showVideoButton;
        JButton exportButton;
        JButton deleteButton;

        File file = new File(MainFrame.getPath() + "\\bytes\\");
        File[] files = file.listFiles();
        String fileName;

        if (files != null) {
            for (File fileFromFolder : files) {
                fileName = fileFromFolder.getName();
                if (fileName.contains(".tmp")) {
                    String[] split = fileName.split("-");
                    long dataLong = Long.parseLong(split[0]);
                    String[] splitInteger = split[1].split("\\.");
                    int cameraGroupNumber = Integer.parseInt(splitInteger[0].substring(0, 1));

                    if (!listOfFilesNames.contains(dataLong)) {
                        listOfFilesNames.add(dataLong);
                    }

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

        Collections.sort(listOfFilesNames);

        for (int i = listOfFilesNames.size() - 1; i >= 0; i--) {

            Long dataLong = listOfFilesNames.get(i);
            Date date = new Date(dataLong);
            Map<Integer, File> filesVideoBytes = mapOfFiles.get(dataLong);

            int countFiles = filesVideoBytes.size();

            numberLabel = new JLabel(String.valueOf(i + 1));
            numberLabel.setPreferredSize(new Dimension(20, 30));
            numberLabel.setFont(new Font(null, Font.BOLD, 15));
            numberLabel.setHorizontalAlignment(SwingConstants.CENTER);

            dateFormat.applyPattern("yyyy.MM.dd");
            dateVideoLabel = new JLabel(dateFormat.format(date));
            dateFormat.applyPattern("HH:mm:ss");
            timeVideoLabel = new JLabel(dateFormat.format(date));
            timeVideoLabel.setFont(new Font(null, Font.BOLD, 15));
            timeVideoLabel.setForeground(new Color(46, 139, 87));

            countFilesLabel = new JLabel("Файлів - "+countFiles);
            countFilesLabel.setPreferredSize(new Dimension(60, 30));

            showVideoButton = new JButton(String.valueOf((char) 9658));//PLAY
            showVideoButton.addActionListener((ActionEvent e) -> {
                VideoPlayer.setShowVideoPlayer(true);
                VideoPlayer videoPlayer = new VideoPlayer(filesVideoBytes, dateFormat.format(new Date(dataLong)));
                MainFrame.getMainFrame().setCentralPanel(videoPlayer);
            });

            exportButton = new JButton("Експорт");
            exportButton.addActionListener((e) -> {
                List<Thread> list = new ArrayList<>();
                int number = 1;
                for (Integer integer : filesVideoBytes.keySet()) {
                    File file1 = filesVideoBytes.get(integer);
                    Thread thread = new Thread(() -> {
                        MainVideoCreator.encodeVideoXuggle(file1);
                    });
                    thread.setName("EncodeVideoThread. Number " + number++);
                    list.add(thread);
                }

                Thread saverThread = new Thread(() -> {
                    for (int j = 0; j < list.size(); j++) {
                        list.get(j).start();
                        while (true) {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e1) {
                                e1.printStackTrace();
                            }
                            if (!list.get(j).isAlive()) {
                                break;
                            }
                        }
                    }
                });
                saverThread.setName("Main Saver Thread");
                saverThread.start();
            });

            deleteButton = new JButton("DEL");
            deleteButton.addActionListener((e) -> {
                for (Integer integer : filesVideoBytes.keySet()) {
                    File folderToDel = filesVideoBytes.get(integer);

                    File[] filesToDel = folderToDel.listFiles();
                    if(filesToDel!=null){
                        for(File f:filesToDel){
                            f.delete();
                        }
                    }
                    folderToDel.delete();
                    showVideos();
                }
            });

            mainVideoPanel = new JPanel(new FlowLayout());
            mainVideoPanel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
            mainVideoPanel.setMaximumSize(new Dimension(700,45));
            mainVideoPanel.add(numberLabel);
            mainVideoPanel.add(Box.createRigidArea(new Dimension(10, 30)));
            mainVideoPanel.add(dateVideoLabel);
            mainVideoPanel.add(timeVideoLabel);
            mainVideoPanel.add(Box.createRigidArea(new Dimension(10, 30)));
            mainVideoPanel.add(countFilesLabel);
            mainVideoPanel.add(Box.createRigidArea(new Dimension(190, 30)));
            mainVideoPanel.add(showVideoButton);
            mainVideoPanel.add(Box.createRigidArea(new Dimension(10, 30)));
            mainVideoPanel.add(exportButton);
            mainVideoPanel.add(Box.createRigidArea(new Dimension(2, 30)));
            mainVideoPanel.add(deleteButton);
            mainVideoPanel.add(Box.createRigidArea(new Dimension(2, 30)));
            mainPanel.add(Box.createRigidArea(new Dimension(600, 2)));
            mainPanel.add(mainVideoPanel);
        }

        mainScrollPanel.repaint();
        MainFrame.getMainFrame().setCentralPanel(this);
    }
}
