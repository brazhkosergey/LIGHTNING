package ui.camera;

import entity.MainVideoCreator;
import ui.main.MainFrame;
import ui.setting.Setting;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

public class VideoCatcher implements Runnable {

    private int whitePercent = -1;
    private int numberGRB;




    private int fps;
    private int maxWidth;
    private int maxHeight;
    private boolean isFullSize;

    private int countDoNotShowImages;
    private int settingCountDoNotShowImages;

    private Map<Long, byte[]> bufferBytes;
    private Map<Long, byte[]> mapBytes;
    private Map<Integer,Boolean> eventsFramesNumber;
    private Deque<Long> timeDeque;

    private VideoCreator videoCreator;
    private BufferedInputStream bufferedInputStream;
    private InputStream inputStream;
    private ByteArrayOutputStream temporaryStream = null;
    private CameraPanel panel;
    private URL url;
    private HttpURLConnection connection = null;

    private boolean catchVideo;
    private boolean changeURL;

    private Thread fpsThread;
    private int fpsNotZero;
    private boolean mainCamera;

    private int stopSaveVideoInt;
    private int sizeVideoSecond;

    private boolean stopSaveVideo;
    private boolean startSaveVideo;

    public VideoCatcher(CameraPanel panel, VideoCreator videoCreator) {
        startSaveVideo = false;
        fpsThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (catchVideo) {
                    if(numberGRB!=MainFrame.getColorRGBNumber()){
                        numberGRB = MainFrame.getColorRGBNumber();
                    }

                    if(settingCountDoNotShowImages!=MainFrame.getDoNotShowImages()){
                        settingCountDoNotShowImages=MainFrame.getDoNotShowImages();
                    }
                    panel.getTitle().setTitle("FPS = " + fps + ". WHITE: " + whitePercent + " %");
                    if (fps != 0) {
                        fpsNotZero = fps;
                    }
                    fps = 0;
                    if (!startSaveVideo) {
                        if (MainVideoCreator.isSaveVideo()) {
                            int frameCount = timeDeque.size();
                            eventsFramesNumber.put(frameCount,MainVideoCreator.isProgramingLightCatch());
                            startSaveVideo = true;
                        }

                        if (sizeVideoSecond != MainFrame.getTimeToSave()) {
                            sizeVideoSecond = MainFrame.getTimeToSave();
                            while (true) {
                                if (timeDeque.size() > sizeVideoSecond * fpsNotZero) {
                                    bufferBytes.remove(timeDeque.pollLast());
                                } else {
                                    break;
                                }
                            }
                        }
                    }

                    if (startSaveVideo) {
                        stopSaveVideoInt++;
                        if (stopSaveVideoInt == sizeVideoSecond) {
                            stopSaveVideo = true;
                        }
                        if (mainCamera) {
                            MainFrame.showInformMassage("Залишилось секунд " + (sizeVideoSecond - stopSaveVideoInt), true);
                        }
                    }
                } else {
                    panel.getTitle().setTitle("Камера вимкнена");
                }
            }
        });

        timeDeque = new ConcurrentLinkedDeque<>();
        bufferBytes = new HashMap<>();
        mapBytes = new HashMap<>();
        eventsFramesNumber = new HashMap<>();
        this.videoCreator = videoCreator;
        this.panel = panel;
        setWidthAndHeight(270, 260);
        panel.repaint();
    }

    public void startCatchVideo(URL url) {
        if (this.url != null) {
            changeURL = true;
            this.url = url;
            catchVideo = false;
        } else {
            this.url = url;
            catchVideo = true;
            panel.startShowVideo();
        }
    }

    private void createInputStream() throws IOException {

        if (url != null) {
            connection = (HttpURLConnection) url.openConnection();
            try {
                inputStream = connection.getInputStream();
                bufferedInputStream = new BufferedInputStream(inputStream);
            } catch (Exception e) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    @Override
    public void run() {
        fpsThread.start();
        int x = 0;
        int t = 0;

        BufferedImage image;
        while (true) {
            while (catchVideo) {
                if (bufferedInputStream == null) {
                    temporaryStream = new ByteArrayOutputStream(65535);
                    try {
                        createInputStream();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (bufferedInputStream != null) {
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
                        if (countDoNotShowImages >= settingCountDoNotShowImages) {
                            ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBytes);
                            try {
                                image = ImageIO.read(inputStream);
                                inputStream.close();
                                panel.setBufferedImage(processImage(image, maxWidth, maxHeight));
                                panel.repaint();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            countDoNotShowImages = 0;
                        } else {
                            countDoNotShowImages++;
                        }

                        long l = System.currentTimeMillis();
                        timeDeque.addFirst(l);
                        bufferBytes.put(l, imageBytes);
                        fps++;

                        if (!startSaveVideo) {
                            if (timeDeque.size() > sizeVideoSecond * fpsNotZero) {
                                bufferBytes.remove(timeDeque.pollLast());
                            }
                        }

                        if (stopSaveVideo) {
                            System.out.println("Стоп сохранять видео.");
                            MainVideoCreator.stopCatchVideo();
                            int size = timeDeque.size();
                            for (int i = 0; i < size; i++) {
                                Long timeLong = timeDeque.pollLast();
                                mapBytes.put(timeLong, bufferBytes.get(timeLong));
                                bufferBytes.remove(timeLong);
                            }

                            int num;
                            if (panel.getCameraNumber() % 2 == 0) {
                                num = 2;
                            } else {
                                num = 1;
                            }
                            videoCreator.addMapByte(num, mapBytes, fpsNotZero, eventsFramesNumber);
                            mapBytes = new HashMap<>();
                            eventsFramesNumber = new HashMap<>();
                            stopSaveVideoInt = 0;
                            stopSaveVideo = false;
                            startSaveVideo = false;
                        }
                    }
                }
            }

            if (!catchVideo) {
                try {
                    if (temporaryStream != null) {
                        temporaryStream.close();
                        temporaryStream = null;
                    }

                    if (bufferedInputStream != null) {
                        bufferedInputStream.close();
                        bufferedInputStream = null;
                    }

                    if (inputStream != null) {
                        inputStream.close();
                        inputStream = null;
                    }

                    if (connection != null) {
                        connection.getInputStream().close();
                        connection.disconnect();
                        connection = null;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (changeURL) {
                catchVideo = true;
            }

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void continueSaveVideo() {
        int frameCount = timeDeque.size();
        eventsFramesNumber.put(frameCount,MainVideoCreator.isProgramingLightCatch());
        stopSaveVideoInt = 0;
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
//                AffineTransformOp op = new AffineTransformOp(tr, AffineTransformOp.TYPE_BILINEAR);
                AffineTransformOp op = new AffineTransformOp(tr, AffineTransformOp.TYPE_BICUBIC);
//                AffineTransformOp op = new AffineTransformOp(tr, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
                Double w = bi.getWidth() * trans;
                Double h = bi.getHeight() * trans;
                bi2 = new BufferedImage(w.intValue(), h.intValue(), bi.getType());
                op.filter(bi, bi2);
            }
        }

        if (bi2 != null) {
            int[] rgb1 = bi.getRGB(0, 0, bi.getWidth(), bi.getHeight(), null, 0, 2048);
            int countWhite = 0;
            int allPixels = rgb1.length;

            int k = 30 - (settingCountDoNotShowImages*2);

            for (int i = 0; i < allPixels; i=i+k) {
              if (rgb1[i] > numberGRB && rgb1[i] < -1) {
                    countWhite++;
                }
            }

            if(countWhite!=0){
                countWhite = countWhite * k;
            }

            double percent = (double) countWhite / allPixels;
            int percentInt = (int) (percent * 100);

            if (whitePercent != -1) {
                int differentWhitePercent = Math.abs(percentInt - whitePercent);
                if (differentWhitePercent > MainFrame.getPercentDiffWhite()) {
                    VideoCreator.startSaveVideoProgram();
                    whitePercent = -1;
                } else {
                    if (percentInt != whitePercent) {
                        whitePercent = percentInt;
                    }
                }
            } else {
                whitePercent = percentInt;
            }
            return bi2;
        } else {
            whitePercent = -1;
            return bi;
        }
    }

    VideoCreator getVideoCreator() {
        return videoCreator;
    }

    public void setWidthAndHeight(int width, int height) {
        if (width > 270) {
            isFullSize = true;
        } else {
            isFullSize = false;
        }

        panel.setWindowSize(width, height);
        panel.setPreferredSize(new Dimension(width+5,height+5));
        this.maxWidth = width;
        this.maxHeight = height;
    }

    public void stopCatchVideo() {
        panel.stopShowVideo();
        url = null;
        catchVideo = false;
    }

    public void setMainCamera(boolean mainCamera) {
        this.mainCamera = mainCamera;
    }

    public boolean isFullSize() {
        return isFullSize;
    }

    public boolean isCatchVideo() {
        return catchVideo;
    }
}
