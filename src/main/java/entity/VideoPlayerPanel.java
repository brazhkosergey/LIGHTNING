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
import java.util.HashMap;
import java.util.Map;

public class VideoPlayerPanel extends JPanel {

    private int whitePercent = -1;
    private int numberGRB;

    private int width;
    private int height;
    private int numberVideoPanel;

    private boolean mainPanel;
    private long currentByteNumber = -1;
    private long fileSize;
    private int position;

    private int FPS;
    private File file;
    private Thread showVideoThread;
    private Thread mainVideoThread;

    private int changeImage;
    private int nextImageInt;
    private int prewImageIng;
    private int countImageFramesChangeIng;

    private VideoPlayerToShowOneVideo videoPlayerToShowOneVideo;
    private JLayer<JPanel> videoStreamLayer;
    private JLabel label;
    private boolean blockHaveVideo = true;
    private boolean videoPlay = true;
    private BufferedInputStream bufferedInputStream = null;
    private FileInputStream fileInputStream = null;
    private ByteArrayOutputStream temporaryStream = null;

    private Map<Integer, byte[]> buffMap;
    private int numberImageInBuffer;

    VideoPlayerPanel(File file, int numberVideoPanel) {
        this.numberVideoPanel = numberVideoPanel;
        this.file = file;
        buffMap = new HashMap<>();
        this.setLayout(new FlowLayout(FlowLayout.CENTER));
        this.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        if (file != null) {
            fileSize = file.length();
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
                    if (VideoPlayer.isPLAY()) {
                        if (!videoPlay) {
                            videoPlay = true;
                            this.remove(label);
                            videoStreamLayer.repaint();
                            this.add(videoStreamLayer);
                            this.revalidate();
                            this.repaint();
                        }

                        if (file != null) {
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

                            try {
                                fileInputStream = new FileInputStream(file);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }

                            bufferedInputStream = new BufferedInputStream(fileInputStream);

                            if (temporaryStream != null) {
                                try {
                                    temporaryStream.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                temporaryStream = null;
                            }
                        }

                        if (VideoPlayer.isSetPOSITION()) {
                            showFrames(VideoPlayer.getPosition());
                        } else {
                            showFrames(0);
                        }
                    } else {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        if (videoPlay) {
                            videoPlay = false;
                            this.remove(videoStreamLayer);
                            this.add(label);
                            this.revalidate();
                            this.repaint();
                        }
                    }

                    if (!VideoPlayer.isShowVideoPlayer()) {
                        blockHaveVideo = false;
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
                }
                buffMap.clear();
                this.mainPanel = false;
            });
            showVideoThread.setName("ShowVideoThread. Player " + numberVideoPanel);
        }
    }

    private void showFrames(int startBytePercent) {
        if (bufferedInputStream != null) {
            temporaryStream = new ByteArrayOutputStream(65535);
            int x = 0;
            int t = 0;
            BufferedImage image = null;
            if (startBytePercent > 0) {
                long startByte = (long) (startBytePercent * fileSize) / 100;

                int smallBuff = 0;
                int maxBuffCount = 0;
                if (startByte > 2147483646) {
                    smallBuff = (int) (startByte % 2147483646);
                    maxBuffCount = (int) (startByte / 2147483646);
                } else {
                    smallBuff = (int) startByte;
                }

                for (int i = 0; i < maxBuffCount; i++) {
                    try {
                        bufferedInputStream.read(new byte[2147483646]);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                try {
                    bufferedInputStream.read(new byte[smallBuff]);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                currentByteNumber = startByte;

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                VideoPlayer.setSetPOSITION(false);
            }

            ByteArrayInputStream inputImageStream;
            BufferedInputStream bufferedInputImageStream;

            while (x >= 0 && VideoPlayer.isShowVideoPlayer()) {
//                if (!VideoPlayer.isShowVideoPlayer()) {
//                    currentByteNumber = 0L;
//                    position = 0;
//                    changeImage = 0;
//                    if (mainPanel) {
//                        VideoPlayer.setSTOP(true);
//                        VideoPlayer.setPLAY(false);
//                        VideoPlayer.setPAUSE(false);
//                        VideoPlayer.setSliderPosition(position);
//                        VideoPlayer.setCountDoNotShowImageToZero();
//                        VideoPlayer.setSpeedToZero();
//                    }
//                    return;
//                } else


                if (VideoPlayer.isSetPOSITION()) {
                    return;
                } else if (VideoPlayer.isPLAY()) {
                    if (countImageFramesChangeIng == 0) {
                        t = x;
                        try {
                            x = bufferedInputStream.read();
                            currentByteNumber++;
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

                            buffMap.put(numberImageInBuffer++, imageBytes);
                            if (changeImage == VideoPlayer.getCountDoNotShowImage()) {

                                try {
                                    inputImageStream = new ByteArrayInputStream(imageBytes);
                                    bufferedInputImageStream = new BufferedInputStream(new ByteArrayInputStream(imageBytes));
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
                        }
                    } else {
                        byte[] bytes = buffMap.get(numberImageInBuffer - (countImageFramesChangeIng--));
                        if (bytes != null) {
                            currentByteNumber = currentByteNumber + bytes.length;
                            try {
                                inputImageStream = new ByteArrayInputStream(bytes);
                                bufferedInputImageStream = new BufferedInputStream(inputImageStream);
                                image = ImageIO.read(bufferedInputImageStream);
                                inputImageStream.close();
                                bufferedInputImageStream.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if (image != null) {
                                videoPlayerToShowOneVideo.setBufferedImage(processImage(image, width, height));
                                videoPlayerToShowOneVideo.repaint();
                            }
                        }
                    }
                } else if (VideoPlayer.isPAUSE()) {
                    if (VideoPlayer.isSaveIMAGE()) {
                        byte[] bytes = buffMap.get(numberImageInBuffer - countImageFramesChangeIng - 1);

                        String path = MainFrame.getPath() + System.currentTimeMillis() + "-" + numberVideoPanel + ".jpg";
                        File file = new File(path);
                        try {
                            if (file.createNewFile()) {
                                FileOutputStream fileOutputStream = new FileOutputStream(file);
                                fileOutputStream.write(bytes);
                                fileOutputStream.flush();
                                fileOutputStream.close();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        if (VideoPlayer.isSaveIMAGE()) {
                            VideoPlayer.ReSetSaveIMAGE();
                        }
                    } else if (VideoPlayer.isNextIMAGE()) {
                        if (nextImageInt != VideoPlayer.getNextImagesInt()) {
                            nextImageInt = VideoPlayer.getNextImagesInt();
                            if (countImageFramesChangeIng == 0) {
                                while (x >= 0) {
                                    t = x;
                                    try {
                                        x = bufferedInputStream.read();
                                        currentByteNumber++;
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
                                        buffMap.put(numberImageInBuffer++, imageBytes);

                                        try {
                                            inputImageStream = new ByteArrayInputStream(imageBytes);
                                            bufferedInputImageStream = new BufferedInputStream(inputImageStream);
                                            image = ImageIO.read(bufferedInputImageStream);
                                            inputImageStream.close();
                                            bufferedInputImageStream.close();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                        if (image != null) {
                                            videoPlayerToShowOneVideo.setBufferedImage(processImage(image, width, height));
                                            videoPlayerToShowOneVideo.repaint();
                                        }
                                        break;
                                    }
                                }
                            } else {
                                byte[] bytes = buffMap.get(numberImageInBuffer - (countImageFramesChangeIng--));
                                if (bytes != null) {
                                    currentByteNumber = currentByteNumber + bytes.length;
                                    try {
                                        inputImageStream = new ByteArrayInputStream(bytes);
                                        bufferedInputImageStream = new BufferedInputStream(inputImageStream);
                                        image = ImageIO.read(bufferedInputImageStream);
                                        inputImageStream.close();
                                        bufferedInputImageStream.close();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    if (image != null) {
                                        videoPlayerToShowOneVideo.setBufferedImage(processImage(image, width, height));
                                        videoPlayerToShowOneVideo.repaint();
                                    }
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
                            byte[] bytes = buffMap.get(numberImageInBuffer - (countImageFramesChangeIng++));
                            if (bytes != null) {
                                currentByteNumber = currentByteNumber - bytes.length;

                                try {
                                    inputImageStream = new ByteArrayInputStream(bytes);
                                    bufferedInputImageStream = new BufferedInputStream(inputImageStream);
                                    image = ImageIO.read(bufferedInputImageStream);
                                    inputImageStream.close();
                                    bufferedInputImageStream.close();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

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
                    } else {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                } else if (VideoPlayer.isSTOP()) {
                    currentByteNumber = 0L;
                    position = 0;
                    VideoPlayer.setSliderPosition(position);
                    return;
                }

                if (mainPanel) {
                    double percent = (double) currentByteNumber / fileSize;
                    percent = percent * 100.0;
                    if (position != (int) percent) {
                        position = (int) percent;
                        VideoPlayer.setSliderPosition(position);
                        System.out.println(position);
                    }
                }
            }

            currentByteNumber = 0L;
            position = 0;
            changeImage = 0;
            if (mainPanel) {
                VideoPlayer.setSTOP(true);
                VideoPlayer.setPLAY(false);
                VideoPlayer.setPAUSE(false);
                VideoPlayer.setSliderPosition(position);
                VideoPlayer.setCountDoNotShowImageToZero();
                VideoPlayer.setSpeedToZero();
            }
        }
    }

    void setMainPanel(boolean mainPanel) {
        this.mainPanel = mainPanel;
        if (mainPanel) {
            if (mainVideoThread == null) {
                mainVideoThread = new Thread(() -> {
                    while (this.mainPanel) {
                        VideoPlayer.FPSLabel.setText("FPS - " + FPS + ":" + whitePercent);
                        VideoPlayer.FPSLabel.repaint();

                        FPS = 0;
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        numberGRB = MainFrame.getColorRGBNumber();
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

    Thread getShowVideoThread() {
        return showVideoThread;
    }

    String getFileName() {
        if (file != null) {
            return file.getName();
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
}
