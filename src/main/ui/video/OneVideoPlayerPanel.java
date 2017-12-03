package ui.video;

import entity.MainVideoCreator;
import ui.camera.CameraPanel;
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

/**
 * class to show video from one cameras group
 */
class OneVideoPlayerPanel extends JPanel {
    /**
     * link for folder with bytes files
     */
    private File folder;


    /**
     * interface variables
     */
    private VideoPlayerToShowOneVideo videoPlayerToShowOneVideo;
    private JLayer<JPanel> videoStreamLayer;
    private JLabel informLabel;
    private JPanel videoPanel;
    private JLabel currentFrameLabel;
    private JPanel partExportPanel;
    private JLabel currentFPSLabel;


    /**
     * mark contain video files for part or not
     */
    private boolean blockHaveVideo = true;
    private int numberVideoPanel;


    /**
     * variables to read bytes files
     */
    private int x = 0;
    private int t = 0;
    private BufferedInputStream bufferedInputStream = null;
    private FileInputStream fileInputStream = null;
    private ByteArrayOutputStream temporaryStream = null;


    /**
     * link to file as key, count frames in each file as value
     */
    private Map<File, Integer> framesInFiles;
    private List<File> filesList;

    /**
     * total frames in video
     */
    private int totalCountFrames;
    /**
     * mark to show video now, if other OneVideoPlayer is full size, this one will be not worked
     */
    private boolean showVideoNow;

    /**
     * collections of thread. 10 Items. Each thread will create backGround from bytes in buffer ("framesBytesInBuffMap" ),
     * mark number of this frame and save it to backGround buffer - "framesImagesInBuffMap". After replace oun key to NULL in buffImageThreadMap.
     * <p>
     * use this to create some future images before VideoPlayer ask to show them. And when OneVideoPlayer should be draw this backGround,
     * it will not spend time to create it.
     */
    private Map<Integer, Thread> buffImageThreadMap;
    private Map<Integer, BufferedImage> framesImagesInBuffMap;

    /**
     * thread read bytes arrays(Image bytes) and add it to buffer "framesBytesInBuffMap" .
     * always have in buffer frames number from "current frame number" till  "currentFrameNumber+500"
     */
    private Thread buffBytesThread;
    private Map<Integer, byte[]> framesBytesInBuffMap;
    private Deque<Integer> frameInBuffDeque;
    private boolean allFilesIsInBuff;
    private int numberOfFrameFromStartVideo = 0;
    private int currentFrameNumber = 0;


    /**
     * this thread will be created and run each time, when it will be null, and VideoPlayer ask this OneVideoPlayerPanel to show frame
     * it takes a video part number(after splitting by events) and percent number of frame,
     * find this frame in this video, find the file with this frame, read bytes, create backGround, draw it to panel
     * and set "showFrameThread = null" to be possible show next frame
     */
    private Thread showFrameThread;
    /**
     * update real time FPS for video, start buffering images for future showing,
     * clean framesImagesInBuffMap, delete images from buffer, which already was showed
     */
    private Thread FPSThread;

    /**
     * realTime FPS, how many images program was showed by one second
     */
    private int FPS = 0;

    /**
     * number of frames, when was lightning
     */
    private List<Integer> eventFrameNumberList;
    /**
     * class will split video by frame count and numbers of "eventFrameNumberList". After splitting will save
     * parts number as key, and count of frames in part as value it "tempEventsMapPartSize"
     */
    private Map<Integer, Integer> tempEventsMapPartSize;


    private BufferedImage backGround = null;

    private boolean setPosition;
    private int startFrame;
    private int startFileNumber = 0;

    private boolean fullSize;

