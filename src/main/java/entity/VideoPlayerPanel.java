package entity;

import ui.camera.CameraPanel;
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
    private int width;
    private int height;
    private int numberVideoPanel;

    private boolean mainPanel;
    private int position;

    private int FPS;
    private File folder;
    private Thread showVideoThread;
    private Thread mainVideoThread;

    private int changeImage;
    private int nextImageInt;
    private int prewImageIng;
    private int countImageFramesChangeIng;
    private int positionForNextPreviousImage;

    private BufferedImage image;
    private int x = 0;
    private int t = 0;

    private VideoPlayerToShowOneVideo videoPlayerToShowOneVideo;
    private JLayer<JPanel> videoStreamLayer;
    private JLabel label;
    private boolean blockHaveVideo = true;
    private boolean videoPlay = true;
    private BufferedInputStream bufferedInputStream = null;
    private FileInputStream fileInputStream = null;
    private ByteArrayOutputStream temporaryStream = null;

    private Map<Integer, byte[]> buffMap;
    private Deque<Integer> buffDeque;
    //    private int numberImageInBuffer;
    private int positionImage;

    private Map<File, Integer> framesInFiles;
    private List<File> filesList;

    private int totalCountFrames;
    private boolean showVideoNow;

    private JPanel videoPanel;
    private JPanel exportPanel;
    private JLabel currentFrameLabel;


    VideoPlayerPanel(File folderWithTemporaryFiles, int numberVideoPanel) {

        this.numberVideoPanel = numberVideoPanel;
        this.folder = folderWithTemporaryFiles;
        buffMap = new HashMap<>();
        buffDeque = new ConcurrentLinkedDeque<>();

        filesList = new ArrayList<>();
        if (folderWithTemporaryFiles != null) {
            framesInFiles = new HashMap<>();
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    filesList.add(file);
                    try {
                        String name = file.getName();
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
                System.out.println("Всего кадров " + totalCountFrames);
                Collections.sort(filesList);
            }

            videoPlayerToShowOneVideo = new VideoPlayerToShowOneVideo();

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
            label = new JLabel("Натистіть PLAY");
        } else {
            blockHaveVideo = false;
            label = new JLabel("Камери не працювали");
        }

        videoPanel = new JPanel();

        videoPanel.setPreferredSize(new Dimension(377, 219));
        videoPanel.setBorder(BorderFactory.createEtchedBorder());

        exportPanel = new JPanel();
        exportPanel.setPreferredSize(new Dimension(150, 219));
        exportPanel.setBorder(BorderFactory.createEtchedBorder());

        JLabel label = new JLabel("ЕКСПОРТ");
        label.setPreferredSize(new Dimension(150, 30));
        label.setHorizontalTextPosition(SwingConstants.CENTER);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        JButton exportButton = new JButton("ВІДЕО");
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

        JButton imageButton = new JButton("КАДР");
        imageButton.setPreferredSize(new Dimension(120, 50));
        imageButton.addActionListener((e) -> {
            Thread thread = new Thread(() -> {
                BufferedImage image = readImage(buffMap.get(positionImage));
                if (image != null) {
                    String path = MainFrame.getPath() + System.currentTimeMillis() + "-" + numberVideoPanel + ".jpg";
                    File file = new File(path);
                    try {
                        if (file.createNewFile()) {
                            ImageIO.write(image, "jpg", file);
                        }
                    } catch (Exception xe) {
                        MainFrame.showInformMassage("Не вдалось зберегти", new Color(171, 55, 49));
                        xe.printStackTrace();
                    }
                } else {
                    MainFrame.showInformMassage("Не вдалось зберегти", new Color(171, 40, 33));
                }
            });
            thread.start();
        });

        currentFrameLabel = new JLabel("  Кадр:   0");
        currentFrameLabel.setPreferredSize(new Dimension(150, 15));
        JLabel totalFrameLabel = new JLabel("  Всього кадрів:   " + totalCountFrames);
        totalFrameLabel.setPreferredSize(new Dimension(150, 15));

        JLabel totalSecondsLabel = new JLabel("  Всього секунд:   "+ filesList.size());
        totalSecondsLabel.setPreferredSize(new Dimension(150, 15));

        JPanel partExportPanel = new JPanel(new FlowLayout());
        partExportPanel.setBorder(BorderFactory.createLineBorder(Color.lightGray));
        partExportPanel.setPreferredSize(new Dimension(255, 283));
        JLabel partExportLabel = new JLabel("Частковий експорт");
        partExportLabel.setPreferredSize(new Dimension(200, 25));
        partExportLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel startPartExportLabel = new JLabel("Перший кадр");
        startPartExportLabel.setPreferredSize(new Dimension(100, 25));
        JTextField startPartExportTextField = new JTextField();
        startPartExportTextField.setPreferredSize(new Dimension(70, 25));

        JLabel endPartExportLabel = new JLabel("Останній кадр");
        endPartExportLabel.setPreferredSize(new Dimension(100, 25));
        JTextField endPartExportTextField = new JTextField();
        endPartExportTextField.setPreferredSize(new Dimension(70, 25));

        JLabel informPartExportLabel = new JLabel("<html>Вкажіть номер першого <br> та останнього кадру</html>");
        informPartExportLabel.setPreferredSize(new Dimension(200, 50));
        informPartExportLabel.setHorizontalAlignment(SwingConstants.CENTER);
        informPartExportLabel.setHorizontalTextPosition(SwingConstants.CENTER);

        JButton partExportButton = new JButton("Зберегти");
        partExportButton.setPreferredSize(new Dimension(100, 50));
        partExportButton.addActionListener((w) -> {
            Thread save = new Thread(()->{
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

                if(startFrame<1||endFrameInt<1){
                    continueSave = false;
                    informPartExportLabel.setText("<html>Не можливо <br> зберегти кадри з "+startFrame+" по "+endFrameInt+"<hr></html>");
                }

                if(endFrameInt>totalCountFrames||endFrameInt>totalCountFrames){
                    continueSave = false;
                    informPartExportLabel.setText("<html>Запис мае меньше <br> кадрів ніж "+startFrame+" чи "+ endFrameInt+"<hr></html>");
                }

                if(startFrame>endFrameInt){
                    continueSave = false;
                    informPartExportLabel.setText("<html>Перший кадр знаходиться після останнього<hr></html>");
                }

                if (continueSave) {
                    boolean finkStartFile = false;
                    int firstFile = 0;
                    int lastFile = 0;

                    int totalFrames = 0;
                    if (endFrameInt != 0) {
                        for (int i = 0; i < filesList.size(); i++) {
                            File fileToRead = filesList.get(i);
                            Integer integer = framesInFiles.get(fileToRead);
                            if (finkStartFile) {
                                totalFrames += integer;
                                if (totalFrames + integer > endFrameInt) {
                                    lastFile = i;
                                    break;
                                }
                            } else {
                                if (totalFrames + integer > startFrame) {
                                    firstFile = i;
                                    finkStartFile = true;
                                } else {
                                    totalFrames += integer;
                                }
                            }
                        }
                    }

                    if (lastFile != 0) {
                        List<File> filesToSave = new ArrayList<>();
                        int totalFramesToSave = 0;

                        for (int i = firstFile; i < lastFile; i++) {
                            File file = filesList.get(i);
                            totalFramesToSave+=framesInFiles.get(file);
                            filesToSave.add(file);
                        }

                        String name = folder.getName();
                        String[] split = name.split("-");
                        long dateLong = Long.parseLong(split[0]);

                        Date date = new Date(dateLong);
                        SimpleDateFormat dateFormat = new SimpleDateFormat();
                        dateFormat.applyPattern("dd MMMM yyyy,HH-mm-ss");
                        String dateString = dateFormat.format(date);

                        String[] fpsSplit = split[1].split("\\.");
                        String numberOfGroupCameraString = fpsSplit[0].substring(0, 1);
                        int integer = 0;

                        try {
                            integer = Integer.parseInt(numberOfGroupCameraString);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        int i = fpsSplit[0].indexOf(")");
                        String totalFpsString = fpsSplit[0].substring(2, i);
                        int totalFPS = Integer.parseInt(totalFpsString);

                        BufferedImage imageToConnect = null;
                        String absolutePathToImage = folder.getAbsolutePath().replace(".tmp",".jpg");
                        File imageFile = new File(absolutePathToImage);
                        if(imageFile.exists()){
                            try {
                                imageToConnect = ImageIO.read(new FileInputStream(imageFile));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        String pathToVideo = MainFrame.getPath() + "\\" + dateString + ". Секунди з " + firstFile + " по " + lastFile + ". Группа камер -" + numberVideoPanel + ".mp4";
                        informPartExportLabel.setText("<html>Буде збережено <br> Cекунд: "+(lastFile-firstFile)+".<br> Кадрів: "+totalFramesToSave+".<hr></html>");
                        MainVideoCreator.savePartOfVideoFile(pathToVideo, filesToSave,totalFPS,imageToConnect);
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


        exportPanel.add(label);
        exportPanel.add(exportButton);
        exportPanel.add(imageButton);
        exportPanel.add(currentFrameLabel);
        exportPanel.add(totalFrameLabel);
        exportPanel.add(totalSecondsLabel);
        exportPanel.add(partExportPanel);
        if (numberVideoPanel % 2 != 0) {
            this.add(exportPanel);
            this.add(videoPanel);
        } else {
            this.add(videoPanel);
            this.add(exportPanel);
        }

        this.setLayout(new FlowLayout());
        this.setPreferredSize(new Dimension(540, 227));
        this.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        createThread();
        setWidthAndHeight(364, 205);
    }

    void setWidthAndHeight(int width, int height) {
        if (width < 400) {
            this.setPreferredSize(new Dimension(540, 227));
            videoPanel.setPreferredSize(new Dimension(377, 219));
            exportPanel.setPreferredSize(new Dimension(150, 219));
        } else {
            this.setPreferredSize(new Dimension(1085, 457));
            videoPanel.setPreferredSize(new Dimension(800, 448));
            exportPanel.setPreferredSize(new Dimension(270, 448));
        }

        if (videoPlayerToShowOneVideo != null) {
            videoPlayerToShowOneVideo.setPreferredSize(new Dimension(width, height));
        }

        this.width = width - 5;
        this.height = height - 5;
    }

    private void setCurrentFrameNumber(int currentFrameLabelText) {
        currentFrameLabel.setText("  Кадр:    " + currentFrameLabelText);
    }

    boolean isBlockHaveVideo() {
        return blockHaveVideo;
    }

    class MyLayer extends LayerUI<JPanel> {
        BufferedImage bufferedImage;

        public MyLayer(BufferedImage bufferedImage) {
            this.bufferedImage = bufferedImage;
        }

        @Override
        public void paint(Graphics g, JComponent c) {
            super.paint(g, c);

            if (bufferedImage != null) {
                g.drawImage(CameraPanel.animateCircle(processImage(bufferedImage, width, height), BufferedImage.TYPE_INT_ARGB), 0, 0, null);
                g.dispose();
            }

        }

//            if (MainFrame.imagesForBlock.get(numberVideoPanel) != null) {
//                g.drawImage(CameraPanel.animateCircle(processImage(MainFrame.imagesForBlock.get(numberVideoPanel), width, height), BufferedImage.TYPE_INT_ARGB), 0, 0, null);
//                g.dispose();
//            }
//    }
    }

    private void createThread() {
        label = new JLabel("Натистіть PLAY");
        if (blockHaveVideo) {
            showVideoThread = new Thread(() -> {
                while (blockHaveVideo) {

                    if ((VideoPlayer.isPLAY() || VideoPlayer.isSetPOSITION()) && showVideoNow) {
                        if (!videoPlay) {
                            videoPlay = true;
                            videoPanel.removeAll();
                            videoPanel.add(videoStreamLayer);
                            videoPanel.validate();
                            videoPanel.repaint();
                        }

                        if (folder != null) {
                            closeStreams();
                            if (VideoPlayer.isSetPOSITION()) {
                                showFrames(VideoPlayer.getPosition());
                            } else {
                                showFrames(0);
                            }
                        }
                    } else {
                        if (VideoPlayer.isSTOP()) {

                            position = 0;
                            changeImage = 0;
                            positionImage = 0;
                            buffMap.clear();
                            buffDeque.clear();

                            if (mainPanel) {
                                VideoPlayer.setSTOP(true);
                                VideoPlayer.setPLAY(false);
                                VideoPlayer.setPAUSE(false);
                                setCurrentFrameNumber(0);
                                VideoPlayer.setCurrentFrameLabelText(0, 0);
                                VideoPlayer.setCountDoNotShowImageToZero();
                                VideoPlayer.setSpeedToZero();
                            } else {
                                setCurrentFrameNumber(0);
                            }
                            VideoPlayer.setSliderPosition(0);
                        }
                        if (videoPlay) {
                            videoPlay = false;
                            videoPanel.remove(videoStreamLayer);
                            videoPanel.add(label);
                            videoPanel.validate();
                            videoPanel.repaint();
                        }

                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    if (!VideoPlayer.isShowVideoPlayer()) {
                        blockHaveVideo = false;
                        closeStreams();
                    }
                }
                this.mainPanel = false;
            });
            showVideoThread.setName("ShowVideoThread. Player " + numberVideoPanel);
        }
    }

    private BufferedImage readImage(byte[] imageBytes) {
        BufferedImage bufferedImage = null;
        if (imageBytes != null) {
            try {
                ByteArrayInputStream inputImageStream = new ByteArrayInputStream(imageBytes);
                BufferedInputStream bufferedInputImageStream = new BufferedInputStream(new ByteArrayInputStream(imageBytes));
                bufferedImage = ImageIO.read(bufferedInputImageStream);
                inputImageStream.close();
                bufferedInputImageStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Пустой массив");
        }
        return bufferedImage;
    }

    private void readBytesForImage() {
        while (true) {
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
                buffMap.put(++positionImage, imageBytes);
                positionForNextPreviousImage = positionImage;
                buffDeque.addFirst(positionImage);
                while (buffDeque.size() > 500) {
                    Integer integer = buffDeque.pollLast();
                    buffMap.remove(integer);
                }

                if (changeImage == VideoPlayer.getCountDoNotShowImage()) {
                    try {
                        ByteArrayInputStream inputImageStream = new ByteArrayInputStream(imageBytes);
                        BufferedInputStream bufferedInputImageStream = new BufferedInputStream(new ByteArrayInputStream(imageBytes));
                        image = ImageIO.read(bufferedInputImageStream);
                        inputImageStream.close();
                        bufferedInputImageStream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (image != null) {
                        videoPlayerToShowOneVideo.setBufferedImage(processImage(image, width, height));
                        videoPlayerToShowOneVideo.repaint();

                        videoStreamLayer.repaint();
                        changeImage = 0;
                    }
                } else {
                    changeImage++;
                }

                if (mainPanel) {
                    FPS++;
                }

                try {
                    Thread.sleep(VideoPlayer.getSpeed());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return;
            }

            if (x < 0) {
                return;
            }
        }
    }

    private void showFrames(int startPositionPercent) {

        int startFrame = totalCountFrames * startPositionPercent / 1000;
        int startFileNumber = 0;
        int totalFrames = 0;

        System.out.println(" Позиция " + startPositionPercent);
        System.out.println(" Всего кадров " + totalCountFrames);
        System.out.println(" Начальный кадр " + startFrame);

        for (int i = 0; i < filesList.size(); i++) {
            File fileToRead = filesList.get(i);
            Integer integer = framesInFiles.get(fileToRead);

            if (startFrame < integer) {
                if (startPositionPercent > 0) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    VideoPlayer.setSetPOSITION(false);
                }
                positionImage = 0;
                buffMap.clear();
                buffDeque.clear();
                break;
            } else {
                if (totalFrames + integer > startFrame) {
                    startFileNumber = i;
                    if (startPositionPercent > 0) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        VideoPlayer.setSetPOSITION(false);
                    }
                    buffMap.clear();
                    buffDeque.clear();
                    break;
                } else {
                    totalFrames += integer;
                }
                positionImage = totalFrames;
            }
        }

        System.out.println(" Текущий кадр воспроизведения " + positionImage);
        System.out.println(" Всего кадров " + totalCountFrames);
        System.out.println(" Начальный файл " + startFileNumber);

        for (int i = startFileNumber; i < filesList.size(); i++) {
            File fileToRead = filesList.get(i);
            try {
                fileInputStream = new FileInputStream(fileToRead);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            if (fileInputStream != null) {
                bufferedInputStream = new BufferedInputStream(fileInputStream);
                temporaryStream = new ByteArrayOutputStream(65535);
                while (x >= 0 && VideoPlayer.isShowVideoPlayer()) {

                    if (mainPanel) {
                        VideoPlayer.setCurrentFrameLabelText(positionImage, buffMap.size());
                    }
                    setCurrentFrameNumber(positionImage);


                    if (VideoPlayer.isSetPOSITION()) {
                        return;
                    } else if (VideoPlayer.isPLAY()) {
                        if (countImageFramesChangeIng == 0) {
                            readBytesForImage();
                        } else {
                            positionImage++;
                            image = readImage(buffMap.get(positionForNextPreviousImage - (--countImageFramesChangeIng)));

                            if (image != null) {
                                videoPlayerToShowOneVideo.setBufferedImage(processImage(image, width, height));
                                videoPlayerToShowOneVideo.repaint();
                            }
                        }
                    } else if (VideoPlayer.isPAUSE()) {
                        if (VideoPlayer.isNextIMAGE()) {
                            if (nextImageInt != VideoPlayer.getNextImagesInt()) {
                                nextImageInt = VideoPlayer.getNextImagesInt();
                                if (countImageFramesChangeIng == 0) {
                                    readBytesForImage();
                                } else {
                                    positionImage++;
                                    image = readImage(buffMap.get(positionForNextPreviousImage - (--countImageFramesChangeIng)));
                                    if (image != null) {
                                        videoPlayerToShowOneVideo.setBufferedImage(processImage(image, width, height));
                                        videoPlayerToShowOneVideo.repaint();
                                    }
                                }
                            } else {
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else if (VideoPlayer.isPrewIMAGE()) {
                            if (prewImageIng != VideoPlayer.getPrewImagesInt()) {
                                prewImageIng = VideoPlayer.getPrewImagesInt();
                                int numberOfFrame = positionForNextPreviousImage - (++countImageFramesChangeIng);
                                if (countImageFramesChangeIng <= buffMap.size()) {
                                    image = readImage(buffMap.get(numberOfFrame));
                                } else {
                                    if (mainPanel) {
                                        MainFrame.showInformMassage("Немае зображень в буфері", new Color(199, 29, 25));
                                    }
                                }
                                positionImage--;
                                if (image != null) {
                                    videoPlayerToShowOneVideo.setBufferedImage(processImage(image, width, height));
                                    videoPlayerToShowOneVideo.repaint();
                                }
                            } else {
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    } else if (VideoPlayer.isSTOP()) {
                        return;
                    }

                    if (mainPanel) {

                        int percent = (int) ((positionImage * 1000.0) / totalCountFrames);
//                        System.out.println(" Текущий кадр воспроизведения " + positionImage);
//                        System.out.println(" Всего кадров " + totalCountFrames);
//                        System.out.println(" Процентов " + percent);
//                        System.out.println("=====================================");
                        if (position != percent) {
                            position = percent;
                            VideoPlayer.setSliderPosition(position);
                        }
                    }
                }
                x = 0;
            }
            closeStreams();
        }
        if (mainPanel) {
            VideoPlayer.setSTOP(true);
            VideoPlayer.setPLAY(false);
            VideoPlayer.setPAUSE(false);
            VideoPlayer.setSliderPosition(0);
            setCurrentFrameNumber(0);
            VideoPlayer.setCurrentFrameLabelText(0, 0);
            VideoPlayer.setCountDoNotShowImageToZero();
            VideoPlayer.setSpeedToZero();
        } else {
            setCurrentFrameNumber(0);
        }
    }

    void setMainPanel(boolean mainPanel) {
        this.mainPanel = mainPanel;
        if (mainPanel) {
            if (mainVideoThread == null) {
                mainVideoThread = new Thread(() -> {
                    while (this.mainPanel) {
                        VideoPlayer.FPSLabel.setText("FPS - " + FPS);
                        VideoPlayer.FPSLabel.repaint();

                        FPS = 0;
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        if (!VideoPlayer.isShowVideoPlayer()) {
                            break;
                        }
                    }
                    mainVideoThread = null;
                });
                mainVideoThread.setName("MainVideoPlayerThread. Number " + numberVideoPanel);
                mainVideoThread.start();
                this.setBorder(BorderFactory.createLineBorder(Color.green));
            }
        } else {
            this.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        }
    }

    private BufferedImage processImage(BufferedImage bi, int maxWidth, int maxHeight) {
        BufferedImage bi2 = null;
        double max;
        int size;
        int ww = maxWidth - bi.getWidth();
        int hh = maxHeight - bi.getHeight();

        if (ww < 0 || hh < 0) {
            if (ww < hh) {
                max = maxWidth;
                size = bi.getWidth();
            } else {
                max = maxHeight;
                size = bi.getHeight();
            }

            if (size > 0 && size > max) {
                double trans = 1.0 / (size / max);
                AffineTransform tr = new AffineTransform();
                tr.scale(trans, trans);
                AffineTransformOp op = new AffineTransformOp(tr, AffineTransformOp.TYPE_BILINEAR);
                Double w = bi.getWidth() * trans;
                Double h = bi.getHeight() * trans;
                bi2 = new BufferedImage(w.intValue(), h.intValue(), bi.getType());
                op.filter(bi, bi2);
            }
        } else {
            return bi;
        }
        return bi2;
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
        return showVideoThread;
    }

    String getFileName() {
        if (folder != null) {
            return folder.getName();
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
}
