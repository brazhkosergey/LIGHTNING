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
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

class VideoPlayerPanel extends JPanel {

    private int whitePercent = -1;
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

    VideoPlayerPanel(File folderWithTemporaryFiles, int numberVideoPanel) {
        this.numberVideoPanel = numberVideoPanel;
        this.folder = folderWithTemporaryFiles;
        buffMap = new HashMap<>();
        buffDeque = new ConcurrentLinkedDeque<>();
        this.setLayout(new FlowLayout(FlowLayout.CENTER));
        this.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));

        if (folderWithTemporaryFiles != null) {
            framesInFiles = new HashMap<>();
            filesList = new ArrayList<>();
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
                Collections.sort(filesList);
            }

            videoPlayerToShowOneVideo = new VideoPlayerToShowOneVideo();
            LayerUI<JPanel> layerUI = new VideoPlayerPanel.MyLayer();
            videoStreamLayer = new JLayer<JPanel>(videoPlayerToShowOneVideo, layerUI);
            label = new JLabel("Натистіть PLAY");
        } else {
            blockHaveVideo = false;
            label = new JLabel("Камери не працювали");
            this.add(label);
        }
        createThread();
        setWidthAndHeight(535, 222);
    }

    void setWidthAndHeight(int width, int height) {
        this.setPreferredSize(new Dimension(width + 5, height + 5));
        if (videoPlayerToShowOneVideo != null) {
            videoPlayerToShowOneVideo.setPreferredSize(new Dimension(width, height));
        }

        this.width = width - 4;
        this.height = height - 4;
    }

    class MyLayer extends LayerUI<JPanel> {
        @Override
        public void paint(Graphics g, JComponent c) {
            super.paint(g, c);
            if (MainFrame.imagesForBlock.get(numberVideoPanel) != null) {
                g.drawImage(CameraPanel.animateCircle(processImage(MainFrame.imagesForBlock.get(numberVideoPanel), width, height), BufferedImage.TYPE_INT_ARGB), 0, 0, null);
                g.dispose();
            }
        }
    }

    private void createThread() {
        label = new JLabel("Натистіть PLAY");
        if (blockHaveVideo) {
            showVideoThread = new Thread(() -> {
                while (blockHaveVideo) {

                    if ((VideoPlayer.isPLAY()||VideoPlayer.isSetPOSITION()) && showVideoNow) {
                        if (!videoPlay) {
                            videoPlay = true;
                            this.remove(label);
                            videoStreamLayer.repaint();
                            this.add(videoStreamLayer);
                            this.revalidate();
                            this.repaint();
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
                        if(VideoPlayer.isSTOP()){
                            position = 0;
                            changeImage = 0;
                            positionImage = 0;
                            buffMap.clear();
                            buffDeque.clear();
                        }
                        if (videoPlay) {
                            videoPlay = false;
                            this.remove(videoStreamLayer);
                            this.add(label);
                            this.revalidate();
                            this.repaint();
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

    private BufferedImage readImage(byte[] imageBytes){
        BufferedImage bufferedImage = null;
        if(imageBytes!=null){
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
                buffMap.put(positionImage++, imageBytes);
                positionForNextPreviousImage = positionImage;
                buffDeque.addFirst(positionImage);
                while (buffDeque.size()>500){
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
        int startFrame = totalCountFrames * startPositionPercent / 100;
        int startFileNumber = 0;
        int totalFrames = 0;
        for (int i = 0; i < filesList.size(); i++) {
            File fileToRead = filesList.get(i);
            Integer integer = framesInFiles.get(fileToRead);
            totalFrames += integer;
            if (totalFrames > startFrame) {
                startFileNumber = i;
                if (startPositionPercent > 0) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    VideoPlayer.setSetPOSITION(false);
                }
                break;
            }
            positionImage = startFrame;
            buffMap.clear();
            buffDeque.clear();
        }

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
                    if(mainPanel){
                        VideoPlayer.setCurrentFrameLabelText(positionImage, buffMap.size());
                    }

                    if (VideoPlayer.isSetPOSITION()) {
                        return;
                    } else if (VideoPlayer.isPLAY()) {

                        if (countImageFramesChangeIng == 0) {
                            readBytesForImage();

                        } else {
                            image = readImage(buffMap.get(positionForNextPreviousImage - (countImageFramesChangeIng--)));
                            positionImage++;
                            if (image != null) {
                                videoPlayerToShowOneVideo.setBufferedImage(processImage(image, width, height));
                                videoPlayerToShowOneVideo.repaint();
                            }
                        }
                    } else if (VideoPlayer.isPAUSE()) {
                        if (VideoPlayer.isSaveIMAGE()) {
                            image = readImage(buffMap.get(buffMap.size() - countImageFramesChangeIng - 1));
                            String path = MainFrame.getPath() + System.currentTimeMillis() + "-" + numberVideoPanel + ".jpg";
                            File file = new File(path);
                            try {
                                if (file.createNewFile()) {
                                    ImageIO.write(image, "jpg", file);
                                }
                                Thread.sleep(1000);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            if (VideoPlayer.isSaveIMAGE()) {
                                VideoPlayer.ReSetSaveIMAGE();
                            }
                        } else if (VideoPlayer.isNextIMAGE()) {
                            if (nextImageInt != VideoPlayer.getNextImagesInt()) {
                                nextImageInt = VideoPlayer.getNextImagesInt();
                                if (countImageFramesChangeIng == 0) {
                                    readBytesForImage();
                                } else {
                                    image = readImage(buffMap.get(positionForNextPreviousImage - (countImageFramesChangeIng--)));
                                    positionImage++;
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
                                int numberOfFrame = positionForNextPreviousImage - (countImageFramesChangeIng++);
                                if(countImageFramesChangeIng<=buffMap.size()){
                                    image = readImage(buffMap.get(numberOfFrame));
                                } else {
                                    if(mainPanel){
                                        MainFrame.showInformMassage("Немае зображень в буфері",new Color(199, 29, 25));
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
                        int percent = (int) ((positionImage * 100.0) / totalCountFrames);
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
            VideoPlayer.setCurrentFrameLabelText(0, 0);
            VideoPlayer.setCountDoNotShowImageToZero();
            VideoPlayer.setSpeedToZero();
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

    public void setShowVideoNow(boolean showVideoNow) {
        this.showVideoNow = showVideoNow;
    }
}
