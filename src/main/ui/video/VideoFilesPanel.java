package ui.video;

import ui.main.MainFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * panel with video files list
 */
public class VideoFilesPanel extends JPanel {
    private SimpleDateFormat dateFormat = new SimpleDateFormat();
    private static VideoFilesPanel videoFilesPanel;

    /**
     * collections with time when lightning was catch as key, and all folders with bytes for each camera group as value -  for each video
     */
    private static Map<Long, Map<Integer, File>> mapOfFiles;
    /**
     * list of time when lightning was catch
     */
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
        mainPanel.setBackground(Color.LIGHT_GRAY);
        mainScrollPanel = new JScrollPane(mainPanel);
        this.setLayout(new BorderLayout());
        this.add(mainScrollPanel);
    }

    /**
     * method will search all files with bytes from cameras in default folder,
     * parse names of files, and add links from each event to collection,
     * and rebuild panel
     */
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
            Map<Integer, File> filesVideoBytesMap = mapOfFiles.get(dataLong);

            boolean greenColor = false;
            int videoSize = 0;
            for (Integer integer : filesVideoBytesMap.keySet()) {
                File file1 = filesVideoBytesMap.get(integer);

                String name = file1.getName();
                int first = name.indexOf("[");
                int second = name.indexOf("]");
                String substring = name.substring(first + 1, second);
                greenColor = substring.contains("(");

                videoSize = file1.listFiles().length;
                break;
            }

            int countFiles = filesVideoBytesMap.size();
            numberLabel = new JLabel(String.valueOf(i + 1));
            numberLabel.setPreferredSize(new Dimension(40, 30));
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

            countTimeLabel = new JLabel(videoSize + " " + MainFrame.getBundle().getString("seconds"));
            countTimeLabel.setPreferredSize(new Dimension(150, 30));

            showVideoButton = new JButton(String.valueOf((char) 9658));//PLAY
            showVideoButton.setFont(new Font(null, Font.BOLD, 17));
            int finalI = i + 1;
            showVideoButton.addActionListener((ActionEvent e) -> {
                VideoPlayer.setShowVideoPlayer(true);
                VideoPlayer videoPlayer = new VideoPlayer(filesVideoBytesMap, dateFormat.format(new Date(dataLong)), finalI);
                MainFrame.getMainFrame().setCentralPanel(videoPlayer);
            });

            deleteButton = new JButton("<html>&#10006</html>");
            deleteButton.setFont(new Font(null, Font.BOLD, 17));
            deleteButton.addActionListener((e) -> {
                new DelFrame(filesVideoBytesMap, date);
            });

            mainVideoPanel = new JPanel(new FlowLayout());

            if (greenColor) {
                mainVideoPanel.setBackground(new Color(57, 184, 191));
            } else {
                mainVideoPanel.setBackground(Color.LIGHT_GRAY);
            }

            mainVideoPanel.setBorder(BorderFactory.createBevelBorder(0));
            mainVideoPanel.setMaximumSize(new Dimension(1100, 45));
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

    /**
     * frame for check one more time before delete files
     */
    private class DelFrame extends JFrame {
        private DelFrame(Map<Integer, File> filesVideoBytesMap, Date date) {
            super();
            this.setResizable(false);
            this.setAlwaysOnTop(true);
            this.setPreferredSize(new Dimension(300, 150));
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            int sizeWidth = 300;
            int sizeHeight = 150;
            int locationX = (screenSize.width - sizeWidth) / 2;
            int locationY = (screenSize.height - sizeHeight) / 2;
            this.setBounds(locationX, locationY, sizeWidth, sizeHeight);
            this.setLayout(new BorderLayout());
            buildDelFrame(filesVideoBytesMap, date);
            this.setVisible(true);
            this.pack();
        }

        private void buildDelFrame(Map<Integer, File> filesVideoBytesMap, Date date) {
            dateFormat.applyPattern("yyyy.MM.dd HH:mm:ss");

            JLabel label = new JLabel(dateFormat.format(date));
            label.setFont(new Font(null, Font.BOLD, 20));
            label.setForeground(new Color(14, 160, 14));
            label.setHorizontalTextPosition(SwingConstants.CENTER);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            this.getContentPane().setBackground(Color.LIGHT_GRAY);
            this.add(label, BorderLayout.NORTH);

            JLabel okLabel = new JLabel("OK");
            okLabel.setFont(new Font(null, Font.BOLD, 40));
            okLabel.setForeground(new Color(18, 156, 16));
            okLabel.setHorizontalTextPosition(SwingConstants.CENTER);
            okLabel.setHorizontalAlignment(SwingConstants.CENTER);
            okLabel.setVisible(false);

            JButton delButton = new JButton("<html>&#128465</html>");
            delButton.setFont(new Font(null, Font.BOLD, 40));
            delButton.setForeground(new Color(226, 42, 24));
            delButton.setPreferredSize(new Dimension(150, 50));
            delButton.addActionListener((e) -> {
                delButton.setVisible(false);
                Thread thread = new Thread(() -> {
                    for (Integer integer : filesVideoBytesMap.keySet()) {
                        File folderToDel = filesVideoBytesMap.get(integer);
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
                    }
                    showVideos();
                    okLabel.setVisible(true);
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                    this.dispose();
                });
                thread.start();
            });

            JPanel buttonPanel = new JPanel(new FlowLayout());
            buttonPanel.setBorder(BorderFactory.createEtchedBorder());
            buttonPanel.setBackground(Color.LIGHT_GRAY);
            buttonPanel.setPreferredSize(new Dimension(300, 100));
            buttonPanel.add(Box.createRigidArea(new Dimension(250, 10)));
            buttonPanel.add(delButton);
            buttonPanel.add(okLabel);
            this.add(buttonPanel);
        }
    }
}
