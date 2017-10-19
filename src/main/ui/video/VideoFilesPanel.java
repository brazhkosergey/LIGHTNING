package ui.video;

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
        this.setLayout(new BorderLayout());
        this.add(mainScrollPanel,BorderLayout.CENTER);
    }

    public void showVideos() {
        mainPanel.removeAll();
        mapOfFiles.clear();
        listOfFilesNames.clear();

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

        JPanel mainVideoPanel;
        JLabel numberLabel;
        JLabel dateVideoLabel;
        JLabel timeVideoLabel;
        JLabel countFilesLabel;
        JLabel countTimeLabel;

        JButton showVideoButton;
        JButton deleteButton;

        for (int i = listOfFilesNames.size() - 1; i >= 0; i--) {
            Long dataLong = listOfFilesNames.get(i);
            Date date = new Date(dataLong);
            Map<Integer, File> filesVideoBytes = mapOfFiles.get(dataLong);

            int videoSize = 0;
            for (Integer integer : filesVideoBytes.keySet()) {
                File file1 = filesVideoBytes.get(integer);
                videoSize = file1.listFiles().length;
            }

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

            countFilesLabel = new JLabel(MainFrame.getBundle().getString("filesword") + countFiles);
            countFilesLabel.setPreferredSize(new Dimension(60, 30));

            countTimeLabel = new JLabel(videoSize + MainFrame.getBundle().getString("seconds"));
            countTimeLabel.setPreferredSize(new Dimension(150, 30));

            showVideoButton = new JButton(String.valueOf((char) 9658));//PLAY
            int finalI = i + 1;
            showVideoButton.addActionListener((ActionEvent e) -> {
                VideoPlayer.setShowVideoPlayer(true);
                VideoPlayer videoPlayer = new VideoPlayer(filesVideoBytes, dateFormat.format(new Date(dataLong)), finalI);
                MainFrame.getMainFrame().setCentralPanel(videoPlayer);
            });

            deleteButton = new JButton("DEL");
            deleteButton.addActionListener((e) -> {
                for (Integer integer : filesVideoBytes.keySet()) {
                    File folderToDel = filesVideoBytes.get(integer);

                    String absolutePathToImage = folderToDel.getAbsolutePath().replace(".tmp", ".jpg");
                    File imageFile = new File(absolutePathToImage);
                    if (imageFile.exists()) {
                        imageFile.delete();
                    }

                    String name = folderToDel.getName();
                    String[] split = name.split("-");
                    long dateLong = Long.parseLong(split[0]);

                    String audioPath = MainFrame.getPath() + "\\bytes\\" + dateLong + ".wav";
                    File audioFile = new File(audioPath);
                    if (audioFile.exists()) {
                        audioFile.delete();
                    }

                    File[] filesToDel = folderToDel.listFiles();
                    if (filesToDel != null) {
                        for (File f : filesToDel) {
                            f.delete();
                        }
                    }
                    folderToDel.delete();
                    showVideos();
                }
            });

            mainVideoPanel = new JPanel(new FlowLayout());
            mainVideoPanel.setBorder(BorderFactory.createEtchedBorder());
            mainVideoPanel.setMaximumSize(new Dimension(1065, 45));
            mainVideoPanel.add(numberLabel);
            mainVideoPanel.add(Box.createRigidArea(new Dimension(20, 30)));
            mainVideoPanel.add(dateVideoLabel);
            mainVideoPanel.add(timeVideoLabel);
            mainVideoPanel.add(Box.createRigidArea(new Dimension(20, 30)));
            mainVideoPanel.add(countFilesLabel);
            mainVideoPanel.add(Box.createRigidArea(new Dimension(20, 30)));
            mainVideoPanel.add(countTimeLabel);
            mainVideoPanel.add(Box.createRigidArea(new Dimension(360, 30)));
            mainVideoPanel.add(showVideoButton);
            mainVideoPanel.add(Box.createRigidArea(new Dimension(100, 30)));
            mainVideoPanel.add(deleteButton);
            mainVideoPanel.add(Box.createRigidArea(new Dimension(2, 30)));
            mainPanel.add(Box.createRigidArea(new Dimension(600, 2)));
            mainPanel.add(mainVideoPanel);
        }

        mainScrollPanel.repaint();
        MainFrame.getMainFrame().setCentralPanel(this);
    }
}
