package entity;

import ui.camera.CameraPanel;
import ui.camera.VideoCatcher;
import ui.main.MainFrame;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.plaf.LayerUI;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

class VideoPlayerPanel extends JPanel {
    private File folder;
    private int x = 0;
    private int t = 0;
    private VideoPlayerToShowOneVideo videoPlayerToShowOneVideo;
    private JLayer<JPanel> videoStreamLayer;
    private JLabel informLabel;

    private boolean blockHaveVideo = true;

    private BufferedInputStream bufferedInputStream = null;
    private FileInputStream fileInputStream = null;
    private ByteArrayOutputStream temporaryStream = null;

    private Map<File, Integer> framesInFiles;
    private List<File> filesList;

    private int totalCountFrames;
    private boolean showVideoNow;

    private JPanel videoPanel;
    private JLabel currentFrameLabel;

    private JPanel partExportPanel;

    private Thread buffBytesThread;
    private Thread showFrameThread;
    private Thread FPSThread;

    private int FPS = 0;
    private JLabel totalSecondsLabel;

    private List<Integer> eventFrameNumberList;
    private Map<Integer, byte[]> framesBytesInBuffMap;
    private Deque<Integer> frameInBuffDeque;
    private int numberOfFrameFromStartVideo = 0;
    private int currentFrameNumber = 0;

    private boolean setPosition;
    private boolean play;
    private boolean allFilesIsInBuff;
    private int startFrame;
    private int startFileNumber = 0;