    OneVideoPlayerPanel(File folderWithFilesBytesToShowVideo, int numberVideoPanel) {
        this.numberVideoPanel = numberVideoPanel;
        this.folder = folderWithFilesBytesToShowVideo;
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
        if (folderWithFilesBytesToShowVideo != null) {
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

            tempEventsMapPartSize = new HashMap<>();
            int lastFrame = 0;
            for (int i = 0; i < eventFrameNumberList.size(); i++) {
                Integer integer = eventFrameNumberList.get(i);
                tempEventsMapPartSize.put(i, (integer - lastFrame));
                lastFrame = integer;
                if (i == eventFrameNumberList.size() - 1) {
                    tempEventsMapPartSize.put(i + 1, (totalCountFrames - lastFrame));
                }
            }

            String absolutePathToImage = folderWithFilesBytesToShowVideo.getAbsolutePath().replace(".tmp", ".jpg");
            File imageFile = new File(absolutePathToImage);
            if (imageFile.exists()) {
                try {
                    FileInputStream fileInputStream = new FileInputStream(imageFile);
                    backGround = ImageIO.read(fileInputStream);
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            LayerUI<JPanel> layerUI = new OneVideoPlayerPanel.MyLayer(backGround);
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
        exportButton.setPreferredSize(new Dimension(178, 50));
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
        imageButton.setPreferredSize(new Dimension(87, 50));
        imageButton.addActionListener((e) -> {
            saveImage(false);
        });

        JButton imageBackGroundButton = new JButton(MainFrame.getBundle().getString("saveframebutton") + " +");
        imageBackGroundButton.setPreferredSize(new Dimension(87, 50));
        imageBackGroundButton.addActionListener((e) -> {
            saveImage(true);
        });

        currentFrameLabel = new JLabel(MainFrame.getBundle().getString("framenumberlabel"));
        currentFrameLabel.setPreferredSize(new Dimension(150, 15));
        JLabel totalFrameLabel = new JLabel(MainFrame.getBundle().getString("totalframecountlabel") + totalCountFrames);
        totalFrameLabel.setPreferredSize(new Dimension(150, 15));

        currentFPSLabel = new JLabel();
        currentFPSLabel.setPreferredSize(new Dimension(150, 15));

        JLabel totalFPSLabel = new JLabel(MainFrame.getBundle().getString("totalword") + " FPS: " + totalFPSForFile);
        totalFPSLabel.setPreferredSize(new Dimension(150, 15));

        partExportPanel = new JPanel(new FlowLayout());
        partExportPanel.setBorder(BorderFactory.createLineBorder(Color.lightGray));
        partExportPanel.setPreferredSize(new Dimension(190, 300));
        JLabel partExportLabel = new JLabel(MainFrame.getBundle().getString("partsavelabel"));
        partExportLabel.setPreferredSize(new Dimension(190, 25));
        partExportLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel startPartExportLabel = new JLabel(MainFrame.getBundle().getString("firstframelabel"));
        startPartExportLabel.setPreferredSize(new Dimension(100, 25));
        JLabel endPartExportLabel = new JLabel(MainFrame.getBundle().getString("lastframelabel"));
        endPartExportLabel.setPreferredSize(new Dimension(100, 25));

        JTextField startPartExportTextField = new JTextField();
        startPartExportTextField.setPreferredSize(new Dimension(70, 25));
        JTextField endPartExportTextField = new JTextField();
        endPartExportTextField.setPreferredSize(new Dimension(70, 25));

        JLabel informPartExportLabel = new JLabel(MainFrame.getBundle().getString("firstinformvideoplayerlabel"));
        informPartExportLabel.setFocusable(false);
        informPartExportLabel.setPreferredSize(new Dimension(190, 50));
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
        exportPanel.add(imageBackGroundButton);
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


    private void saveImage(boolean saveBackGround) {
        Thread thread = new Thread(() -> {
            int i = currentFrameNumber;
            BufferedImage image = readImage(framesBytesInBuffMap.get(i));
            if (image != null) {
                String path = MainFrame.getPath() + "\\" + System.currentTimeMillis() + "-" + numberVideoPanel + ".jpg";
                File file = new File(path);
                System.out.println("Путь к файлу - " + path);
                try {
                    if (file.createNewFile()) {
                        if (backGround == null || !saveBackGround) {
                            ImageIO.write(image, "jpg", file);
                        } else {
                            BufferedImage connectedImage = MainVideoCreator.connectImage(image, backGround, CameraPanel.getOpacity());
                            ImageIO.write(connectedImage, "jpg", file);
                        }
                    }
                    MainFrame.showInformMassage(MainFrame.getBundle().getString("saveoneframenumber") + i, Color.DARK_GRAY);
                } catch (Exception xe) {
                    MainFrame.showInformMassage(MainFrame.getBundle().getString("cannotsaveinform"), new Color(171, 40, 33));
                    xe.printStackTrace();
                }
            } else {
                MainFrame.showInformMassage(MainFrame.getBundle().getString("cannotsaveinform"), new Color(171, 40, 33));
            }
        });
        thread.start();
    }

    /**
     * draw frame to video panel
     *
     * @param partNumber                  - part of video, after splitting by events count
     * @param currentFramePositionPercent - frame position in this part
     */
    void showFrameNumber(int partNumber, int currentFramePositionPercent) {
        if (showFrameThread == null) {
            showFrameThread = new Thread(() -> {
                Integer sizeOfPart = tempEventsMapPartSize.get(partNumber);
                double i = (double) currentFramePositionPercent / 100000;
                int frameToShowInPart = (int) (i * sizeOfPart);

                Integer startFrameOFPart;
                if (partNumber == 0) {
                    startFrameOFPart = 0;
                } else {
                    startFrameOFPart = eventFrameNumberList.get(partNumber - 1);
                }

                int frameToShowNumber = startFrameOFPart + frameToShowInPart;
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
                                videoPlayerToShowOneVideo.setBufferedImage(CameraPanel.processImage(image, videoPanel.getWidth(), videoPanel.getHeight()));
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

    /**
     * method to create images for future showing and save it to Image buffer
     *
     * @param number - from 1 till 10
     */
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
                                    framesImagesInBuffMap.put(finalK, readImage(framesBytesInBuffMap.get(finalK)));
                                    framesBytesInBuffMap.remove(finalK);
                                    buffImageThreadMap.put(integer, null);
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

    /**
     * read bytes for one backGround from files, and save it to buffer
     */
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

    /**
     * create backGround from byte array
     *
     * @param imageBytes - array of bytes
     * @return - buffered backGround
     */
    private BufferedImage readImage(byte[] imageBytes) {
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
        return bufferedImage;
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

    /**
     * write a current frame number on panel
     *
     * @param currentFrameLabelText - current frame number
     */
    private void setCurrentFrameNumber(int currentFrameLabelText) {
        currentFrameLabel.setText(MainFrame.getBundle().getString("framenumberlabel") + currentFrameLabelText);
    }

    boolean isBlockHaveVideo() {
        return blockHaveVideo;
    }

    /**
     * use it to set background backGround
     */
    class MyLayer extends LayerUI<JPanel> {
        BufferedImage bufferedImage;

        MyLayer(BufferedImage bufferedImage) {
            this.bufferedImage = bufferedImage;
        }

        @Override
        public void paint(Graphics g, JComponent c) {
            super.paint(g, c);
            if (bufferedImage != null) {
                BufferedImage image = CameraPanel.changeOpacity(CameraPanel.processImage(bufferedImage, videoPanel.getWidth(), videoPanel.getHeight()));
                if (image != null) {
                    int x = 0;
                    int imageWidth = image.getWidth();
                    int panelWidth = videoPanel.getWidth();
                    if (panelWidth > imageWidth) {
                        x = (panelWidth - imageWidth) / 2;
                    }
                    g.drawImage(image, x, 0, null);
                }
                g.dispose();
            }
        }
    }

    /**
     * use it to draw backGround here
     */
    class VideoPlayerToShowOneVideo extends JPanel {
        private BufferedImage bufferedImage;

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (bufferedImage != null) {
                int x = 0;

                int imageWidth = bufferedImage.getWidth();
                int panelWidth = videoPanel.getWidth();
                if (panelWidth > imageWidth) {
                    x = (panelWidth - imageWidth) / 2;
                }
                g.drawImage(bufferedImage, x, 0, null);
            }
        }

        private void setBufferedImage(BufferedImage bufferedImage) {
            this.bufferedImage = bufferedImage;
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