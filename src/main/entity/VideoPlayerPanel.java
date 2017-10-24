package entity;

import ui.camera.CameraPanel;
import ui.camera.VideoCatcher;
import ui.main.MainFrame;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.plaf.LayerUI;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
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
    private JLabel totalFPSLabel;

    private JPanel partExportPanel;

    private Thread buffBytesThread;
    private Thread showFrameThread;
    private Thread FPSThread;

    private Map<Integer, Thread> buffImageThreadMap;

    private int FPS = 0;
    private JLabel currentFPSLabel;

    private List<Integer> eventFrameNumberList;
    private Map<Integer, byte[]> framesBytesInBuffMap;
    private Map<Integer, BufferedImage> framesImagesInBuffMap;
    private Deque<Integer> frameInBuffDeque;
    private int numberOfFrameFromStartVideo = 0;
    private int currentFrameNumber = 0;

    private boolean setPosition;
    private boolean allFilesIsInBuff;
    private int startFrame;
    private int startFileNumber = 0;

    private boolean fullSize;

    private int numberVideoPanel;

    VideoPlayerPanel(File folderWithTemporaryFiles, int numberVideoPanel) {
        this.numberVideoPanel = numberVideoPanel;
        this.folder = folderWithTemporaryFiles;
        videoPlayerToShowOneVideo = new VideoPlayerToShowOneVideo();
        eventFrameNumberList = new ArrayList<>();
        buffImageThreadMap = new HashMap<>();

        for (int i = 0; i < 10; i++) {
            buffImageThreadMap.put(i, null);
        }

        framesBytesInBuffMap = new HashMap<>();
        framesImagesInBuffMap = new HashMap<>();
        frameInBuffDeque = new ConcurrentLinkedDeque<>();
        filesList = new ArrayList<>();

        int totalFPSForFile = 0;
        if (folderWithTemporaryFiles != null) {
            setShowVideoNow(true);
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
            String[] split1 = name.split("-");
            String[] fpsSplit = split1[1].split("\\.");
            int i1 = fpsSplit[0].indexOf(")");
            String totalFpsString = fpsSplit[0].substring(2, i1);
            totalFPSForFile = Integer.parseInt(totalFpsString);


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
                    FileInputStream fileInputStream = new FileInputStream(imageFile);
                    image = ImageIO.read(fileInputStream);
                    fileInputStream.close();
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
                int i=currentFrameNumber;
                BufferedImage image = readImage(framesBytesInBuffMap.get(i));
                if (image != null) {
                    String path = MainFrame.getPath() + System.currentTimeMillis() + "-" + numberVideoPanel + ".jpg";
                    File file = new File(path);
                    try {
                        if (file.createNewFile()) {
                            ImageIO.write(image, "jpg", file);
                        }
                        MainFrame.showInformMassage(MainFrame.getBundle().getString("saveoneframenumber")+i, Color.DARK_GRAY);
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

        currentFPSLabel = new JLabel();
        currentFPSLabel.setPreferredSize(new Dimension(150, 15));

        totalFPSLabel = new JLabel(MainFrame.getBundle().getString("totalword")+" FPS: " + totalFPSForFile);
        totalFPSLabel.setPreferredSize(new Dimension(150, 15));

        partExportPanel = new JPanel(new FlowLayout());
        partExportPanel.setBorder(BorderFactory.createLineBorder(Color.lightGray));
        partExportPanel.setPreferredSize(new Dimension(200, 300));
        JLabel partExportLabel = new JLabel(MainFrame.getBundle().getString("partsavelabel"));
        partExportLabel.setPreferredSize(new Dimension(200, 25));
        partExportLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel startPartExportLabel = new JLabel(MainFrame.getBundle().getString("firstframelabel"));
        startPartExportLabel.setPreferredSize(new Dimension(100, 25));
        JTextField startPartExportTextField = new JTextField();
        startPartExportTextField.setPreferredSize(new Dimension(70, 25));

        JLabel endPartExportLabel = new JLabel(MainFrame.getBundle().getString("lastframelabel"));
        endPartExportLabel.setPreferredSize(new Dimension(100, 25));
        JTextField endPartExportTextField = new JTextField();
        endPartExportTextField.setPreferredSize(new Dimension(70, 25));

        JLabel informPartExportLabel = new JLabel(MainFrame.getBundle().getString("firstinformvideoplayerlabel"));
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
                    informPartExportLabel.setText(MainFrame.getBundle().getString("wrongframenumberlabel"));
                    e.printStackTrace();
                }

                if (startFrame < 1 || endFrameInt < 1) {
                    continueSave = false;
                    informPartExportLabel.setText(MainFrame.getBundle().getString("secondinformvideoplayerlabel")
                            + startFrame + MainFrame.getBundle().getString("thirdinformvideoplayerlabel") + endFrameInt + "<hr></html>");
                }

                if (endFrameInt > totalCountFrames || endFrameInt > totalCountFrames) {
                    continueSave = false;
                    informPartExportLabel.setText(MainFrame.getBundle().getString("fourthinformvideoplayerlabel") + startFrame + " : " + endFrameInt + "<hr></html>");
                }

                if (startFrame > endFrameInt) {
                    continueSave = false;
                    informPartExportLabel.setText(MainFrame.getBundle().getString("fifthinformvideoplayerlabel"));
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
                        informPartExportLabel.setText(MainFrame.getBundle().getString("sixinformvideoplayerlabel") + (lastFile - firstFile) + MainFrame.getBundle().getString("seveninformvideoplayerlabel") + totalFramesToSave + ".<hr></html>");
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
        exportPanel.add(currentFPSLabel);
        exportPanel.add(totalFrameLabel);
        exportPanel.add(totalFPSLabel);

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
                if (frameInBuffDeque.size() > 0) {
                    if (frameToShowNumber != currentFrameNumber) {
                        if (framesImagesInBuffMap.containsKey(frameToShowNumber) || framesBytesInBuffMap.containsKey(frameToShowNumber)) {
                            BufferedImage image;
                            if (framesImagesInBuffMap.containsKey(frameToShowNumber)) {
                                image = framesImagesInBuffMap.get(frameToShowNumber);
                            } else {
                                byte[] bytes = framesBytesInBuffMap.get(frameToShowNumber);
                                image = readImage(bytes);
                            }

                            if (image != null) {
                                videoPlayerToShowOneVideo.setBufferedImage(processImage(image, videoPanel.getWidth(), videoPanel.getHeight()));
                                videoPlayerToShowOneVideo.repaint();
                                currentFrameNumber = frameToShowNumber;
                                FPS++;
                                setCurrentFrameNumber(currentFrameNumber);
                                try {
                                    Integer first = frameInBuffDeque.getFirst();
                                    if (frameInBuffDeque.size() > 999 && (first - frameToShowNumber) < 500) {
                                        Integer last = frameInBuffDeque.pollLast();
                                        if (framesBytesInBuffMap.containsKey(last)) {
                                            framesBytesInBuffMap.remove(last);
                                        }
                                        if (framesImagesInBuffMap.containsKey(last)) {
                                            framesImagesInBuffMap.remove(last);
                                        }
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
            showFrameThread.setName("Show frame VIDEO PLAYER thread number " + numberVideoPanel);
            showFrameThread.start();
        }
    }

    private void createThread() {

        FPSThread = new Thread(() -> {
            int countTen = 0;
            while (VideoPlayer.isShowVideoPlayer()) {
                if (showVideoNow) {
                    if (countTen == 10) {
                        currentFPSLabel.setText("FPS: " + FPS);
                        FPS = 0;
                        countTen = 0;
                        List<Integer> list = new ArrayList<>();

                        try {
                            for (Integer integer : framesImagesInBuffMap.keySet()) {
                                if (integer < currentFrameNumber) {
                                    list.add(integer);
                                }
                            }
                        } catch (Exception ignored) {
                        }

                        for (Integer integer : list) {
                            framesImagesInBuffMap.remove(integer);
                        }
//                        System.out.println(framesBytesInBuffMap.size() + " - буфер "+numberVideoPanel+" - " + framesImagesInBuffMap.size());
                    } else {
                        ++countTen;

                        if (fullSize) {
                            buffImage(countTen);
                        }

                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        FPSThread.setName("FPS Thread VIDEO PLAYER for video panel " + numberVideoPanel);


        if (blockHaveVideo) {
            buffBytesThread = new Thread(() -> {
                FPSThread.start();
                while (VideoPlayer.isShowVideoPlayer()) {
                    if (showVideoNow) {
                        if (setPosition) {
                            startFileNumber = 0;
                            int totalFrames = 0;
                            for (int i = 0; i < filesList.size(); i++) {
                                File fileToRead = filesList.get(i);
                                Integer integer = framesInFiles.get(fileToRead);

                                if (startFrame < integer) {
                                    numberOfFrameFromStartVideo = 0;
                                    framesBytesInBuffMap.clear();
                                    framesImagesInBuffMap.clear();
                                    frameInBuffDeque.clear();
                                    break;
                                } else {
                                    if (totalFrames + integer > startFrame) {
                                        startFileNumber = i;
                                        framesBytesInBuffMap.clear();
                                        framesImagesInBuffMap.clear();
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
                                    while (VideoPlayer.isShowVideoPlayer()) {
                                        if (frameInBuffDeque.size() < 1000) {
                                            long startReadByteTime = System.currentTimeMillis();
                                            readBytesImageToBuff();
//                                        System.out.println("Время на считывание массива байт номер - " + numberOfFrameFromStartVideo + ". Равно - " + (System.currentTimeMillis() - startReadByteTime));
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
                        } else {
                            try {
                                Thread.sleep(1);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            buffBytesThread.setName("Buff byte VIDEO PLAYER for video panel " + numberVideoPanel);
        }
    }

    private void buffImage(int number) {
        if (frameInBuffDeque.size() > 0) {
            int frameNumberFromBuff;

            frameNumberFromBuff = currentFrameNumber + 10;

            int count = 0;

            for (int i = frameNumberFromBuff; ; i += number) {
                if (framesBytesInBuffMap.containsKey(i)) {
                    if (!framesImagesInBuffMap.containsKey(i)) {
                        for (Integer integer : buffImageThreadMap.keySet()) {
                            if (buffImageThreadMap.get(integer) == null) {
                                int finalK = i;
                                Thread thread = new Thread(() -> {
                                    long startThread = System.currentTimeMillis();
                                    framesImagesInBuffMap.put(finalK, readImage(framesBytesInBuffMap.get(finalK)));
                                    framesBytesInBuffMap.remove(finalK);
                                    buffImageThreadMap.put(integer, null);
//                                    System.out.println("Работа потока буферизатора - " + (System.currentTimeMillis() - startThread));
                                });
                                buffImageThreadMap.put(integer, thread);
                                thread.setName(" Image Buff Thread . VIDEO PLAYER  Panel number" + numberVideoPanel + ". Frame number - " + finalK);
                                thread.start();
                                break;
                            }
                        }
                        count++;
                    } else {
                        i--;
                    }
                } else {
                    count++;
                }

                if (count > 1) {
                    break;
                }
            }
        }
    }

    private void readBytesImageToBuff() {
        while (!setPosition && VideoPlayer.isShowVideoPlayer()) {
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
                System.out.println("Битая картинка");
            }
        }
//        System.out.println("Создаем изображение - " + (System.currentTimeMillis() - start));
        return bufferedImage;
    }

    private BufferedImage processImage(BufferedImage bi, int maxWidth, int maxHeight) {
        int width;
        int height;

        if (maxWidth / 1.77 > maxHeight) {
            height = maxHeight;
            width = (int) (height * 1.77);
        } else {

            width = maxWidth;
            height = (int) (width / 1.77);
        }

        BufferedImage bi2 = null;
        double max;
        int size;
        int ww = width - bi.getWidth();
        int hh = height - bi.getHeight();

        if (ww < 0 || hh < 0) {
            if (ww < hh) {
                max = width;
                size = bi.getWidth();
            } else {
                max = height;
                size = bi.getHeight();
            }

            if (size > 0 && size > max) {
                double trans = 1.0 / (size / max);
                AffineTransform tr = new AffineTransform();
                tr.scale(trans, trans);
                AffineTransformOp op = new AffineTransformOp(tr, AffineTransformOp.TYPE_BICUBIC);
                Double w = bi.getWidth() * trans;
                Double h = bi.getHeight() * trans;
                bi2 = new BufferedImage(w.intValue(), h.intValue(), bi.getType());
                op.filter(bi, bi2);
            }
        }

        if (bi2 != null) {
            return bi2;
        } else {
            return bi;
        }
    }

    void showVideo() {
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
        if (blockHaveVideo) {
            return buffBytesThread;
        } else {
            return null;
        }
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
        this.fullSize = fullSize;
        partExportPanel.setVisible(fullSize);
    }
}