    VideoPlayerPanel(File folderWithTemporaryFiles, int numberVideoPanel) {
        this.folder = folderWithTemporaryFiles;
        videoPlayerToShowOneVideo = new VideoPlayerToShowOneVideo();
        eventFrameNumberList = new ArrayList<>();
        framesBytesInBuffMap = new HashMap<>();
        frameInBuffDeque = new ConcurrentLinkedDeque<>();
        filesList = new ArrayList<>();

        if (folderWithTemporaryFiles != null) {
            String name = folder.getName();
            int first = name.indexOf("[");
            int second = name.indexOf("]");
            String substring = name.substring(first + 1, second);
            String[] eventsSplit = substring.split(",");
            for (String aSplit : eventsSplit) {
                boolean contains = aSplit.contains("(");
                if (contains) {
                    String s = aSplit.substring(1, aSplit.length() - 1);
                    try {
                        int i1 = Integer.parseInt(s);
                        eventFrameNumberList.add(i1);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else {
                    try {
                        int i1 = Integer.parseInt(aSplit);
                        eventFrameNumberList.add(i1);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }

            Collections.sort(eventFrameNumberList);

            framesInFiles = new HashMap<>();
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    filesList.add(file);
                    try {
                        name = file.getName();
                        String[] split = name.split("\\.");
                        String[] lastSplit = split[0].split("-");
                        String countFramesString = lastSplit[1];
                        int i = Integer.parseInt(countFramesString);
                        framesInFiles.put(file, i);
                        totalCountFrames += i;

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                Collections.sort(filesList);
            }

            BufferedImage image = null;
            String absolutePathToImage = folderWithTemporaryFiles.getAbsolutePath().replace(".tmp", ".jpg");
            File imageFile = new File(absolutePathToImage);
            if (imageFile.exists()) {
                try {
                    image = ImageIO.read(new FileInputStream(imageFile));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            LayerUI<JPanel> layerUI = new VideoPlayerPanel.MyLayer(image);
            videoStreamLayer = new JLayer<JPanel>(videoPlayerToShowOneVideo, layerUI);
            videoStreamLayer.setAlignmentX(CENTER_ALIGNMENT);


            informLabel = new JLabel(MainFrame.getBundle().getString("clickplaylabel"));
        } else {
            blockHaveVideo = false;
            informLabel = new JLabel(MainFrame.getBundle().getString("cameradoesnotwork"));
        }

        informLabel.setHorizontalAlignment(SwingConstants.CENTER);
        informLabel.setVerticalAlignment(SwingConstants.CENTER);

        videoPanel = new JPanel(new BorderLayout());
        videoPanel.setBorder(BorderFactory.createEtchedBorder());
        videoPanel.add(informLabel);

        JPanel totalExportPanel = new JPanel(new FlowLayout());
        totalExportPanel.setPreferredSize(new Dimension(200, 300));

        JPanel exportPanel = new JPanel(new FlowLayout());
        exportPanel.setPreferredSize(new Dimension(190, 235));
        exportPanel.setBorder(BorderFactory.createEtchedBorder());

        JLabel label = new JLabel(MainFrame.getBundle().getString("savevideolabel"));
        label.setPreferredSize(new Dimension(150, 30));
        label.setHorizontalTextPosition(SwingConstants.CENTER);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        JButton exportButton = new JButton(MainFrame.getBundle().getString("savevideobutton"));
        exportButton.setFocusable(false);
        exportButton.setPreferredSize(new Dimension(120, 50));
        exportButton.addActionListener((e) -> {
            if (folder != null) {
                Thread thread = new Thread(() -> {
                    MainVideoCreator.encodeVideoXuggle(folder);
                });
                thread.setName("EncodeVideoThread. Number " + numberVideoPanel);
                thread.start();
            }
        });

        JButton imageButton = new JButton(MainFrame.getBundle().getString("saveframebutton"));
        imageButton.setFocusable(false);
        imageButton.setPreferredSize(new Dimension(120, 50));
        imageButton.addActionListener((e) -> {
            Thread thread = new Thread(() -> {
                BufferedImage image = readImage(framesBytesInBuffMap.get(currentFrameNumber));
                if (image != null) {
                    String path = MainFrame.getPath() + System.currentTimeMillis() + "-" + numberVideoPanel + ".jpg";
                    File file = new File(path);
                    try {
                        if (file.createNewFile()) {
                            ImageIO.write(image, "jpg", file);
                        }
                    } catch (Exception xe) {
                        MainFrame.showInformMassage(MainFrame.getBundle().getString("cannotsaveinform"), new Color(171, 40, 33));
                        xe.printStackTrace();
                    }
                } else {
                    MainFrame.showInformMassage(MainFrame.getBundle().getString("cannotsaveinform"), new Color(171, 40, 33));
                }
            });
            thread.start();
        });

        currentFrameLabel = new JLabel(MainFrame.getBundle().getString("framenumberlabel"));
        currentFrameLabel.setPreferredSize(new Dimension(150, 15));
        JLabel totalFrameLabel = new JLabel(MainFrame.getBundle().getString("totalframecountlabel") + totalCountFrames);
        totalFrameLabel.setPreferredSize(new Dimension(150, 15));

        totalSecondsLabel = new JLabel();
        totalSecondsLabel.setPreferredSize(new Dimension(150, 15));

        partExportPanel = new JPanel(new FlowLayout());
        partExportPanel.setBorder(BorderFactory.createLineBorder(Color.lightGray));
        partExportPanel.setPreferredSize(new Dimension(200, 300));
        JLabel partExportLabel = new JLabel(MainFrame.getBundle().getString("partsavelabel"));
        partExportLabel.setPreferredSize(new Dimension(200, 25));
        partExportLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel startPartExportLabel = new JLabel(MainFrame.getBundle().getString("firstframelabel"));
        startPartExportLabel.setPreferredSize(new Dimension(100, 25));
        JTextField startPartExportTextField = new JTextField();
        startPartExportTextField.setFocusable(false);
        startPartExportTextField.setPreferredSize(new Dimension(70, 25));

        JLabel endPartExportLabel = new JLabel(MainFrame.getBundle().getString("lastframelabel"));
        endPartExportLabel.setPreferredSize(new Dimension(100, 25));
        JTextField endPartExportTextField = new JTextField();
        endPartExportTextField.setFocusable(false);
        endPartExportTextField.setPreferredSize(new Dimension(70, 25));

        JLabel informPartExportLabel = new JLabel("<html>Вкажіть номер першого <br> та останнього кадру</html>");
        informPartExportLabel.setFocusable(false);
        informPartExportLabel.setPreferredSize(new Dimension(200, 50));
        informPartExportLabel.setHorizontalAlignment(SwingConstants.CENTER);
        informPartExportLabel.setHorizontalTextPosition(SwingConstants.CENTER);

        JButton partExportButton = new JButton(MainFrame.getBundle().getString("savepartvideobutton"));
        partExportButton.setFocusable(false);
        partExportButton.setPreferredSize(new Dimension(100, 50));
        partExportButton.addActionListener((w) -> {
            Thread save = new Thread(() -> {
                String startFrameText = startPartExportTextField.getText();
                String endFrameText = endPartExportTextField.getText();
                int startFrame = 0;
                int endFrameInt = 0;
                boolean continueSave = false;
                try {
                    startFrame = Integer.parseInt(startFrameText);
                    endFrameInt = Integer.parseInt(endFrameText);
                    continueSave = true;
                } catch (Exception e) {
                    informPartExportLabel.setText("<html>Не вірно вказаний номер кадру <hr></html>");
                    e.printStackTrace();
                }

                if (startFrame < 1 || endFrameInt < 1) {
                    continueSave = false;
                    informPartExportLabel.setText("<html>Не можливо <br> зберегти кадри з " + startFrame + " по " + endFrameInt + "<hr></html>");
                }

                if (endFrameInt > totalCountFrames || endFrameInt > totalCountFrames) {
                    continueSave = false;
                    informPartExportLabel.setText("<html>Запис мае меньше <br> кадрів ніж " + startFrame + " чи " + endFrameInt + "<hr></html>");
                }

                if (startFrame > endFrameInt) {
                    continueSave = false;
                    informPartExportLabel.setText("<html>Перший кадр знаходиться після останнього<hr></html>");
                }

                if (continueSave) {
                    boolean findStartFile = false;
                    int firstFile = 0;
                    int lastFile = 0;

                    int totalFrames = 0;
                    if (endFrameInt != 0) {
                        for (int i = 0; i < filesList.size(); i++) {
                            File fileToRead = filesList.get(i);
                            Integer integer = framesInFiles.get(fileToRead);

                            if (findStartFile) {
                                if (totalFrames + integer > endFrameInt) {
                                    lastFile = i + 1;
                                    if (firstFile == 1) {
                                        lastFile++;
                                    }
                                    break;
                                }
                            } else {
                                if (totalFrames > startFrame) {
                                    firstFile = i;
                                    findStartFile = true;
                                    continue;
                                }
                            }
                            totalFrames += integer;
                        }
                    }

                    if (lastFile != 0) {
                        List<File> filesToSave = new ArrayList<>();
                        int totalFramesToSave = 0;

                        for (int i = firstFile; i < lastFile; i++) {
                            File file = filesList.get(i - 1);
                            totalFramesToSave += framesInFiles.get(file);
                            filesToSave.add(file);
                        }

                        String name1 = folder.getName();
                        String[] split = name1.split("-");
                        long dateLong = Long.parseLong(split[0]);

                        Date date = new Date(dateLong);
                        SimpleDateFormat dateFormat = new SimpleDateFormat();
                        dateFormat.applyPattern("dd MMMM yyyy,HH-mm-ss");
                        String dateString = dateFormat.format(date);

                        String[] fpsSplit = split[1].split("\\.");

                        int i = fpsSplit[0].indexOf(")");
                        String totalFpsString = fpsSplit[0].substring(2, i);
                        int totalFPS = Integer.parseInt(totalFpsString);

                        BufferedImage imageToConnect = null;
                        String absolutePathToImage = folder.getAbsolutePath().replace(".tmp", ".jpg");
                        File imageFile = new File(absolutePathToImage);
                        if (imageFile.exists()) {
                            try {
                                imageToConnect = ImageIO.read(new FileInputStream(imageFile));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        String pathToVideo = MainFrame.getPath() + "\\" + dateString + ".from " + firstFile + " till " + lastFile + ". group -" + numberVideoPanel + ".mp4";
                        System.out.println(dateString);
                        informPartExportLabel.setText("<html>Буде збережено <br> Cекунд: " + (lastFile - firstFile) + ".<br> Кадрів: " + totalFramesToSave + ".<hr></html>");
                        MainVideoCreator.savePartOfVideoFile(pathToVideo, filesToSave, totalFPS, imageToConnect);
                    }
                }
            });
            save.start();
        });

        partExportPanel.add(partExportLabel);
        partExportPanel.add(startPartExportLabel);
        partExportPanel.add(startPartExportTextField);
        partExportPanel.add(endPartExportLabel);
        partExportPanel.add(endPartExportTextField);
        partExportPanel.add(partExportButton);
        partExportPanel.add(informPartExportLabel);
        partExportPanel.setVisible(false);

        exportPanel.add(label);
        exportPanel.add(exportButton);
        exportPanel.add(imageButton);
        exportPanel.add(currentFrameLabel);
        exportPanel.add(totalFrameLabel);
        exportPanel.add(totalSecondsLabel);


        totalExportPanel.add(exportPanel);
        totalExportPanel.add(partExportPanel);

        this.setLayout(new BorderLayout());
        if (numberVideoPanel % 2 != 0) {
            this.add(totalExportPanel, BorderLayout.WEST);
            this.add(videoPanel, BorderLayout.CENTER);
        } else {
            this.add(totalExportPanel, BorderLayout.EAST);
            this.add(videoPanel, BorderLayout.CENTER);
        }

        this.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        createThread();
    }

    void showFrameNumber(int partNumber, int currentFramePositionPercent) {
        if (showFrameThread == null) {
            showFrameThread = new Thread(() -> {
                Integer integer;
                if (partNumber < eventFrameNumberList.size()) {
                    integer = eventFrameNumberList.get(partNumber);
                } else {
                    integer = totalCountFrames;
                }
                double i = (double) currentFramePositionPercent / 100000;
                int frameToShowNumber = (int) (i * integer) + 1;
                if (framesBytesInBuffMap.size() > 0) {
                    if (frameToShowNumber != currentFrameNumber) {
                        if (framesBytesInBuffMap.containsKey(frameToShowNumber)) {
                            byte[] bytes = framesBytesInBuffMap.get(frameToShowNumber);
                            BufferedImage image = readImage(bytes);
                            if (image != null) {
                                videoPlayerToShowOneVideo.setBufferedImage(VideoCatcher.processImage(image, videoPanel.getWidth(), videoPanel.getHeight()));
                                videoPlayerToShowOneVideo.repaint();
                                currentFrameNumber = frameToShowNumber;

                                FPS++;
                                setCurrentFrameNumber(currentFrameNumber);
                                try {
                                    Integer first = frameInBuffDeque.getFirst();
                                    if (framesBytesInBuffMap.size() > 999 && (first - frameToShowNumber) < 500) {
                                        Integer last = frameInBuffDeque.pollLast();
                                        framesBytesInBuffMap.remove(last);
                                    }
                                } catch (Exception ignored) {
                                }
                            }
                        } else {
                            setPosition = true;
                            startFrame = frameToShowNumber - 1;
                        }
                    }
                }
                showFrameThread = null;
            });
            showFrameThread.start();
        }
    }

    private void createThread() {
        FPSThread = new Thread(() -> {
            while (VideoPlayer.isShowVideoPlayer()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                totalSecondsLabel.setText("FPS: " + FPS);
                FPS = 0;
            }
        });

        if (blockHaveVideo) {
            buffBytesThread = new Thread(() -> {
                FPSThread.start();
                while (VideoPlayer.isShowVideoPlayer()) {
                    if (setPosition) {
                        startFileNumber = 0;
                        int totalFrames = 0;
                        for (int i = 0; i < filesList.size(); i++) {
                            File fileToRead = filesList.get(i);
                            Integer integer = framesInFiles.get(fileToRead);

                            if (startFrame < integer) {
                                numberOfFrameFromStartVideo = 0;
                                framesBytesInBuffMap.clear();
                                frameInBuffDeque.clear();
                                break;
                            } else {
                                if (totalFrames + integer > startFrame) {
                                    startFileNumber = i;
                                    framesBytesInBuffMap.clear();
                                    frameInBuffDeque.clear();
                                    break;
                                } else {
                                    totalFrames += integer;
                                }
                                numberOfFrameFromStartVideo = totalFrames;
                            }
                        }
                        closeStreams();
                        setPosition = false;
                        allFilesIsInBuff = false;
                    }

                    if (play) {
                        if (!allFilesIsInBuff) {
                            for (int i = startFileNumber; i < filesList.size(); i++) {
                                if (setPosition) {
                                    break;
                                }
                                File fileToRead = filesList.get(i);
                                try {
                                    fileInputStream = new FileInputStream(fileToRead);
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                }

                                if (fileInputStream != null) {
                                    bufferedInputStream = new BufferedInputStream(fileInputStream);
                                    temporaryStream = new ByteArrayOutputStream(65535);
                                    while (true) {
                                        if (frameInBuffDeque.size() < 1000) {
                                            readBytesImageToBuff();
                                        } else {
                                            try {
                                                Thread.sleep(1);
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        if (x < 0 || setPosition) {
                                            break;
                                        }
                                    }
                                }
                                closeStreams();
                            }
                            allFilesIsInBuff = true;
                        }
                    }

                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    void showVideo() {
        play = true;
        videoPanel.removeAll();
        videoPanel.add(videoStreamLayer);
        videoPanel.validate();
        videoPanel.repaint();
    }

    void stopVideo() {
        setPosition = true;
        startFrame = 1;
        videoPanel.removeAll();
        videoPanel.add(informLabel);
        videoPanel.validate();
        videoPanel.repaint();
    }

    private void readBytesImageToBuff() {
        while (true && !setPosition) {
            t = x;
            try {
                x = bufferedInputStream.read();
            } catch (IOException e) {
                e.printStackTrace();
            }

            temporaryStream.write(x);
            if (x == 216 && t == 255) {// начало изображения
                temporaryStream.reset();

                temporaryStream.write(t);
                temporaryStream.write(x);
            } else if (x == 217 && t == 255) {//конец изображения
                byte[] imageBytes = temporaryStream.toByteArray();
                framesBytesInBuffMap.put(++numberOfFrameFromStartVideo, imageBytes);
                frameInBuffDeque.addFirst(numberOfFrameFromStartVideo);
                return;
            }
            if (x < 0) {
                return;
            }
        }
    }

    private void setCurrentFrameNumber(int currentFrameLabelText) {
        currentFrameLabel.setText(MainFrame.getBundle().getString("framenumberlabel") + currentFrameLabelText);
    }

    boolean isBlockHaveVideo() {
        return blockHaveVideo;
    }

    class MyLayer extends LayerUI<JPanel> {
        BufferedImage bufferedImage;

        MyLayer(BufferedImage bufferedImage) {
            this.bufferedImage = bufferedImage;
        }

        @Override
        public void paint(Graphics g, JComponent c) {
            super.paint(g, c);
            if (bufferedImage != null) {
                g.drawImage(CameraPanel.animateCircle(VideoCatcher.processImage(bufferedImage, videoPanel.getWidth(), videoPanel.getHeight()), BufferedImage.TYPE_INT_ARGB), 0, 0, null);
                g.dispose();
            }
        }
    }

    private BufferedImage readImage(byte[] imageBytes) {
        long start = System.currentTimeMillis();
        BufferedImage bufferedImage = null;
        if (imageBytes != null) {
            try {
                ByteArrayInputStream inputImageStream = new ByteArrayInputStream(imageBytes);
                ImageIO.setUseCache(false);
                bufferedImage = ImageIO.read(inputImageStream);
                inputImageStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Пустой массив");
        }

//        System.out.println("Времени затрачено на создание изображения - " + (System.currentTimeMillis() - start));
        return bufferedImage;
    }

    private void closeStreams() {
        if (fileInputStream != null) {
            try {
                fileInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                fileInputStream = null;
            }
        }

        if (bufferedInputStream != null) {
            try {
                bufferedInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                bufferedInputStream = null;
            }
        }

        if (temporaryStream != null) {
            try {
                temporaryStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            temporaryStream = null;
        }
    }

    Thread getShowVideoThread() {
        return buffBytesThread;
    }

    class VideoPlayerToShowOneVideo extends JPanel {

        private BufferedImage bufferedImage;

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (bufferedImage != null) {
                g.drawImage(bufferedImage, 0, 0, null);
            }
        }

        private void setBufferedImage(BufferedImage bufferedImage) {
            this.bufferedImage = bufferedImage;
        }
    }

    void setShowVideoNow(boolean showVideoNow) {
        this.showVideoNow = showVideoNow;
    }

    boolean isShowVideoNow() {
        return showVideoNow;
    }

    void setFullSize(boolean fullSize) {
        partExportPanel.setVisible(fullSize);
    }
}